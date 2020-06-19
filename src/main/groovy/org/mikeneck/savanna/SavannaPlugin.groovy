package org.mikeneck.savanna

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.jetbrains.annotations.NotNull

class SavannaPlugin implements Plugin<Project> {

    @Override
    void apply(@NotNull Project project) {
        def testDescriptions = project.objects.listProperty(String)
        project.tasks.withType(Test).configureEach { Test test ->
            test.testLogging {
                afterTest { TestDescriptor desc, TestResult result ->
                    testDescriptions.add("${desc.className} ${desc.displayName} ${result.testCount} (success: ${result.successfulTestCount}/fail: ${result.failedTestCount})")
                }
            }
        }

        project.tasks.create('savanna') {
            group = 'savanna'
            description = 't-wada lion appears when no tests ran.'
            doLast {
                if (testDescriptions.get().empty) {
                    def url = Thread.currentThread().contextClassLoader.getResource('savanna.txt')
                    logger.lifecycle(url.text)
                }            }
        }

        [project.tasks.check, project.tasks.build]*.finalizedBy('savanna')
    }
}
