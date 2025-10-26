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
import org.jetbrains.kotlin.psi.KtAnnotationEntry
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

    private sealed class WildcardType {
        data object None : WildcardType()
        sealed class Dynamic : WildcardType() {
            abstract fun type(context: Context, params: List<String>): Set<String>?
        }
        data class DynamicId(val paramIndex: Int) : Dynamic() {
            override fun type(context: Context, params: List<String>) = when (params[paramIndex]) {
                "NPC" -> context.npcIds
                "GameObject" -> context.objectIds
                "FloorItem" -> context.itemIds
                else -> null
            }
        }
        data object NpcId : WildcardType()
        data object InterfaceId : WildcardType()
        data object ComponentId : WildcardType()
        data object InterfaceComponentId : WildcardType()
        data object ObjectId : WildcardType()
        data object ItemId : WildcardType()
        data object VariableId : WildcardType()
        data class DynamicOption(val paramIndex: Int) : Dynamic() {
            override fun type(context: Context, params: List<String>) = when (params[paramIndex]) {
                "NPC" -> context.npcOptions
                "GameObject" -> context.objectOptions
                "FloorItem" -> context.itemOptions
                else -> null
            }
        }
        data object NpcOption : WildcardType()
        data object InterfaceOption : WildcardType()
        data object FloorItemOption : WildcardType()
        data object ObjectOption : WildcardType()
        data object ItemOption : WildcardType()
    }

    // List of annotation names and their parameters
    private val annotations: Map<String, List<Pair<String, WildcardType>>> = mapOf(
        "Id" to listOf("id" to WildcardType.DynamicId(0)),
        "SkillId" to listOf("skill" to WildcardType.None, "id" to WildcardType.DynamicId(0)),
        "Variable" to listOf("key" to WildcardType.VariableId, "id" to WildcardType.DynamicId(0)),
        "Operate" to listOf("option" to WildcardType.DynamicOption(0), "id" to WildcardType.DynamicId(1)),
        "Approach" to listOf("option" to WildcardType.DynamicOption(0), "id" to WildcardType.DynamicId(1)),
        "NoDelay" to emptyList(),
        "Timer" to listOf("id" to WildcardType.None),
        "ItemOn" to listOf("item" to WildcardType.ItemId, "on" to WildcardType.DynamicId(4)),
        "UseOn" to listOf("id" to WildcardType.InterfaceComponentId, "on" to WildcardType.DynamicId(4)),
    )

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

        val context = loadContext()
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
                val parameters = method.valueParameters.joinToString(",") { param -> param.typeReference!!.getTypeText() }
                val extension = method.receiverTypeReference
                val signature = "${if (extension != null) "${extension.text}." else ""}${method.name}(${parameters})${if (returnType == null) "" else ":${returnType.getTypeText()}"}"
                val entries = method.annotationEntries
                if (entries.isEmpty()) {
                    lines.add("${signature}|$packagePath")
                    continue
                }
                for (annotation in entries) {
                    val annotationName = annotation.shortName?.asString() ?: ""
                    val info = annotations[annotationName] ?: error("Annotation $annotationName metadata not found. Make sure your annotation is registered in ScriptMetadataTask.kt")
                    val params = Array<MutableList<String>>(info.size) { mutableListOf() }
                    // Resolve annotation field names
                    var index = 0
                    for (arg in annotation.valueArguments) {
                        val name = arg.getArgumentName()?.asName?.asString()
                        val value = arg.getArgumentExpression()?.text?.trim('"') ?: ""
                        val idx = if (name != null) info.indexOfFirst { it.first == name } else index++
                        for (part in value.split(",")) {
                            if (value.contains("*") || value.contains("#")) {
                                val type = info[idx].second
                                val matches = context.resolve(part, type, parameters, packagePath, annotation)
                                params[idx].addAll(matches)
                            } else {
                                params[idx].add(part)
                            }
                        }
                    }
                    for (i in info.indices) {
                        val first = params[i].firstOrNull() ?: continue
                        for (value in first.split(",")) {
                            // Expand wildcards into matches
                            if (value.contains("*") || value.contains("#")) {
                                val matches = context.resolve(value, info[i].second, parameters, packagePath, annotation)
                                if (params[i].first() == first) {
                                    params[i].removeAt(0)
                                }
                                params[i].addAll(matches)
                            }
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
        if (currentArray.isEmpty()) {
            generateCombinations(arrays, index + 1, current, call)
            return
        }
        for (element in currentArray) {
            current.add(element)
            generateCombinations(arrays, index + 1, current, call)
            current.removeAt(current.size - 1)
        }
    }


    private data class Context(
        val npcIds: Set<String>,
        val itemIds: Set<String>,
        val objectIds: Set<String>,
        val interfaceIds: Set<String>,
        val componentIds: Set<String>,
        val interfaceComponentIds: Set<String>,
        val variableIds: Set<String>,
        val npcOptions: Set<String>,
        val itemOptions: Set<String>,
        val floorItemOptions: Set<String>,
        val objectOptions: Set<String>,
        val interfaceOptions: Set<String>,
    ) {
        fun resolve(value: String, wildcard: WildcardType, parameters: String, packagePath: String, annotation: KtAnnotationEntry): List<String> {
            val set = when (wildcard) {
                is WildcardType.Dynamic -> wildcard.type(this, parameters.split(",")) ?: error("Unknown wildcard type '${parameters}' for '$value' in $packagePath ${annotation.text}")
                WildcardType.NpcId -> npcIds
                WildcardType.InterfaceId -> interfaceIds
                WildcardType.ComponentId -> componentIds
                WildcardType.InterfaceComponentId -> interfaceComponentIds
                WildcardType.ObjectId -> objectIds
                WildcardType.ItemId -> itemIds
                WildcardType.VariableId -> variableIds
                WildcardType.NpcOption -> npcOptions
                WildcardType.InterfaceOption -> interfaceOptions
                WildcardType.FloorItemOption -> floorItemOptions
                WildcardType.ObjectOption -> objectOptions
                WildcardType.ItemOption -> itemOptions
                WildcardType.None -> error("Unexpected wildcard '$value' in $packagePath ${annotation.text}")
            }
            val matches = set.filter { wildcardEquals(value, it) }
            if (matches.isEmpty()) {
                error("No matches for wildcard '${value}' in $packagePath ${annotation.text}")
            }
            return matches
        }


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

    private fun loadContext(): Context {
        val npcIds = mutableSetOf<String>()
        val itemIds = mutableSetOf<String>()
        val objectIds = mutableSetOf<String>()
        val interfaceIds = mutableSetOf<String>()
        val componentIds = mutableSetOf<String>()
        val interfaceComponentIds = mutableSetOf<String>()
        val variableIds = mutableSetOf<String>()
        collectIds(npcIds, itemIds, objectIds, interfaceIds, componentIds, interfaceComponentIds, variableIds)
        val options = System.currentTimeMillis()
        val npcOptions = loadOptions("npc-options")
        val itemOptions = loadOptions("item-options")
        val floorItemOptions = loadOptions("floor-item-options")
        val objectOptions = loadOptions("object-options")
        val interfaceOptions = loadOptions("interface-options")
        println("Loaded ${npcOptions.size} npc, ${itemOptions.size} item, ${floorItemOptions.size} floor item, ${objectOptions.size} object, ${interfaceOptions.size} interface options in ${System.currentTimeMillis() - options}ms")
        return Context(npcIds, itemIds, objectIds, interfaceIds, componentIds, interfaceComponentIds, variableIds, npcOptions, itemOptions, floorItemOptions, objectOptions, interfaceOptions)
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
        interfaceComponentIds: MutableSet<String>,
        variableIds: MutableSet<String>,
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
                var interfaceId = ""
                for (line in file.readLines()) {
                    if (line.startsWith('[')) {
                        val key = line.substringBefore(']').trim('[')
                        if (key.contains(".")) {
                            val component = key.substringAfter('.')
                            componentIds.add(component)
                            interfaceComponentIds.add("$interfaceId:$component")
                        } else {
                            interfaceIds.add(key)
                            interfaceId = key
                        }
                    }
                }
            } else if (file.name.endsWith(".vars.toml") || file.name.endsWith(".varps.toml") || file.name.endsWith(".varbits.toml") || file.name.endsWith(".varcs.toml") || file.name.endsWith(".strings.toml")) {
                for (line in file.readLines()) {
                    if (line.startsWith('[')) {
                        variableIds.add(line.substringBefore(']').trim('['))
                    }
                }
            }
        }
        println("Collected ${npcIds.size} npcs, ${itemIds.size} items, ${objectIds.size} objects, ${interfaceIds.size} interfaces, ${componentIds.size} components, ${variableIds.size} variables in ${System.currentTimeMillis() - start}ms")
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
