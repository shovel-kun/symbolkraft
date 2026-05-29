# SymbolKraft Example Application

This is a complete Kotlin Multiplatform example application demonstrating the usage of the **SymbolKraft** Gradle plugin.

SymbolKraft is a modified fork of [SymbolCraft](https://github.com/kingsword09/SymbolCraft) published under `com.ebisuzawa` coordinates.

## Overview

This example showcases:
- **Multi-platform support**: Android, iOS, and Desktop (JVM)
- **Icon generation**: Using SymbolKraft to generate icons from multiple sources
- **Material Symbols**: Various weights, variants, and fill states
- **External icon libraries**: MDI (Material Design Icons) integration
- **Compose Preview**: Generated preview functions for all icons
- **Modern configuration**: Using the latest SymbolKraft DSL features

## Project Structure

```
example/
├── composeApp/                    # Shared Compose Multiplatform app
│   ├── src/
│   │   ├── commonMain/           # Common code for all platforms
│   │   │   └── kotlin/
│   │   │       ├── generated/    # Generated icons (gitignored)
│   │   │       │   └── symbols/  # SymbolKraft output
│   │   │       └── App.kt        # Main app composable
│   │   ├── androidMain/          # Android-specific code
│   │   ├── iosMain/              # iOS-specific code
│   │   └── jvmMain/              # Desktop-specific code
│   └── build.gradle.kts          # SymbolKraft configuration
└── iosApp/                        # iOS app wrapper
```

## SymbolKraft Configuration

The example demonstrates various configuration options in `composeApp/build.gradle.kts`:

```kotlin
symbolKraft {
    // Output directory for generated icons
    outputDirectory.set("src/commonMain/kotlin/generated/symbols")
    packageName.set("com.ebisuzawa.symbolkraft.example")
    generatePreview.set(true)

    // Icon naming configuration
    naming {
        pascalCase()  // Use PascalCase convention
    }

    // Material Symbols examples
    materialSymbol("search") {
        standardWeights() // Adds 400, 500, 700 weights
    }

    materialSymbol("home") {
        weights(400, 500, variant = SymbolVariant.ROUNDED)
        bothFills(weight = 400) // Both filled and unfilled
    }

    materialSymbol("person") {
        allVariants(weight = SymbolWeight.W500) // All variants
    }

    materialSymbol("settings") {
        style(weight = 400, variant = SymbolVariant.OUTLINED)
        style(weight = 500, variant = SymbolVariant.ROUNDED, fill = SymbolFill.FILLED)
    }

    // External icons from MDI
    externalIcons(*listOf("abacus", "ab-testing").toTypedArray(), libraryName = "mdi") {
        urlTemplate = "https://esm.sh/@mdi/svg@latest/svg/{name}.svg"
    }

    // External icons with style variants
    externalIcons(*listOf("home", "search", "person", "settings", "arrow_back").toTypedArray(), 
                  libraryName = "official") {
        urlTemplate = "https://rawcdn.githack.com/google/material-design-icons/master/symbols/web/{name}/materialsymbolsrounded/{name}{fill}_24px.svg?min=1"
        styleParam("fill") {
            values("", "_fill1")  // unfilled, filled variants
        }
    }
}
```

## Getting Started

### Prerequisites

- **JDK 17** or higher
- **Android Studio** (for Android development)
- **Xcode** (for iOS development, macOS only)
- **Gradle 8.0+** (included via wrapper)

### Step 1: Generate Icons

Before building the app, generate the icons:

```bash
./gradlew generateSymbolKraftIcons
```

This will:
- Download SVG files from configured sources
- Convert them to Compose ImageVectors
- Generate Kotlin files in `composeApp/src/commonMain/kotlin/generated/symbols/`

### Step 2: Build and Run

#### Android

```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Or run directly on connected device/emulator
./gradlew :composeApp:installDebug
```

You can also open the project in Android Studio and run from there.

#### Desktop (JVM)

```bash
./gradlew :composeApp:run
```

#### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select a simulator or device
3. Click Run (⌘R)

Alternatively, from the terminal:
```bash
# Open in Xcode
open iosApp/iosApp.xcodeproj
```

## Platform-Specific Notes

### Android
- **Min SDK**: 24
- **Target SDK**: 35
- **Compile SDK**: 35

### iOS
- **Deployment Target**: iOS 15.0+
- **Requires**: Xcode 14.0 or later
- **Architecture**: arm64 (device), arm64 simulator

### Desktop
- **JVM Target**: 17
- **Supported OS**: Windows, macOS, Linux

## Development Tasks

### Common Gradle Tasks

```bash
# Generate icons
./gradlew generateSymbolKraftIcons

# Clean generated icons
./gradlew cleanSymbolKraftIcons

# Clean icon cache
./gradlew cleanSymbolKraftCache

# Validate configuration
./gradlew validateSymbolKraftConfig

# Clean everything
./gradlew clean

# Build all platforms
./gradlew build
```

### Troubleshooting

**Problem**: Icons not found after generation  
**Solution**: Run `./gradlew clean` then `./gradlew generateSymbolKraftIcons`

**Problem**: Build fails with missing imports  
**Solution**: Ensure icons are generated before building: `./gradlew generateSymbolKraftIcons`

**Problem**: iOS build fails  
**Solution**: Run `./gradlew clean` and regenerate the iOS framework

## Using Generated Icons

Generated icons can be used in Compose like this:

```kotlin
import com.ebisuzawa.symbolkraft.example.icons.materialsymbols.Icons
import com.ebisuzawa.symbolkraft.example.icons.materialsymbols.icons.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun MyScreen() {
    // Direct import
    Icon(
        imageVector = SearchW400Outlined,
        contentDescription = "Search"
    )

    // Via Icons accessor object
    Icon(
        imageVector = Icons.HomeW500Rounded,
        contentDescription = "Home"
    )

    // External icons
    Icon(
        imageVector = Icons.AbacusMdi,
        contentDescription = "Abacus"
    )
}
```

## Preview Support

The example enables preview generation with `generatePreview.set(true)`. You can view icon previews:

1. Open generated icon files in Android Studio/IntelliJ IDEA
2. Look for `@Preview` annotated functions
3. Click the "Preview" panel on the right side
4. View rendered icons directly in the IDE

Generated previews use `androidx.compose.ui.tooling.preview.Preview` and require
`org.jetbrains.compose.ui:ui-tooling-preview`.

## Learn More

- [SymbolKraft Documentation](../README.md)
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Material Symbols](https://fonts.google.com/icons)

## License

This example is part of the SymbolKraft project and is licensed under Apache 2.0.
