package io.github.tcrawford.gradle.semver

import io.github.tcrawford.gradle.semver.internal.calculator.VersionFactoryContext
import io.github.tcrawford.gradle.semver.internal.calculator.versionFactory
import io.github.tcrawford.gradle.semver.internal.extensions.lifecycle
import io.github.tcrawford.gradle.semver.internal.forTesting
import io.github.tcrawford.gradle.semver.internal.modifier
import io.github.tcrawford.gradle.semver.internal.overrideVersion
import io.github.tcrawford.gradle.semver.internal.stage
import io.github.tcrawford.gradle.semver.internal.tagPrefix
import io.github.tcrawford.gradle.semver.internal.writer.writeVersionToPropertiesFile
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

private val log: Logger = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

class SemverSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val semverExtension = SemverExtension(settings)

        val versionFactoryContext = VersionFactoryContext(
            initialVersion = semverExtension.initialVersion.get(),
            stage = settings.stage.get(),
            modifier = settings.modifier.get(),
            forTesting = settings.forTesting.get(),
            overrideVersion = settings.overrideVersion.orNull,
            rootDir = semverExtension.rootProjectDir.getOrElse { settings.rootDir }.asFile,
            mainBranch = semverExtension.mainBranch.orNull,
            developmentBranch = semverExtension.developmentBranch.orNull
        )

        val nextVersion = settings.providers.versionFactory(versionFactoryContext).get()

        // TODO: Log this at the end of the build
        log.lifecycle { nextVersion }

        settings.writeVersionToPropertiesFile(nextVersion, settings.tagPrefix.get())

        settings.gradle.beforeProject { project ->
            project.version = nextVersion
        }
    }
}
