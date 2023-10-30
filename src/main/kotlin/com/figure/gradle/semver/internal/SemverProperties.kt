package com.figure.gradle.semver.internal

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File
import java.util.Properties

internal val Project.gradlePropertiesFile: File
    get() = projectDir.resolve("gradle.properties")

internal val Project.gradleProperties: Properties
    get() = Properties().apply { load(gradlePropertiesFile.inputStream()) }

internal val Project.modifierProperty: Provider<Modifier>
    get() = semverProperty(SemverProperty.Modifier).map { Modifier.fromValue(it) }.orElse(Modifier.Auto)

internal val Project.stageProperty: Provider<Stage>
    get() = semverProperty(SemverProperty.Stage).map { Stage.fromValue(it) }.orElse(Stage.Auto)

internal val Project.tagPrefixProperty: Provider<String>
    get() = semverProperty(SemverProperty.TagPrefix).orElse("v")

internal val Project.overrideVersion: Provider<Version>
    get() = semverProperty(SemverProperty.OverrideVersion).map { it.toVersion() }

internal val Project.forTesting: Provider<Boolean>
    get() = semverProperty(SemverProperty.ForTesting).map { it.toBoolean() }.orElse(false)

internal enum class SemverProperty(val property: String) {
    Stage("semver.stage"),
    Modifier("semver.modifier"),
    TagPrefix("semver.tagPrefix"),
    OverrideVersion("semver.overrideVersion"),

    ForTesting("semver.forTesting"),
}

internal enum class Modifier(val value: String) {
    Major("major"),
    Minor("minor"),
    Patch("patch"),
    Auto("auto");

    companion object {
        fun fromValue(value: String): Modifier =
            values().find { it.value == value } ?: error("Invalid modifier provided: $value")
    }
}

// In order from lowest to highest priority
internal enum class Stage(val value: String) {
    Dev("dev"),
    Alpha("alpha"),
    Beta("beta"),
    Preview("rc"),
    Snapshot("snapshot"),
    Final("final"),
    GA("ga"),
    Release("release"),
    Auto("auto");

    companion object {
        fun fromValue(value: String): Stage =
            values().find { it.value == value } ?: error("Invalid stage provided: $value")
    }
}

private fun Project.semverProperty(semverProperty: SemverProperty): Provider<String> =
    when {
        gradle.startParameter.projectProperties[semverProperty.property] != null -> {
            provider { gradle.startParameter.projectProperties[semverProperty.property] }
        }

        gradlePropertiesFile.exists() -> {
            provider { gradleProperties.getProperty(semverProperty.property) }
        }

        else -> {
            providers.gradleProperty(semverProperty.property)
        }
    }
