package com.figure.gradle.semver.internal.calculator

import com.figure.gradle.semver.SemverExtension
import com.figure.gradle.semver.internal.Stage
import com.figure.gradle.semver.internal.command.KGit
import com.figure.gradle.semver.internal.forTesting
import com.figure.gradle.semver.internal.modifierProperty
import com.figure.gradle.semver.internal.stageProperty
import com.figure.gradle.semver.internal.tagPrefixProperty
import com.figure.gradle.semver.log
import org.gradle.api.Project

internal object VersionFactory {
    fun nextVersion(semverExtension: SemverExtension, rootProject: Project): String {
        val kgit = KGit(directory = rootProject.rootDir)
        val forTesting = rootProject.forTesting.get()

        val stage = rootProject.stageProperty.get()
        val modifier = rootProject.modifierProperty.get()
        val tagPrefix = rootProject.tagPrefixProperty.get()

        val currentBranch = kgit.branch.currentRef(forTesting).name

        log.lifecycle("Stage: $stage")
        log.lifecycle("Modifier: $modifier")
        log.lifecycle("Tag prefix: $tagPrefix")
        log.lifecycle("Current Branch: $currentBranch")
        log.lifecycle("Main branch: ${kgit.branches.mainBranch.name}")
        log.lifecycle("Development branch: ${kgit.branches.developmentBranch.name}")

        val version = when {
            // Stage is the highest priority, so it can be used from any branch
            stage != Stage.Auto -> {
                val stageBasedVersionCalculator = StageBasedVersionCalculator(kgit)
                stageBasedVersionCalculator.calculate(semverExtension, rootProject)
            }
            kgit.branch.isOnMainBranch(forTesting) -> {
                val stableVersionCalculator = StableVersionCalculator(kgit)
                stableVersionCalculator.calculate(semverExtension, rootProject)
            }
            kgit.branch.isOnDevelopmentBranch(forTesting) -> {
                val branchBasedVersionCalculator = BranchBasedVersionCalculator(kgit)
                branchBasedVersionCalculator.calculate(semverExtension, rootProject)
            }
            else -> error("Could not determine versioning strategy")
        }

        return version
    }
}
