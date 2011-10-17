/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.bytecode.loader;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.sonar.java.ast.SquidTestUtils;

public class SquidClassLoaderTest {

  /**
   * See SONAR-2824:
   * Created ClassLoader should be able to load classes only from JDK and from provided list of JAR-files,
   * thus it shouldn't be able to load his class.
   */
  @Test
  public void shouldBeIsolated() throws Exception {
    SquidClassLoader classLoader = new SquidClassLoader(Collections.EMPTY_LIST);
    try {
      classLoader.loadClass(SquidClassLoader.class.getName());
      fail();
    } catch (ClassNotFoundException e) {
      // ok
    }
    assertThat(classLoader.loadClass("java.lang.Integer"), not(nullValue()));
    assertThat(classLoader.getResource("java/lang/Integer.class"), not(nullValue()));
  }

  @Test
  public void createFromJar() throws Exception {
    File jar = SquidTestUtils.getFile("/bytecode/lib/hello.jar");
    SquidClassLoader classLoader = new SquidClassLoader(Arrays.asList(jar));

    assertThat(classLoader.loadClass("org.sonar.tests.Hello"), not(nullValue()));
    assertThat(classLoader.getResource("org/sonar/tests/Hello.class"), not(nullValue()));
    try {
      classLoader.loadClass("foo.Unknown");
      fail();
    } catch (ClassNotFoundException e) {
      // ok
    }

    classLoader.close();
  }

  @Test
  public void unknownJarIsIgnored() throws Exception {
    File jar = SquidTestUtils.getFile("/bytecode/lib/unknown.jar");
    SquidClassLoader classLoader = new SquidClassLoader(Arrays.asList(jar));

    assertThat(classLoader.getResource("org/sonar/tests/Hello.class"), nullValue());

    classLoader.close();
  }

  @Test
  public void createFromDirectory() throws Exception {
    File dir = SquidTestUtils.getFile("/bytecode/bin/");
    SquidClassLoader classLoader = new SquidClassLoader(Arrays.asList(dir));

    assertThat(classLoader.loadClass("tags.TagName"), not(nullValue()));
    assertThat(classLoader.getResource("tags/TagName.class"), not(nullValue()));
    try {
      classLoader.loadClass("tags.Unknown");
      fail();
    } catch (ClassNotFoundException e) {
      // ok
    }

    classLoader.close();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void findResourcesNotSupported() throws Exception {
    SquidClassLoader classLoader = new SquidClassLoader(Collections.EMPTY_LIST);
    classLoader.findResources("");
  }

}
