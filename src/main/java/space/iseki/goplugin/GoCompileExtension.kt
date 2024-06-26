package space.iseki.goplugin

import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.jvm.tasks.ProcessResources
import java.util.*

class GoCompileExtension(private val project: Project) {
    private val obj = project.objects

    inner class CompilationPerTargetConfig(
        val buildMode: BuildMode,
        val taskName: String,
        val inputSource: DirectoryProperty,
        val goTarget: GoTarget,
    ) {
        val buildTagsProperty: ListProperty<String> =
            obj.listProperty(String::class.java).convention(emptyList<String>())
        val environmentProperty: MapProperty<String, String> =
            obj.mapProperty(String::class.java, String::class.java).convention(GoCompilationTask.DEFAULT_ENVIRONMENT)
        val trimPathProperty: Property<Boolean> = obj.property(Boolean::class.java).convention(true)
        val packageProperty: Property<String> = obj.property(String::class.java).convention(".")
        val ldflagsProperty: Property<String> = obj.property(String::class.java).convention("-s -w -buildid=")
        val freeArgumentsProperty: ListProperty<String> =
            obj.listProperty(String::class.java).convention(emptyList<String>())
    }

    inner class CompilationConfiguration(internal val buildMode: BuildMode) {
        internal val targets = mutableMapOf<GoTarget, CompilationPerTargetConfig.() -> Unit>()
        fun goTargets(vararg targets: GoTarget, config: CompilationPerTargetConfig.() -> Unit = {}) {
            targets.forEach {
                this.targets[it] = config
            }
        }

        val inputSource: DirectoryProperty = obj.directoryProperty()
    }

    fun exec(name: String, configuration: CompilationConfiguration.() -> Unit) {
        val conf = CompilationConfiguration(BuildMode.Default)
        conf.configuration()
        extracted(conf, name, true)
    }

    fun dll(name: String, configuration: CompilationConfiguration.() -> Unit) {
        val conf = CompilationConfiguration(BuildMode.CShared)
        conf.configuration()
        extracted(conf, name, true)
    }

    private fun extracted(conf: CompilationConfiguration, name: String, mergeIntoResource: Boolean) {
        val logger = project.logger
        val tasks = mutableListOf<Provider<GoCompilationTask>>()
        for ((target, configBlock) in conf.targets) {
            val taskName = nameOf(name, target)
            val config = CompilationPerTargetConfig(
                buildMode = conf.buildMode,
                taskName = taskName,
                inputSource = conf.inputSource,
                goTarget = target,
            ).apply(configBlock)
            tasks += doRegister(taskName, name, config, target)
        }
        if (mergeIntoResource) {
            project.afterEvaluate {
                try {
                    project.tasks.getByName("processResources") {
                        if (it !is ProcessResources) {
                            logger.error("processResources is not a ProcessResources")
                            return@getByName
                        }
                        for (newTask in tasks) {
                            it.from(newTask.get().outputFileProperty)
                        }
                    }
                } catch (e: UnknownDomainObjectException) {
                    logger.warn(
                        "project processResources not found, the compiled binary will not be bundled into resource, {}",
                        e.message
                    )
                }
            }
        }
    }

    private fun nameOf(name: String, goTarget: GoTarget): String {
        val namePart = name.split('-').joinToString(separator = "") { it.capitalize() }
        return "compileGo$namePart${goTarget.goos.capitalize()}${goTarget.goarch.capitalize()}"
    }

    private fun String.capitalize() = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }

    private fun doRegister(
        name: String,
        binName: String,
        conf: CompilationPerTargetConfig,
        goTarget: GoTarget
    ): TaskProvider<GoCompilationTask> {
        val newTask = project.tasks.register(name, GoCompilationTask::class.java) {
            it.group = "go"
            it.description = "Compile go sources"
            it.goTargetProperty.set(goTarget)
            it.inputSourceProperty.set(conf.inputSource)
            it.buildModeProperty.set(conf.buildMode)
            it.buildTagsProperty.set(conf.buildTagsProperty)
            it.environmentProperty.set(conf.environmentProperty)
            it.trimPathProperty.set(conf.trimPathProperty)
            it.packageProperty.set(conf.packageProperty)
            it.ldflagsProperty.set(conf.ldflagsProperty)
            it.freeArgumentsProperty.set(conf.freeArgumentsProperty)
            val suffix = when (conf.buildMode) {
                BuildMode.Default -> when (goTarget.goos) {
                    "windows" -> ".exe"
                    else -> ""
                }

                BuildMode.CShared -> when (goTarget.goos) {
                    "windows" -> ".dll"
                    "darwin" -> ".dylib"
                    else -> ".so"
                }
            }
            val filename = "$binName-${goTarget.goos}-${goTarget.goarch}$suffix"
            it.outputFileProperty.set(project.layout.buildDirectory.file("go/$filename"))
        }
        return newTask
    }
}

