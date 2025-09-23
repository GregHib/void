import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import java.io.File

/**
 * Gradle task which incrementally collects annotation info about classes inside a given directory.
 */
abstract class ScriptMetadataTask : DefaultTask() {

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:OutputFile
    abstract var scriptsFile: File

    init {
        description = "Analyzes Kotlin files and extracts annotation information"
        group = "metadata"
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val start = System.currentTimeMillis()

        val scriptsList: MutableList<String>
        if (!inputChanges.isIncremental) {
            // Clean output for non-incremental runs
            scriptsFile.delete()
            logger.info("Non-incremental run: analyzing all files")
            scriptsList = mutableListOf()
        } else {
            scriptsList = if (scriptsFile.exists()) scriptsFile.readLines().toMutableList() else mutableListOf()
        }

        val disposable = Disposer.newDisposable()
        val environment = createKotlinEnvironment(disposable)
        var additions = 0
        for (change in inputChanges.getFileChanges(inputDirectory)) {
            val file = change.file
            if (change.changeType == ChangeType.REMOVED) {
                val name = file.nameWithoutExtension
                removeName(scriptsList, name)
                continue
            }
            if (!file.isFile || !file.name.endsWith(".kt")) {
                continue
            }
            val localFile = environment.findLocalFile(file.path)
            if (localFile == null) {
                println("Local file not found: ${file.path}")
                continue
            }
            val psiFile: KtFile = PsiManager.getInstance(environment.project).findFile(localFile) as KtFile
            val classes = psiFile.collectDescendantsOfType<KtClass>()
            val packageName = psiFile.packageFqName.asString()
            if (change.changeType == ChangeType.MODIFIED) {
                for (name in classes.map { it.name }) {
                    if (!scriptsList.removeIf { it.endsWith("$packageName.$name") } && change.changeType == ChangeType.MODIFIED) {
                        removeName(scriptsList, name)
                    }
                }
            }
            if (change.changeType == ChangeType.MODIFIED || change.changeType == ChangeType.ADDED) {
                for (ktClass in classes) {
                    val className = ktClass.name ?: "Anonymous"
                    if (ktClass.annotationEntries.any { anno -> anno.shortName!!.asString() == "Script" }) {
                        val methods = ktClass.declarations.filterIsInstance<KtNamedFunction>().filter { it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
                        additions++
                        if (methods.isEmpty()) {
                            scriptsList.add("$packageName.$className")
                            continue
                        }
                        val signatures = methods.joinToString(separator = "|", postfix = "|") { method ->
                            val returnType = method.typeReference
                            "${method.name}(${method.valueParameters.joinToString(",") { param -> param.typeReference!!.getTypeText() }})${if (returnType == null) "" else ":${returnType.getTypeText()}"}"
                        }
                        scriptsList.add("$signatures$packageName.$className")
                    }
                }
            }
        }
        scriptsFile.writeText(scriptsList.joinToString("\n"))
        disposable.dispose()
        println("Metadata for $additions scripts collected in ${System.currentTimeMillis() - start} ms")
    }

    private fun removeName(scriptsList: MutableList<String>, name: String?) {
        if (scriptsList.count { it.endsWith(".$name") } > 1) {
            error("Deletion failed due to duplicate script names: ${scriptsList.filter { it.endsWith(".$name") }}. Please update scripts.txt or run `gradle cleanScriptMetadata`.")
        }
        scriptsList.removeIf { it.endsWith(".$name") }
    }

    private fun createKotlinEnvironment(disposable: Disposable): KotlinCoreEnvironment = KotlinCoreEnvironment.createForProduction(
        disposable,
        CompilerConfiguration.EMPTY,
        EnvironmentConfigFiles.JVM_CONFIG_FILES,
    )
}
