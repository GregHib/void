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
 * Collects:
 *  - @Script annotations for invocation
 *  - Overridden methods
 *  - Annotations on methods and it's data
 *  - Processes annotation wildcards
 */
abstract class ScriptMetadataTask : DefaultTask() {

    private enum class WildcardType {
        NpcId,
        InterfaceId,
        ComponentId,
        ObjectId,
        ItemId,
        NpcOption,
        InterfaceOption,
        FloorItemOption,
        ObjectOption,
        ItemOption,
    }

    // List of annotation names and their parameters
    private val annotations: Map<String, List<Pair<String, WildcardType>>> = mapOf()

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:Internal
    abstract var dataDirectory: File

    @get:Internal
    abstract var resourceDirectory: File

    @get:OutputFile
    abstract var scriptsFile: File

    init {
        description = "Analyzes Kotlin files and extracts annotation information"
        group = "metadata"
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val start = System.currentTimeMillis()

        val npcIds = mutableSetOf<String>()
        val itemIds = mutableSetOf<String>()
        val objectIds = mutableSetOf<String>()
        val interfaceIds = mutableSetOf<String>()
        val componentIds = mutableSetOf<String>()
        collectIds(npcIds, itemIds, objectIds, interfaceIds, componentIds)
        val options = System.currentTimeMillis()
        val npcOptions = loadOptions("npc-options")
        val itemOptions = loadOptions("item-options")
        val floorItemOptions = loadOptions("floor-item-options")
        val objectOptions = loadOptions("object-options")
        val interfaceOptions = loadOptions("interface-options")
        println("Loaded ${npcOptions.size} npc, ${itemOptions.size} item, ${floorItemOptions.size} floor item, ${objectOptions.size} object, ${interfaceOptions.size} interface options in ${System.currentTimeMillis() - options}ms")

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
        var methodCount = 0
        var annotationCount = 0
        val instance = PsiManager.getInstance(environment.project)
        val scriptClasses = mutableListOf<Pair<KtClass, String>>()
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

            val classes = psiFile.collectDescendantsOfType<KtClass>()
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
                    if (ktClass.annotationEntries.any { anno -> anno.shortName!!.asString() == "Script" }) {
                        scriptClasses.add(ktClass to "$packageName.$className")
                    }
                }
            }
        }

        for ((ktClass, packagePath) in scriptClasses) {
            val methods = ktClass.declarations.filterIsInstance<KtNamedFunction>().filter { it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
            scripts++
            if (methods.isEmpty()) {
                lines.add(packagePath)
                continue
            }
            for (method in methods) {
                methodCount++
                val returnType = method.typeReference
                val signature = "${method.name}(${method.valueParameters.joinToString(",") { param -> param.typeReference!!.getTypeText() }})${if (returnType == null) "" else ":${returnType.getTypeText()}"}"
                val entries = method.annotationEntries
                if (entries.isEmpty()) {
                    lines.add("${signature}|$packagePath")
                    continue
                }
                for (annotation in entries) {
                    val annotationName = annotation.shortName?.asString() ?: ""
                    val info = annotations.get(annotationName) ?: error("Annotation ${annotationName} metadata not found. Make sure your annotation is registered in ScriptMetadataTask.kt")
                    val params = Array<MutableList<String>>(info.size) { mutableListOf() }
                    // Resolve annotation field names
                    var index = 0
                    for (arg in annotation.valueArguments) {
                        val name = arg.getArgumentName()?.asName?.asString()
                        val value = arg.getArgumentExpression()?.text?.trim('"') ?: ""
                        var idx = if (name != null) info.indexOfFirst { it.first == name } else index++
                        params[idx].add(value)
                    }
                    for (i in info.indices) {
                        val value = params[i].first()
                        // Expand wildcards into matches
                        if (value.contains("*") || value.contains("#")) {
                            val set = when (info[i].second) {
                                WildcardType.NpcId -> npcIds
                                WildcardType.InterfaceId -> interfaceIds
                                WildcardType.ComponentId -> componentIds
                                WildcardType.ObjectId -> objectIds
                                WildcardType.ItemId -> itemIds
                                WildcardType.NpcOption -> npcOptions
                                WildcardType.InterfaceOption -> interfaceOptions
                                WildcardType.FloorItemOption -> floorItemOptions
                                WildcardType.ObjectOption -> objectOptions
                                WildcardType.ItemOption -> itemOptions
                            }
                            val matches = set.filter { wildcardEquals(value, it) }
                            if (matches.isEmpty()) {
                                error("No matches for wildcard '${value}' in ${packagePath} ${annotation.text}")
                            }
                            params[i].removeAt(0)
                            params[i].addAll(matches)
                        }
                    }
                    generateCombinations(params) { args ->
                        annotationCount++
                        lines.add("@${annotation.shortName}|${args.joinToString(":")}|$signature|$packagePath")
                    }
                }
            }
        }
        scriptsFile.writeText(lines.joinToString("\n"))
        disposable.dispose()
        println("Metadata for $scripts scripts, $methodCount methods and $annotationCount annotations collected in ${System.currentTimeMillis() - start} ms")
    }

    private fun generateCombinations(arrays: Array<MutableList<String>>, index: Int = 0, current: MutableList<String> = mutableListOf(), call: (List<String>) -> Unit) {
        if (index == arrays.size) {
            call.invoke(current)
            return
        }
        val currentArray = arrays[index]
        for (element in currentArray) {
            current.add(element)
            generateCombinations(arrays, index + 1, current, call)
            current.removeAt(current.size - 1)
        }
    }

    private fun loadOptions(type: String): Set<String> {
        return ScriptMetadataTask::class.java.getResource("$type.txt")!!.readText().lines().toSet()
    }

    private fun collectIds(
        npcIds: MutableSet<String>,
        itemIds: MutableSet<String>,
        objectIds: MutableSet<String>,
        interfaceIds: MutableSet<String>,
        componentIds: MutableSet<String>,
    ) {
        val start = System.currentTimeMillis()
        for (file in dataDirectory.walkTopDown()) {
            if (!file.isFile) {
                continue
            }
            if (file.name.endsWith(".npcs.toml")) {
                for (line in file.readLines()) {
                    if (line.startsWith('[')) {
                        npcIds.add(line.substringBefore(']').trim('['))
                    }
                }
            } else if (file.name.endsWith(".items.toml")) {
                for (line in file.readLines()) {
                    if (line.startsWith('[')) {
                        itemIds.add(line.substringBefore(']').trim('['))
                    }
                }
            } else if (file.name.endsWith(".objs.toml")) {
                for (line in file.readLines()) {
                    if (line.startsWith('[')) {
                        objectIds.add(line.substringBefore(']').trim('['))
                    }
                }
            } else if (file.name.endsWith(".ifaces.toml")) {
                for (line in file.readLines()) {
                    if (line.startsWith('[')) {
                        val key = line.substringBefore(']').trim('[')
                        if (key.contains(".")) {
                            componentIds.add(key.substringAfter('.'))
                        } else {
                            interfaceIds.add(key)
                        }
                    }
                }
            }
        }
        println("Collected ${npcIds.size} npcs, ${itemIds.size} items, ${objectIds.size} objects, ${interfaceIds.size} interfaces, ${componentIds.size} components in ${System.currentTimeMillis() - start}ms")
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


    private fun wildcardEquals(wildcard: String, other: String): Boolean {
        if (wildcard == "*") {
            return true
        }
        var wildIndex = 0
        var otherIndex = 0
        var starIndex = -1
        var matchIndex = -1

        while (otherIndex < other.length) {
            when {
                wildIndex < wildcard.length && (wildcard[wildIndex] == '#' && other[otherIndex].isDigit()) -> {
                    wildIndex++
                    otherIndex++
                }
                wildIndex < wildcard.length && wildcard[wildIndex] == '*' -> {
                    starIndex = wildIndex
                    matchIndex = otherIndex
                    wildIndex++
                }
                wildIndex < wildcard.length && wildcard[wildIndex] == other[otherIndex] -> {
                    wildIndex++
                    otherIndex++
                }
                starIndex != -1 -> {
                    wildIndex = starIndex + 1
                    matchIndex++
                    otherIndex = matchIndex
                }
                else -> return false
            }
        }

        while (wildIndex < wildcard.length && wildcard[wildIndex] == '*') {
            wildIndex++
        }

        return wildIndex == wildcard.length && otherIndex == other.length
    }

}
