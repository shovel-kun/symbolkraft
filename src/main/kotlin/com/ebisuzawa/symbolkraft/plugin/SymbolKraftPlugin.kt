package com.ebisuzawa.symbolkraft.plugin

import com.ebisuzawa.symbolkraft.tasks.CleanSymbolsCacheTask
import com.ebisuzawa.symbolkraft.tasks.CleanSymbolsIconsTask
import com.ebisuzawa.symbolkraft.tasks.GenerateSymbolsTask
import com.ebisuzawa.symbolkraft.tasks.ValidateSymbolsConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin entry point registered as `com.ebisuzawa.symbolkraft`.
 *
 * The plugin wires the [SymbolKraftExtension] DSL, registers generation/cleanup tasks, and ensures
 * Kotlin compilation depends on freshly generated icons.
 */
class SymbolKraftPlugin : Plugin<Project> {
    /** Installs the extension and all supporting tasks on the target [project]. */
    override fun apply(project: Project) {
        val extension = project.extensions.create("symbolKraft", SymbolKraftExtension::class.java)
        extension.projectDirectory.set(project.layout.projectDirectory.asFile.absolutePath)

        val generateTaskProvider =
            project.tasks.register("generateSymbolKraftIcons", GenerateSymbolsTask::class.java) {
                task ->
                task.group = "symbolkraft"
                task.description = "Generate icons from configured libraries"
                task.extension.set(extension)
                task.outputDir.set(project.layout.projectDirectory.dir(extension.outputDirectory))
                task.cacheDirectory.set(extension.cacheDirectory)
                task.gradleUserHomeDir.set(project.gradle.gradleUserHomeDir.absolutePath)
                task.projectBuildDir.set(project.layout.buildDirectory.get().asFile.absolutePath)
                task.inputs.property("symbolsConfig", extension.getConfigHash())
                task.inputs.property("generatePreview", extension.generatePreview)
                task.inputs.property("namingConfigSignature", extension.namingConfigSignature())
            }

        project.tasks.register("cleanSymbolKraftCache", CleanSymbolsCacheTask::class.java) { task ->
            task.group = "symbolkraft"
            task.description = "Clean SymbolKraft icon cache"
            task.cacheDirectory.set(extension.cacheDirectory)
            task.projectBuildDir.set(project.layout.buildDirectory.get().asFile.absolutePath)
        }

        project.tasks.register("cleanSymbolKraftIcons", CleanSymbolsIconsTask::class.java) { task ->
            task.group = "symbolkraft"
            task.description = "Clean generated SymbolKraft icon files"
            task.packageName.set(extension.packageName)
            task.outputDirectory.set(project.layout.projectDirectory.dir(extension.outputDirectory))
        }

        project.tasks.register(
            "validateSymbolKraftConfig",
            ValidateSymbolsConfigTask::class.java,
        ) { task ->
            task.group = "symbolkraft"
            task.description = "Validate SymbolKraft icon configuration"
            task.extension.set(extension)
        }

        project.afterEvaluate {
            // Make Kotlin compilation depend on our generation
            project.tasks.configureEach { task ->
                val n = task.name
                if (
                    n.startsWith("compile", ignoreCase = true) &&
                        n.contains("Kotlin", ignoreCase = true)
                ) {
                    task.dependsOn(generateTaskProvider)
                }
                // Fix metadata compilation dependency for multiplatform projects
                if (n.contains("compileCommonMainKotlinMetadata", ignoreCase = true)) {
                    task.dependsOn(generateTaskProvider)
                }
                // Fix Android compilation dependencies
                if (
                    n.contains("compileDebugKotlin", ignoreCase = true) ||
                        n.contains("compileReleaseKotlin", ignoreCase = true)
                ) {
                    task.dependsOn(generateTaskProvider)
                }
                // Fix Android asset merging dependency
                if (
                    n.contains("merge", ignoreCase = true) &&
                        n.contains("Assets", ignoreCase = true)
                ) {
                    task.dependsOn(generateTaskProvider)
                }
                // Also add dependency for resource processing
                if (
                    n.contains("process", ignoreCase = true) &&
                        n.contains("Resources", ignoreCase = true)
                ) {
                    task.dependsOn(generateTaskProvider)
                }
            }
        }
    }
}
