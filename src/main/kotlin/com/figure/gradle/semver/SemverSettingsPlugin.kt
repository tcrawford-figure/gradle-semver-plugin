package com.figure.gradle.semver

import com.figure.gradle.semver.internal.calculator.versionFactory
import com.figure.gradle.semver.internal.forTesting
import com.figure.gradle.semver.internal.modifier
import com.figure.gradle.semver.internal.overrideVersion
import com.figure.gradle.semver.internal.stage
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

private val log: Logger = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

class SemverSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val semverExtension = SemverExtension(settings)

        // Don't inline this. The `convention` lambda doesn't like to serialize the settings object and evaluate
        // the value later.
        val settingsDir = settings.rootDir

        val nextVersion = settings.providers.versionFactory(
            initialVersion = semverExtension.initialVersion,
            stage = settings.stage,
            modifier = settings.modifier,
            forTesting = settings.forTesting,
            overrideVersion = settings.overrideVersion,
            rootDir = semverExtension.rootProjectDir.convention { settingsDir }
        ).get()

        log.lifecycle("Found next version: $nextVersion")

        settings.gradle.beforeProject { project ->
            project.version = nextVersion
        }
    }
}
