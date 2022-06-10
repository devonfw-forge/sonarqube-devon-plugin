package com.devonfw.ide.sonarqube.common.impl.check.thirdparty;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

/**
 * Test of {@link DevonArchitecture3rdPartyDatatypeMappingsCheck}
 */
public class DevonArchitecture3rdPartyDatatypeMappingsCheckTest {

  /**
   * Test of {@link DevonArchitecture3rdPartyDatatypeMappingsCheck} This test checks if the import of
   * javax.persistence.Convert is detected as non-compliant
   */
  @Test
  public void testJavaxPersistenceConvert() {

    JavaCheckVerifier.verify(
        "src/test/files/thirdparty/DevonArchitecture3rdPartyDatatypeMappingsCheck_JavaxPersistenceConvert.java",
        new DevonArchitecture3rdPartyDatatypeMappingsCheck());
  }

  /**
   * Test of {@link DevonArchitecture3rdPartyDatatypeMappingsCheck} This test checks if the import of org.hibernate.Type
   * is detected as non-compliant
   */
  @Test
  public void testOrgHibernateType() {

    JavaCheckVerifier.verify(
        "src/test/files/thirdparty/DevonArchitecture3rdPartyDatatypeMappingsCheck_OrgHibernateType.java",
        new DevonArchitecture3rdPartyDatatypeMappingsCheck());
  }

  /**
   * Test of {@link DevonArchitecture3rdPartyDatatypeMappingsCheck} This test checks if the import of
   * javax.persistence.Converter is detected as compliant
   */
  @Test
  public void testJavaxPersistenceConverter() {

    JavaCheckVerifier.verifyNoIssue(
        "src/test/files/thirdparty/DevonArchitecture3rdPartyDatatypeMappingsCheck_JavaxPersistenceConverter.java",
        new DevonArchitecture3rdPartyDatatypeMappingsCheck());
  }

  /**
   * Test of {@link DevonArchitecture3rdPartyDatatypeMappingsCheck} This test checks if the check does not throw an
   * exception when investigating a file without imports
   */
  @Test
  public void testNoTarget() {

    JavaCheckVerifier.verifyNoIssue(
        "src/test/files/thirdparty/DevonArchitecture3rdPartyDatatypeMappingsCheck_NoTarget.java",
        new DevonArchitecture3rdPartyDatatypeMappingsCheck());
  }

}
