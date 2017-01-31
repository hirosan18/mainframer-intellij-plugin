package com.elpassion.intelijidea

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.project.Project


class MFRemoveBeforeTaskProjectComponent(private val project: Project) : AbstractProjectComponent(project) {

    override fun projectOpened() {
        val runManagerEx = RunManagerEx.getInstanceEx(project)
        val mfTaskProvider = BeforeRunTaskProvider.getProvider(project, MFBeforeRunTaskProvider.ID)!!

        val existingConfigurations = runManagerEx.getExistingConfigurations()
        val templateConfigurations = runManagerEx.getTemplateConfigurations()
        val configurationTypes = existingConfigurations.values + templateConfigurations.values
        configurationTypes.forEach {
            val task = mfTaskProvider.createTask(it.configuration)
            if (task != null) {
                task.isEnabled = true
                runManagerEx.setBeforeRunTasks(it.configuration, listOf<BeforeRunTask<*>>(task), false)
            }
        }
    }

    private fun RunManagerEx.getExistingConfigurations(): Map<String, RunnerAndConfigurationSettings> = getFieldByReflection("myConfigurations")

    private fun RunManagerEx.getTemplateConfigurations(): Map<String, RunnerAndConfigurationSettings> = getFieldByReflection("myTemplateConfigurationsMap")
}

//TODO: remove usage of reflection
private fun <T> Any.getFieldByReflection(fieldName: String): T {
    val declaredField = this.javaClass.getDeclaredField(fieldName)
    declaredField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return declaredField.get(this) as T
}