/*
 * This is a header...
 */
package com.devonfw.ide.sonarqube.service.api;

import com.devonfw.ide.sonarqube.dataaccess.api.Bar; // Noncompliant
import com.devonfw.module.jpa.dataaccess.api.RevisionMetadata; // compliant (build in exclusion)

class MyClass {

  private com.devonfw.ide.sonarqube.dataaccess.api.Foo foo; // Noncompliant

  private Bar bar;

  public com.devonfw.ide.sonarqube.dataaccess.api.Foo getFoo() { // Noncompliant
    return this.foo;
  }

  public void init() {

    this.foo = new com.devonfw.ide.sonarqube.dataaccess.api.Foo();// Noncompliant
  }
}