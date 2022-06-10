package com.devonfw.ide.sonarqube.common.impl.check.naming;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.TypeTree;

import com.devonfw.ide.sonarqube.common.impl.check.DevonArchitectureCodeCheck;

/**
 * Abstract base class for naming convention checks of interfaces
 */
public abstract class DevonNamingConventionInterfaceExtendsInterfaceCheck extends DevonArchitectureCodeCheck {

  /**
   * This needs to be the suffix of the checked interface if it extends certain other interfaces.
   */
  protected final Pattern extendingInterfaceSuffixRegEx;

  /**
   * The constructor.
   *
   * @param extendingInterfaceSuffix See JavaDoc on variable declaration.
   */
  public DevonNamingConventionInterfaceExtendsInterfaceCheck(String extendingInterfaceSuffix) {

    this.extendingInterfaceSuffixRegEx = Pattern.compile(extendingInterfaceSuffix);
  }

  /**
   * Method called after parsing and semantic analysis has been done on file.
   *
   * @param tree Tree currently being investigated.
   * @param context Context of analysis containing the parsed tree.
   */
  @Override
  public void doScanFile(ClassTree tree, JavaFileScannerContext context) {

    String interfaceName = tree.simpleName().name();
    Set<String> superInterfacesNames = getSuperInterfacesNames(tree);

    if (doesSuperInterfaceHaveRegEx(superInterfacesNames) && !doesInterfaceHaveRegEx(interfaceName)) {
      context.addIssue(tree.openBraceToken().line(), this,
          "If a superinterface has " + this.extendingInterfaceSuffixRegEx.toString()
              + " as suffix, then the subinterface should also have " + this.extendingInterfaceSuffixRegEx.toString()
              + " as suffix");
    }

  }

  /**
   * Gets the names of all super interfaces of the checked interface.
   *
   * @param tree Tree currently being investigated.
   * @return List of names
   */
  protected Set<String> getSuperInterfacesNames(ClassTree tree) {

    Set<String> superInterfacesNames = new LinkedHashSet<>();

    for (TypeTree typeTree : tree.superInterfaces()) {
      superInterfacesNames.add(typeTree.toString());
    }
    return superInterfacesNames;
  }

  /**
   * Checks if the name of the checked interface matches the reg ex pattern.
   *
   * @param interfaceName Name of the checked interface.
   * @return True or false.
   */
  protected boolean doesInterfaceHaveRegEx(String interfaceName) {

    return this.extendingInterfaceSuffixRegEx.matcher(interfaceName).matches();
  }

  /**
   * Checks if one of the super interfaces matches the reg ex pattern.
   *
   * @param superInterfaces List of super interfaces of the checked interface.
   * @return True or false.
   */
  protected boolean doesSuperInterfaceHaveRegEx(Set<String> superInterfaces) {

    for (String superInterface : superInterfaces) {
      if (this.extendingInterfaceSuffixRegEx.matcher(superInterface).matches()) {
        return true;
      }
    }

    return false;
  }

}
