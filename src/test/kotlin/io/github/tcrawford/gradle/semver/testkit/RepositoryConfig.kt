package io.github.tcrawford.gradle.semver.testkit

import java.io.File

sealed class Action

class CheckoutAction : Action() {
    var branch: String = ""
}

class CommitAction : Action() {
    var message: String = ""
    var tag: String = ""
}

class RunScriptAction : Action() {
    lateinit var script: File
    lateinit var arguments: List<String>
}

class RepositoryConfig {
    lateinit var initialBranch: String
    val actions = mutableListOf<Action>()

    fun actions(config: Actions.() -> Unit) {
        val actionObject = Actions()
        actionObject.config()
        actions.addAll(actionObject.actions)
    }
}

class Actions {
    val actions = mutableListOf<Action>()

    fun checkout(config: CheckoutAction.() -> Unit) {
        val checkoutAction = CheckoutAction()
        checkoutAction.config()
        actions.add(checkoutAction)
    }

    fun checkout(branch: String) {
        val checkoutAction = CheckoutAction()
        checkoutAction.branch = branch
        actions.add(checkoutAction)
    }

    fun commit(config: CommitAction.() -> Unit) {
        val commitAction = CommitAction()
        commitAction.config()
        actions.add(commitAction)
    }

    fun commit(message: String = "Empty Commit", tag: String = "") {
        val commitAction = CommitAction()
        commitAction.message = message
        commitAction.tag = tag
        actions.add(commitAction)
    }

    fun runScript(config: RunScriptAction.() -> Unit) {
        val runScriptAction = RunScriptAction()
        runScriptAction.config()
        actions.add(runScriptAction)
    }

    fun runScript(script: File, vararg arguments: String) {
        val runScriptAction = RunScriptAction()
        runScriptAction.script = script
        runScriptAction.arguments = arguments.toList()
        actions.add(runScriptAction)
    }
}

fun repositoryConfig(config: RepositoryConfig.() -> Unit): RepositoryConfig {
    val repositoryConfig = RepositoryConfig()
    repositoryConfig.config()
    return repositoryConfig
}
