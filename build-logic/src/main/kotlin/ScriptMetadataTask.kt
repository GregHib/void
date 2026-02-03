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
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import java.io.File

/**
 * Gradle task which incrementally collects script classes inside a given directory.
 */
abstract class ScriptMetadataTask : DefaultTask() {

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:Internal
    abstract var resourceDirectory: File

    @get:OutputFile
    abstract var scriptsFile: File

    init {
        description = "Analyzes Kotlin files and extracts list of classes which extend Script interface"
        group = "metadata"
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val start = System.currentTimeMillis()
        val lines: MutableList<String>
        if (!inputChanges.isIncremental) {
            // Clean output for non-incremental runs
            scriptsFile.delete()
            logger.info("Non-incremental run: analyzing all files")
            lines = mutableListOf()
        } else {
            lines = if (scriptsFile.exists()) scriptsFile.readLines().toMutableList() else mutableListOf()
        }
        val disposable = Disposer.newDisposable()
        val environment = createKotlinEnvironment(disposable)
        var scripts = 0
        val instance = PsiManager.getInstance(environment.project)
        for (change in inputChanges.getFileChanges(inputDirectory)) {
            val file = change.file
            if (change.changeType == ChangeType.REMOVED) {
                removeName(lines, file.nameWithoutExtension)
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
            val psiFile: KtFile = instance.findFile(localFile) as KtFile
            val classes = psiFile
                .collectDescendantsOfType<KtClass>()
                .filter { clazz -> clazz.superTypeListEntries.any { type -> type.text == "Script" } }
            val packageName = psiFile.packageFqName.asString()
            if (change.changeType == ChangeType.MODIFIED) {
                for (name in classes.map { it.name }) {
                    if (!lines.removeIf { it.endsWith("$packageName.$name") }) {
                        removeName(lines, name)
                    }
                }
            }
            if (change.changeType == ChangeType.MODIFIED || change.changeType == ChangeType.ADDED) {
                for (ktClass in classes) {
                    val className = ktClass.name ?: "Anonymous"
                    lines.add("$packageName.$className")
                    scripts++
                }
            }
        }
        scriptsFile.writeText(lines.joinToString("\n"))
        disposable.dispose()
        println("Metadata for $scripts scripts collected in ${System.currentTimeMillis() - start} ms")
    }

    private fun removeName(scriptsList: MutableList<String>, name: String?) {
        if (scriptsList.filter { it.endsWith(".$name") }.map { it.split("|").last() }.distinct().count() > 1) {
            error("Deletion failed due to duplicate script names: ${scriptsList.filter { it.endsWith(".$name") }.map { it.split("|").last() }.distinct()}. Please update scripts.txt or run `gradle cleanScriptMetadata`.")
        }
        scriptsList.removeIf { it.endsWith(".$name") }
    }

    private fun createKotlinEnvironment(disposable: Disposable): KotlinCoreEnvironment = KotlinCoreEnvironment.createForProduction(
        disposable,
        CompilerConfiguration.EMPTY,
        EnvironmentConfigFiles.JVM_CONFIG_FILES,
    )

}
