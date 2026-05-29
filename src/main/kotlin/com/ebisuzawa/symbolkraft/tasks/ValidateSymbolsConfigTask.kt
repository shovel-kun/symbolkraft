package com.ebisuzawa.symbolkraft.tasks

import com.ebisuzawa.symbolkraft.plugin.SymbolKraftExtension
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Task that validates the DSL configuration before generation.
 *
 * Exposed to consumers as `validateSymbolsConfig`.
 */
abstract class ValidateSymbolsConfigTask : DefaultTask() {
    @get:Internal abstract val extension: Property<SymbolKraftExtension>

    /** Checks that at least one icon configuration has been declared. */
    @TaskAction
    fun validate() {
        val config = extension.get().getIconsConfig()
        if (config.isEmpty()) {
            throw IllegalStateException(
                "No icons configured. Use symbolKraft { } in build.gradle.kts"
            )
        }
        val count = config.values.sumOf { it.size }
        logger.lifecycle(
            "✅ Valid configuration. Icons: ${config.size}, Total configurations: $count"
        )
    }
}
