import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    testImplementation(libs.junit)
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.7.1.202607240634-r") {
        exclude(group = "org.slf4j")
    }

    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        version = provider { project.version.toString() }

        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n")
            }.let(::markdownToHTML)
        }

        changeNotes = provider {
            changelog.renderItem(
                changelog.getOrNull(project.version.toString()) ?: changelog.getUnreleased(),
                org.jetbrains.changelog.Changelog.OutputType.HTML,
            )
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        failureLevel = listOf(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS,
            VerifyPluginTask.FailureLevel.INVALID_PLUGIN,
            VerifyPluginTask.FailureLevel.MISSING_DEPENDENCIES,
        )
        ides {
            recommended()
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = "https://github.com/0zd0/context2prompt"
}
