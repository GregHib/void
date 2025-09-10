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
import org.jetbrains.kotlin.psi.*
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
        for (change in inputChanges.getFileChanges(inputDirectory)) {
            val file = change.file
            if (!file.isFile || !file.name.endsWith(".kt")) {
                continue
            }
            val localFile = environment.findLocalFile(file.path)!!
            val psiFile: KtFile = PsiManager.getInstance(environment.project).findFile(localFile) as KtFile
            val classes = psiFile.collectDescendantsOfType<KtClass>()
            val packageName = psiFile.packageFqName.asString()
            if (change.changeType == ChangeType.MODIFIED || change.changeType == ChangeType.REMOVED) {
                for (name in classes.map { it.name }) {
                    scriptsList.removeIf { it == "$packageName.$name" }
                }
            }
            if (change.changeType == ChangeType.MODIFIED || change.changeType == ChangeType.ADDED) {
                for (ktClass in classes) {
                    val className = ktClass.name ?: "Anonymous"
                    if (ktClass.annotationEntries.any { anno -> anno.shortName!!.asString() == "Script" }) {
                        scriptsList.add("$packageName.$className")
                    }
                }
            }
        }
        scriptsFile.writeText(scriptsList.joinToString("\n"))
        disposable.dispose()
        println("Metadata collected in ${System.currentTimeMillis() - start} ms")
    }

    private fun createKotlinEnvironment(disposable: Disposable): KotlinCoreEnvironment = KotlinCoreEnvironment.createForProduction(
        disposable,
        CompilerConfiguration.EMPTY,
        EnvironmentConfigFiles.JVM_CONFIG_FILES,
    )
}
