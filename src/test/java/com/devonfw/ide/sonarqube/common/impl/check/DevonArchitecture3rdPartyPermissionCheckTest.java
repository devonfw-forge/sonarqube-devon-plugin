package com.devonfw.ide.sonarqube.common.impl.check;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

/**

 */
public class DevonArchitecture3rdPartyPermissionCheckTest {

  /**
   *
   */
  @Test
  public void test() {

    /*
     * JavaCheckVerifier.verify("src/test/files/DevonArchitecture3rdPartyPermissionCheck" +
     * "/DevonArchitecture3rdPartyPermissionCheck_impl.java", new DevonArchitecture3rdPartyPermissionCheck());
     */

    /*
     * JavaCheckVerifier.verify("src/test/files/DevonArchitecture3rdPartyPermissionCheck" +
     * "/DevonArchitecture3rdPartyPermissionCheck_impl.java", new DevonArchitecture3rdPartyPermissionCheck());
     */
  }

  /**
   *
   */
  @Test
  public void testNoIssue() {

    JavaCheckVerifier.verifyNoIssue("src\\test\\files\\DevonArchitecture3rdPartyPermissionCheck"
        + "\\DevonArchitecture3rdPartyPermissionCheck_impl.java", new DevonArchitecture3rdPartyPermissionCheck());
  }

}
