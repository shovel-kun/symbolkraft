import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.ktfmt)
    `java-gradle-plugin`
    signing
}

group = "com.ebisuzawa"

version = "0.3.4"

kotlin { jvmToolchain(17) }

ktfmt { kotlinLangStyle() }

// Configure Kotlin compiler options
tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers"))
    }
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // HTTP Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // SVG to Compose (with automatic transitive dependency resolution)
    implementation(libs.svg.to.compose)

    // Gradle API
    compileOnly(libs.gradle.api)
    compileOnly(libs.kotlin.gradle.plugin)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(gradleTestKit())
}

// Configure the Gradle plugin marker published to Maven Central.
gradlePlugin {
    plugins {
        create("symbolkraft") {
            id = "com.ebisuzawa.symbolkraft"
            implementationClass = "com.ebisuzawa.symbolkraft.plugin.SymbolKraftPlugin"
        }
    }
}

// Configure Vanniktech Maven Publish
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(group.toString(), "symbolkraft", version.toString())

    pom {
        name.set("SymbolKraft")
        description.set(
            "Generate icons on-demand from multiple libraries (Material Symbols, Bootstrap Icons, etc.) for Compose Multiplatform with smart caching."
        )
        inceptionYear.set("2025")
        url.set("https://github.com/shovel-kun/SymbolKraft")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("shovel-kun")
                name.set("shovel-kun")
                url.set("https://github.com/shovel-kun")
                email.set("ebisuzawakurumi@proton.me")
            }
        }

        scm {
            url.set("https://github.com/shovel-kun/SymbolKraft")
            connection.set("scm:git:git://github.com/shovel-kun/SymbolKraft.git")
            developerConnection.set("scm:git:ssh://git@github.com/shovel-kun/SymbolKraft.git")
        }
    }
}

signing {
    val signingKey = project.findProperty("signingKey") as String? ?: System.getenv("SIGNING_KEY")
    val signingPassword =
        project.findProperty("signingPassword") as String? ?: System.getenv("SIGNING_PASSWORD")

    // Always set isRequired to false
    isRequired = false

    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)

        // Configure signing after all publications are created
        afterEvaluate { sign(publishing.publications) }
    }
}

// Configure test framework
tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    addTestListener(
        object : org.gradle.api.tasks.testing.TestListener {
            override fun beforeSuite(suite: org.gradle.api.tasks.testing.TestDescriptor) = Unit

            override fun beforeTest(test: org.gradle.api.tasks.testing.TestDescriptor) = Unit

            override fun afterTest(
                test: org.gradle.api.tasks.testing.TestDescriptor,
                result: org.gradle.api.tasks.testing.TestResult,
            ) = Unit

            override fun afterSuite(
                suite: org.gradle.api.tasks.testing.TestDescriptor,
                result: org.gradle.api.tasks.testing.TestResult,
            ) {
                if (suite.parent == null) {
                    println(
                        "\nTest Summary: ${result.resultType} | Total: ${result.testCount}, " +
                            "Passed: ${result.successfulTestCount}, Failed: ${result.failedTestCount}, " +
                            "Skipped: ${result.skippedTestCount}"
                    )
                }
            }
        }
    )
}

// Generate sources and javadoc JARs
java { withSourcesJar() }

tasks.named("check") { dependsOn("ktfmtCheck") }

tasks.withType<Jar>().configureEach { from(listOf("LICENSE", "NOTICE")) { into("META-INF") } }

tasks.withType<GenerateModuleMetadata>().configureEach { dependsOn("dokkaJavadocJar") }

// Configure JAR manifest
tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "shovel-kun",
            "Built-By" to System.getProperty("user.name"),
            "Built-JDK" to System.getProperty("java.version"),
            "Built-Gradle" to gradle.gradleVersion,
        )
    }
}
