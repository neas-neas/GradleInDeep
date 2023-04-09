package kts

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler.compileBunchOfSources
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmSdkRoots
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer.dispose
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer.newDisposable
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.config.JVMConfigurationKeys.OUTPUT_DIRECTORY
import org.jetbrains.kotlin.config.JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY
import org.jetbrains.kotlin.scripting.compiler.plugin.ScriptingCompilerConfigurationComponentRegistrar
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys.SCRIPT_DEFINITIONS
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.configurationDependencies
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.JvmGetScriptingClass

val classPath = listOf(
    File("./libs/kotlin-stdlib-1.8.10.jar"),
    File("./build/libs/GradleInDeep.jar")
)

fun main() {
    val outputDirectory = File(".")
    val scriptFile = File(".", "hello.kts")
    val scriptFiles = mutableListOf(scriptFile.absolutePath)
    compileKotlinScriptModuleTo(
        outputDirectory, "buildscript", scriptFiles, scriptDefinitionFromTemplate(
            DefaultKotlinScript::class,
            listOf(),
            classPath = classPath
        ), classPath
    )

    // run script
    val url = outputDirectory.toURI().toURL()
    val urls = arrayOf(url)
    val classloader = URLClassLoader(urls)
    val cls = classloader.loadClass("Hello")
    cls.getDeclaredConstructor().newInstance()
}

// scriptDefinition
fun scriptDefinitionFromTemplate(
    template: KClass<out Any>,
    implicitImports: List<String>,
    implicitReceiver: KClass<*>? = null,
    injectedProperties: Map<String, KotlinType> = mapOf(),
    classPath: List<File> = listOf(),
): ScriptDefinition {
    val hostConfiguration = ScriptingHostConfiguration {
        getScriptingClass(JvmGetScriptingClass())
        configurationDependencies(JvmDependency(classPath))
    }
    return ScriptDefinition.FromConfigurations(
        hostConfiguration = hostConfiguration,
        compilationConfiguration = ScriptCompilationConfiguration {
            baseClass(template)
            defaultImports(implicitImports)
            hostConfiguration(hostConfiguration)
            implicitReceiver?.let {
                implicitReceivers(it)
            }
            providedProperties(injectedProperties)
        },
        evaluationConfiguration = null
    )
}

private
fun compileKotlinScriptModuleTo(
    outputDirectory: File,
    moduleName: String,
    scriptFiles: Collection<String>,
    scriptDef: ScriptDefinition,
    classPath: Iterable<File>,
) {
    withRootDisposable {
        val configuration = compilerConfigurationFor().apply {
            put(RETAIN_OUTPUT_IN_MEMORY, false)
            put(OUTPUT_DIRECTORY, outputDirectory)
            put(CommonConfigurationKeys.MODULE_NAME, moduleName)
            addScriptingCompilerComponents()
            addScriptDefinition(scriptDef)
            scriptFiles.forEach { addKotlinSourceRoot(it) }
            classPath.forEach { addJvmClasspathRoot(it) }
        }

        val environment = kotlinCoreEnvironmentFor(configuration)
        compileBunchOfSources(environment)
    }
}

private
fun Disposable.kotlinCoreEnvironmentFor(configuration: CompilerConfiguration): KotlinCoreEnvironment {
    org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback()
    return KotlinCoreEnvironment.createForProduction(
        this,
        configuration,
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    )
}

private
fun CompilerConfiguration.addScriptDefinition(scriptDef: ScriptDefinition) {
    add(SCRIPT_DEFINITIONS, scriptDef)
}

@OptIn(ExperimentalCompilerApi::class)
private
fun CompilerConfiguration.addScriptingCompilerComponents() {
    add(
        ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS,
        ScriptingCompilerConfigurationComponentRegistrar()
    )
}

private
fun compilerConfigurationFor(): CompilerConfiguration =
    CompilerConfiguration().apply {
        put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, TestMessageCollector)
        put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_11)
        put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")))
        put(JVMConfigurationKeys.IR, true)
        put(JVMConfigurationKeys.SAM_CONVERSIONS, JvmClosureGenerationScheme.CLASS)
        addJvmSdkRoots(PathUtil.getJdkClassesRootsFromCurrentJre())
        put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, gradleKotlinDslLanguageVersionSettings)
        put(CommonConfigurationKeys.ALLOW_ANY_SCRIPTS_IN_SOURCE_ROOTS, true)
    }

private object TestMessageCollector : MessageCollector {
    override fun clear() = Unit
    override fun hasErrors(): Boolean = false
    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        println("$severity: $message")
    }
}

private val gradleKotlinDslLanguageVersionSettings = LanguageVersionSettingsImpl(
    languageVersion = LanguageVersion.KOTLIN_1_8,
    apiVersion = ApiVersion.KOTLIN_1_8,
    analysisFlags = mapOf(
        AnalysisFlags.skipMetadataVersionCheck to true,
        JvmAnalysisFlags.jvmDefaultMode to JvmDefaultMode.ENABLE,
    ),
    specificFeatures = mapOf(
        LanguageFeature.DisableCompatibilityModeForNewInference to LanguageFeature.State.ENABLED,
        LanguageFeature.TypeEnhancementImprovementsInStrictMode to LanguageFeature.State.DISABLED,
    )
)

private inline fun <T> withRootDisposable(action: Disposable.() -> T): T {
    val rootDisposable = newDisposable()
    try {
        return action(rootDisposable)
    } finally {
        dispose(rootDisposable)
    }
}