/*
 * Copyright 2020 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mikeneck.savanna

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import run.ktcheck.Assertion
import run.ktcheck.Given
import run.ktcheck.KtCheck
import run.ktcheck.assertion.NoDep.satisfies
import run.ktcheck.assertion.NoDep.should
import run.ktcheck.assertion.StringMatchers.contain
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption


data class SavannaPluginCheck(val name: String, val rootPath: Path) {

  fun srcMainJava(operation: Directory.() -> Unit): Directory =
      Directory(Files.createDirectories(rootPath.resolve("src/main/java"))).also(operation)

  fun srcTestJava(operation: Directory.() -> Unit): Directory =
      Directory(Files.createDirectories(rootPath.resolve("src/test/java"))).also(operation)

  fun buildGradle(contents: String): Gradle =
      Files.write(rootPath.resolve("build.gradle"), 
          listOf(contents),
          Charsets.UTF_8,
          StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
          .let { Gradle(GradleRunner.create(), rootPath) }

  fun resourceText(resourceName: String): String =
      Thread.currentThread().contextClassLoader.getResource(resourceName)?.readText(Charsets.UTF_8) ?: throw IOException("resource <$resourceName> not found")

  fun lionComesAt(result: BuildResult): Assertion =
      result.output should contain(resourceText("savanna.txt"))

  fun lionNeverComesAt(result: BuildResult): Assertion =
      result.output satisfies {
        it.contains(resourceText("savanna.txt")).not()
      }

  companion object {
    fun start(name: String): SavannaPluginCheck =
        SavannaPluginCheck(name, Files.createTempDirectory(name))
  }
}

data class Directory(val path: Path) {
  operator fun String.invoke(contents: String): File =
      path.resolve(this)
          .also { if (!Files.exists(it.parent)) Files.createDirectories(it.parent) }
          .let { File(Files.write(it, contents.toByteArray(Charsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) }

  operator fun String.invoke(operation: Directory.() -> Unit): Directory =
      Directory(Files.createDirectories(path.resolve(this))).also(operation)
}

data class File(val path: Path) {
  val text: String = Files.newBufferedReader(path, Charsets.UTF_8).readText()
}

class Gradle(private val runner: GradleRunner, private val rootPath: Path) {
  operator fun invoke(vararg arguments: String): BuildResult =
      runner.also { it.withProjectDir(rootPath.toFile()) }
          .also { it.forwardOutput() }
          .also { it.withPluginClasspath() }
          .also { it.withArguments(*arguments) }
          .build()
}

object LionComesWhenRunBuildTaskWithoutTests: KtCheck
by Given(
    description = "Java project without tests",
    before = { SavannaPluginCheck.start("java-project-without-tests") },
    after = { println(this.rootPath) },
    action = { 
      srcMainJava {
        "com/example/App.java"(
            //language=java
            """package com.example;
class App {
  public static void main(String[] args){
    System.out.println(new App().message());
  }
  public String message() { return "hello savanna!"; }
}
""")
      }
      return@Given buildGradle(
          //language=gradle
          """
plugins {
  id 'java'
  id 'org.mikeneck.savanna-gradle-plugin'
}
repositories {
  mavenCentral()
}
dependencies {
  testImplementation 'org.junit.jupiter:junit-jupiter:5.6.0'
}
test {
  useJUnitPlatform()
}
"""
      )
    }
)
    .When("Run build task", { gradle -> 
      gradle("build")
    })
    .Then("Lion comes!", { _, result ->
      lionComesAt(result)
    })

object LionNeverComesWhenRunBuildTaskWithTests: KtCheck
by Given(
    description = "Java project with tests",
    before = { SavannaPluginCheck.start("java-project-without-tests") },
    action = {
      srcMainJava {
        "com/example/App.java"(
            //language=java
            """package com.example;
class App {
  public static void main(String[] args){
    System.out.println(new App().message());
  }
  public String message() { return "hello savanna!"; }
}
""")
      }
      srcTestJava { 
        "com/example/AppTest.java"(
            //language=java
            """package com.example;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {
  @Test
  void messageContainsSavanna() {
    App app = new App();
    assertTrue(app.message().contains("savanna"));
  }
}
""".trimIndent())
      }
      return@Given buildGradle(
          //language=gradle
          """
plugins {
  id 'java'
  id 'org.mikeneck.savanna-gradle-plugin'
}
repositories {
  mavenCentral()
}
dependencies {
  testImplementation 'org.junit.jupiter:junit-jupiter:5.6.0'
}
test {
  useJUnitPlatform()
}
"""
      )
    }
)
    .When("Run build task", { gradle ->
      gradle("build")
    })
    .Then("Lion never comes!", { _, result ->
      lionNeverComesAt(result)
    })
    .When("Run build task skipping tests", { gradle ->
      gradle("build", "-x", "test")
    })
    .Then("Lion comes!!!!", { _, result ->
      lionComesAt(result)
    })
