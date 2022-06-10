package com.devonfw.ide.sonarqube.common.impl.check.component;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

/**
 * Test of {@link DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck}.
 */
public class DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheckTest {

  /**
   * Test of {@link DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck}.
   */
  @Test
  public void testOKSameComponent() {

    JavaCheckVerifier.verifyNoIssue(
        "src/test/files/component/DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck_OKSameComponent.java",
        new DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck());
  }

  /**
   * Test of {@link DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck}.
   */
  @Test
  public void testOKDifferentComponent() {

    JavaCheckVerifier.verifyNoIssue(
        "src/test/files/component/DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck_OKDifferentComponent.java",
        new DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck());
  }

  /**
   * Test of {@link DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck}.
   */
  @Test
  public void testNotOK() {

    JavaCheckVerifier.verify(
        "src/test/files/component/DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck_NotOK.java",
        new DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck());
  }

  /**
   * Test of {@link DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck}
   */
  @Test
  public void testPackageInfoCase() {

    JavaCheckVerifier.verifyNoIssue("src/test/files/DevonNPEOnPackageInfoCheck.java",
        new DevonArchitectureLayerDataaccess2Dataaccess4ComponentCheck());
  }

}
