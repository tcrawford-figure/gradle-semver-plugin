package com.figure.gradle.semver.internal.command

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

internal object Open {
    operator fun invoke(): Git = Git(
        FileRepositoryBuilder()
            .readEnvironment()
            .findGitDir()
            .build()
    )

    operator fun invoke(rootDir: File): Git =
        Git.open(rootDir)
}
