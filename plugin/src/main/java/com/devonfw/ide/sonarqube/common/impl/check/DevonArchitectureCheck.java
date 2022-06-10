package com.devonfw.ide.sonarqube.common.impl.check;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.ModifierKeywordTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.PackageDeclarationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.devonfw.ide.sonarqube.common.api.JavaType;
import com.devonfw.ide.sonarqube.common.api.config.Architecture;
import com.devonfw.ide.sonarqube.common.api.config.Configuration;
import com.devonfw.ide.sonarqube.common.api.config.DevonArchitecturePackage;
import com.devonfw.ide.sonarqube.common.api.config.Packages;
import com.devonfw.ide.sonarqube.common.impl.config.ConfigurationFactory;

/**
 * Abstract base class for all SonarQube architecture checks of this plugin.
 */
public abstract class DevonArchitectureCheck extends BaseTreeVisitor implements JavaFileScanner {

  private DevonArchitecturePackage sourcePackage;

  private JavaType sourceType;

  private JavaFileScannerContext context;

  private int packageLine;

  private List<ImportTree> imports;

  private Packages packages;

  private Configuration configuration;

  private static final Logger logger = Logger.getGlobal();

  /**
   * The constructor.
   */
  public DevonArchitectureCheck() {

    super();
    this.imports = new ArrayList<>();
  }

  /**
   * Called in case of a dependency that is devonfw compliant.
   *
   * @param source the {@link JavaType} to analyze.
   * @param target the {@link JavaType} used by the source type (as dependency).
   * @return the message of an issue to create due to an undesired dependency or {@code null} if dependency is fine.
   */
  protected abstract String checkDependency(JavaType source, JavaType target);

  /**
   * Called from {@link #scanFile(JavaFileScannerContext)} after the {@link Configuration} has been set.
   *
   * @param ctx the {@link JavaFileScannerContext}.
   */
  protected void onConfigurationSet(JavaFileScannerContext ctx) {

  }

  @Override
  public final void scanFile(JavaFileScannerContext fileContext) {

    this.imports.clear();
    this.context = fileContext;
    this.configuration = ConfigurationFactory.get(getFileToScan());
    if (this.configuration == null) {
      this.configuration = new Configuration();
    }
    this.packages = Architecture.getPackages(this.configuration.getArchitecture());
    onConfigurationSet(this.context);
    ClassTree tree = getClassTree(this.context);

    if (tree == null) {
      logger.log(Level.INFO, "Tree currently being investigated is not of type ClassTree.");
      return;
    } else {
      doScanFile(tree, this.context);
      scan(this.context.getTree());
    }

    this.context = null;
    this.sourcePackage = null;
  }

  @Override
  public void visitImport(ImportTree tree) {

    this.imports.add(tree);
    super.visitImport(tree);
  }

  @Override
  public void visitVariable(VariableTree variableTree) {

    String qualifiedName = getQualifiedName(variableTree.type());
    checkIfDisallowed(qualifiedName, variableTree.type());
    super.visitVariable(variableTree);
  }

  @Override
  public void visitMethod(MethodTree methodTree) {

    if (methodTree.returnType() != null) {
      String returnTypeName = getQualifiedName(methodTree.returnType());
      checkIfDisallowed(returnTypeName, methodTree.returnType());
    }
    super.visitMethod(methodTree);
  }

  @Override
  public void visitNewClass(NewClassTree newClassTree) {

    String newClassTypeName = getQualifiedName(newClassTree.identifier());
    Tree parent = newClassTree.parent();
    if (parent != null && !parent.is(Tree.Kind.VARIABLE)) {
      checkIfDisallowed(newClassTypeName, newClassTree);
    }
    super.visitNewClass(newClassTree);
  }

  @Override
  public void visitClass(ClassTree classTree) {

    IdentifierTree simpleNameTree = classTree.simpleName();
    String simpleName;
    if (simpleNameTree == null) {
      simpleName = "";
    } else {
      simpleName = simpleNameTree.name();
    }
    this.sourceType = new JavaType(this.sourcePackage, simpleName);
    for (ImportTree tree : this.imports) {
      String qualifiedName = getQualifiedName(tree.qualifiedIdentifier());
      checkIfDisallowed(qualifiedName, tree);
    }
    if (classTree.parent() instanceof CompilationUnitTree) {
      String warning = createIssueForInvalidSourcePackage(this.sourceType, classTree);
      if (warning != null) {
        if (this.sourcePackage == null) {
          this.packageLine = classTree.firstToken().line();
        }
        this.context.addIssue(this.packageLine, this, warning);
      }
    }
    TypeTree superClass = classTree.superClass();
    if (superClass != null) {
      String superClassTypeName = superClass.symbolType().fullyQualifiedName();
      checkIfDisallowed(superClassTypeName, superClass);
    }
    super.visitClass(classTree);
  }

  private void checkIfDisallowed(String className, Tree tree) {

    if (!isTreeAndSourcePackageValid(tree) || className == null) {
      return;
    }

    int lastDot = className.lastIndexOf('.');
    if (lastDot <= 0) {
      return;
    }

    String pkgName = className.substring(0, lastDot);
    String simpleName = className.substring(lastDot + 1);
    DevonArchitecturePackage targetPkg = DevonArchitecturePackage.of(pkgName, this.packages);
    JavaType targetType = new JavaType(targetPkg, simpleName);
    String warning = null;

    if (!targetPkg.isValid() || (targetPkg.getRoot() == null)) {
      if (isCheckDependencyOnInvalidPackage()) {
        warning = checkDependency(this.sourceType, targetType);
      }
    } else {
      if (targetPkg.getRoot().startsWith("com.devonfw") && !this.sourcePackage.hasSameRoot(targetPkg)
          && isTargetDependencyAllowed(targetPkg)) {
        return;
      }
      warning = checkDependency(this.sourceType, targetType);
    }

    if (warning != null) {
      int line = tree.firstToken().line();
      this.context.addIssue(line, this, warning);
    }

  }

  private boolean isTreeAndSourcePackageValid(Tree tree) {

    return (!tree.is(Tree.Kind.INFERED_TYPE) && this.sourcePackage != null && this.sourcePackage.isValid());
  }

  private boolean isTargetDependencyAllowed(DevonArchitecturePackage targetPkg) {

    boolean targetDependencyAllowed;
    String targetComponent = targetPkg.getComponent();

    if (targetComponent.equals("jpa")) {
      targetDependencyAllowed = this.sourcePackage.isLayerDataAccess();
    } else if (targetComponent.equals("batch")) {
      targetDependencyAllowed = this.sourcePackage.isLayerBatch();
    } else {
      targetDependencyAllowed = true;
    }

    return targetDependencyAllowed;
  }

  /**
   * @return {@code true} if {@link #checkDependency(JavaType, JavaType)} shall also be called for invalid target
   *         packages, {@code false} otherwise.
   */
  protected boolean isCheckDependencyOnInvalidPackage() {

    return false;
  }

  @Override
  public void visitPackage(PackageDeclarationTree tree) {

    String qualifiedName = getQualifiedName(tree.packageName());
    this.sourcePackage = DevonArchitecturePackage.of(qualifiedName, this.packages);
    this.packageLine = tree.firstToken().line();
    this.sourceType = new JavaType(this.sourcePackage, null);
    super.visitPackage(tree);
  }

  /**
   * @param tree the {@link Tree} name to traverse.
   * @return the qualified name.
   */
  protected String getQualifiedName(Tree tree) {

    if (tree == null) {
      return "";
    }
    QualifiedNameVisitor qnameVisitor = new QualifiedNameVisitor();
    tree.accept(qnameVisitor);
    return qnameVisitor.getQualifiedName();
  }

  /**
   * @param fileContext of analysis containing the parsed tree.
   * @return ClassTree instance.
   */
  protected ClassTree getClassTree(JavaFileScannerContext fileContext) {

    CompilationUnitTree parsedTree = fileContext.getTree();
    List<Tree> types = parsedTree.types();

    for (Tree tree : types) {
      if (tree instanceof ClassTree) {
        return (ClassTree) tree;
      }
    }

    return null;
  }

  /**
   * Creates a new {@link File} out of the currently analyzed file.
   *
   * @return {@link File} of the file to scan
   */
  protected File getFileToScan() {

    return new File(this.context.getInputFile().toString());
  }

  /**
   * Returns all methods of the given tree.
   *
   * @param tree Tree currently being investigated.
   * @return List of MethodTree.
   */
  protected List<MethodTree> getMethodsOfTree(ClassTree tree) {

    List<Tree> membersOfTree = tree.members();
    List<MethodTree> methodsOfTree = new ArrayList<>();

    for (Tree member : membersOfTree) {
      if (member.is(Tree.Kind.METHOD)) {
        methodsOfTree.add((MethodTree) member);
      }
    }

    return methodsOfTree;
  }

  /**
   * Checks if a method has a public modifier.
   *
   * @param method to be checked
   * @return true or false
   */
  protected boolean isMethodPublic(MethodTree method) {

    for (ModifierKeywordTree modifier : method.modifiers().modifiers()) {
      if (modifier.modifier() == Modifier.PUBLIC) {
        return true;
      }
    }

    return false;
  }

  /**
   * @return the {@link Configuration} for the current project.
   */
  protected Configuration getConfiguration() {

    return this.configuration;
  }

  /**
   * @return the {@link Packages} for the current project.
   */
  public Packages getPackages() {

    return this.packages;
  }

  /**
   * @param source the {@link JavaType} of the source type to analyze.
   * @param classTree the {@link ClassTree} of the top-level type.
   * @return the message of an issue to create in case the source package itself is invalid.
   */
  protected String createIssueForInvalidSourcePackage(JavaType source, ClassTree classTree) {

    return null;
  }

  /**
   * @param tree Tree currently being investigated.
   * @param fileContext of analysis containing the parsed tree.
   */
  protected abstract void doScanFile(ClassTree tree, JavaFileScannerContext fileContext);

  /**
   * @param source the {@link JavaType} of the source type.
   * @param target the {@link JavaType} of the target type referenced from the source type.
   * @return {@code true} if both {@link JavaType}s have the same {@link DevonArchitecturePackage#getComponent()
   *         component} and {@link DevonArchitecturePackage#getLayer() layer}, {@code false} otherwise.
   */
  protected boolean isSameOrGeneralComponentWithSameOrCommonLayer(DevonArchitecturePackage source,
      DevonArchitecturePackage target) {

    if (target.isLayerCommon() || target.hasSameLayer(source)) {
      if (target.isComponentGeneral()) {
        return true;
      }
      if (target.hasSameComponent(source) && target.hasSameRoot(source)) {
        return true;
      }
    }
    return false;
  }

}
