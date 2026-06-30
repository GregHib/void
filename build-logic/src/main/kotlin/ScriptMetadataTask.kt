import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
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
        var scripts = 0
        for (change in inputChanges.getFileChanges(inputDirectory)) {
            val file = change.file
            if (change.changeType == ChangeType.REMOVED) {
                removeName(lines, file.nameWithoutExtension)
                continue
            }
            if (!file.isFile || !file.name.endsWith(".kt")) {
                continue
            }
            val (packageName, classes) = parseScriptClasses(file)
            if (change.changeType == ChangeType.MODIFIED) {
                for (name in classes) {
                    if (!lines.removeIf { it.endsWith("$packageName.$name") }) {
                        removeName(lines, name)
                    }
                }
            }
            if (change.changeType == ChangeType.MODIFIED || change.changeType == ChangeType.ADDED) {
                for (name in classes) {
                    lines.add("$packageName.$name")
                    scripts++
                }
            }
        }
        scriptsFile.writeText(lines.joinToString("\n"))
        println("Metadata for $scripts scripts collected in ${System.currentTimeMillis() - start} ms")
    }

    private fun parseScriptClasses(file: File): Pair<String, List<String>> {
        val text = file.readText()
        val packageName = PACKAGE_REGEX.find(text)?.groupValues?.get(1) ?: ""
        val classes = CLASS_REGEX.findAll(text)
            .filter { it.groupValues[2].split(",").map(String::trim).contains("Script") }
            .map { it.groupValues[1] }
            .toList()
        return packageName to classes
    }

    private fun removeName(scriptsList: MutableList<String>, name: String?) {
        if (scriptsList.filter { it.endsWith(".$name") }.map { it.split("|").last() }.distinct().count() > 1) {
            error("Deletion failed due to duplicate script names: ${scriptsList.filter { it.endsWith(".$name") }.map { it.split("|").last() }.distinct()}. Please update scripts.txt or run `gradle cleanScriptMetadata`.")
        }
        scriptsList.removeIf { it.endsWith(".$name") }
    }

    companion object {
        private val PACKAGE_REGEX = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
        // Matches class declarations including multi-line constructor params, then captures supertypes after ':'
        private val CLASS_REGEX = Regex(
            """(?:^|\n)\s*(?:(?:abstract|open|data|sealed|inner)\s+)*class\s+(\w+)\s*(?:\([^)]*\)\s*)?:\s*([^{]+)\{""",
            RegexOption.DOT_MATCHES_ALL,
        )
    }

}
