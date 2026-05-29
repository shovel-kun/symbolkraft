package com.ebisuzawa.symbolkraft.plugin.samples

import com.ebisuzawa.symbolkraft.plugin.SymbolKraftExtension

/** Sample functions referenced from KDoc to demonstrate local icon configuration. */
@Suppress("unused") // Referenced via KDoc @sample
internal fun SymbolKraftExtension.localIconsIncludeSample() {
    localIcons(libraryName = "brand") {
        directory = "design/exported"
        include("brand/**/*.svg")
    }
}

@Suppress("unused") // Referenced via KDoc @sample
internal fun SymbolKraftExtension.localIconsExcludeSample() {
    localIcons {
        directory = "src/commonMain/resources/icons"
        exclude("legacy/**")
    }
}
