package com.devonfw.ide.sonarqube.common.impl.check.layer;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

/**
 * Test of {@link DevonArchitectureLayerService2BatchCheck}.
 */
public class DevonArchitectureLayerService2BatchCheckTest {

  /**
   * Test of {@link DevonArchitectureLayerService2BatchCheck}.
   */
  @Test
  public void test() {

    JavaCheckVerifier.verify("src/test/files/layer/DevonArchitectureLayerService2BatchCheck.java",
        new DevonArchitectureLayerService2BatchCheck());
  }

}
