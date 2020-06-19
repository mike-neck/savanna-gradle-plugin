package org.mikeneck.savanna

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

import java.nio.file.Files

class SavannaPluginTest {

    @Test
    void runPlugin() {
        def project = ProjectBuilder.builder().build()
        project.plugins.apply('java')
        project.plugins.apply('org.mikeneck.savanna-gradle-plugin')

        def task = project.tasks.findByName('savanna')
        assert task != null
        assert task.group == 'savanna'
    }

    @Test
    void runTests() {
        def projectDir = Files.createTempDirectory('savanna-plugin-test')
        // language=gradle
        projectDir.resolve('build.gradle').write("""
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
""", 'UTF-8')
        def javaFile = projectDir.resolve('src/main/java/com/example/App.java')
        Files.createDirectories(javaFile.parent)
        //language=java
        javaFile.write("""package com.example;
class App {
    public static void main(String[] args) {
        System.out.println(new App().message());
    }
    public String message() { return "hello"; }
}
""")
        def gradleRunner = GradleRunner.create()
        gradleRunner.forwardOutput()
        gradleRunner.withPluginClasspath()
        gradleRunner.withArguments('build', '--stacktrace')
        gradleRunner.withProjectDir(projectDir.toFile())
        def result = gradleRunner.build()

        def savanna = Thread.currentThread().contextClassLoader.getResource('savanna.txt')

        assert result.output.contains(savanna.text)
    }
}
