import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
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
 * Gradle task which incrementally collects method info about script classes inside a given directory.
 * Collects:
 *  -  annotations for invocation
 *  - Parameters of called methods in init {} and their resolved wildcards
 */
abstract class ScriptMetadataTask : DefaultTask() {

    private sealed class WildcardType {
        data object None : WildcardType()
        data object NpcId : WildcardType()
        data object InterfaceId : WildcardType()
        data object ComponentId : WildcardType()
        data object InterfaceComponentId : WildcardType()
        data object ObjectId : WildcardType()
        data object ItemId : WildcardType()
        data object VariableId : WildcardType()
        data object NpcOption : WildcardType()
        data object InterfaceOption : WildcardType()
        data object FloorItemOption : WildcardType()
        data object ObjectOption : WildcardType()
        data object ItemOption : WildcardType()
    }

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:InputFiles
    abstract var dataDirectory: ConfigurableFileTree

    @get:Internal
    abstract var resourceDirectory: File

    @get:OutputFile
    abstract var scriptsFile: File

    @get:OutputFile
    abstract var wildcardsFile: File

    init {
        description = "Analyzes Kotlin files and extracts annotation information"
        group = "metadata"
    }

    private val methods = mapOf(
        "npcSpawn" to listOf("id" to WildcardType.NpcId),
        "objectSpawn" to listOf("id" to WildcardType.ObjectId),
        "floorItemSpawn" to listOf("id" to WildcardType.ItemId),
        "npcDespawn" to listOf("id" to WildcardType.NpcId),
        "objectDespawn" to listOf("id" to WildcardType.ObjectId),
        "floorItemDespawn" to listOf("id" to WildcardType.ItemId),
        "npcLevelChanged" to listOf("id" to WildcardType.NpcId),
        "npcMoved" to listOf("id" to WildcardType.NpcId),
        "variableSet" to listOf("key" to WildcardType.VariableId),
        "npcVariableSet" to listOf("key" to WildcardType.VariableId),
        // Approach interactions
        "talkWithApproach" to listOf("npc" to WildcardType.NpcId),
        "npcApproach" to listOf("option" to WildcardType.NpcOption, "npc" to WildcardType.NpcId),
        "objectApproach" to listOf("option" to WildcardType.ObjectOption, "obj" to WildcardType.ObjectId),
        "floorItemApproach" to listOf("option" to WildcardType.FloorItemOption, "obj" to WildcardType.ItemId),
        "npcApproachNPC" to listOf("option" to WildcardType.NpcOption, "npc" to WildcardType.NpcId),
        "npcApproachObject" to listOf("option" to WildcardType.ObjectOption, "obj" to WildcardType.ObjectId),
        "npcApproachFloorItem" to listOf("option" to WildcardType.FloorItemOption, "item" to WildcardType.ItemId),
        "talkWith" to listOf("npc" to WildcardType.NpcId),
        // Operate interactions
        "npcOperate" to listOf("option" to WildcardType.NpcOption, "npc" to WildcardType.NpcId),
        "objectOperate" to listOf("option" to WildcardType.ObjectOption, "obj" to WildcardType.ObjectId),
        "floorItemOperate" to listOf("option" to WildcardType.FloorItemOption, "obj" to WildcardType.ItemId),
        "npcOperateNPC" to listOf("option" to WildcardType.NpcOption, "npc" to WildcardType.NpcId),
        "npcOperateObject" to listOf("option" to WildcardType.ObjectOption, "obj" to WildcardType.ObjectId),
        "npcOperateFloorItem" to listOf("option" to WildcardType.FloorItemOption, "item" to WildcardType.ItemId),
        // Interface on operate interactions
        "onPlayerOperate" to listOf("id" to WildcardType.InterfaceComponentId),
        "onNPCOperate" to listOf("id" to WildcardType.InterfaceComponentId, "npc" to WildcardType.NpcId),
        "onObjectOperate" to listOf("id" to WildcardType.InterfaceComponentId, "obj" to WildcardType.ObjectId),
        "itemOnPlayerOperate" to listOf("item" to WildcardType.ItemId),
        "itemOnNPCOperate" to listOf("item" to WildcardType.ItemId, "npc" to WildcardType.NpcId),
        "itemOnObjectOperate" to listOf("item" to WildcardType.ItemId, "obj" to WildcardType.ObjectId),
        // Interface on approach interactions
        "onPlayerApproach" to listOf("id" to WildcardType.InterfaceComponentId),
        "onNPCApproach" to listOf("id" to WildcardType.InterfaceComponentId, "npc" to WildcardType.NpcId),
        "onObjectApproach" to listOf("id" to WildcardType.InterfaceComponentId, "obj" to WildcardType.ObjectId),
        "itemOnPlayerApproach" to listOf("item" to WildcardType.ItemId),
        "itemOnNPCApproach" to listOf("item" to WildcardType.ItemId, "npc" to WildcardType.NpcId),
        "itemOnObjectApproach" to listOf("item" to WildcardType.ItemId, "obj" to WildcardType.ObjectId),
        // Interface interactions
        "onItem" to listOf("id" to WildcardType.InterfaceComponentId, "item" to WildcardType.ItemId),
        "itemOnItem" to listOf("fromItem" to WildcardType.ItemId, "toItem" to WildcardType.ItemId),
    )

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val start = System.currentTimeMillis()

        val context = loadContext()
        val lines: MutableList<String>
        val wildcards: MutableList<String>
        if (!inputChanges.isIncremental) {
            // Clean output for non-incremental runs
            scriptsFile.delete()
            logger.info("Non-incremental run: analyzing all files")
            lines = mutableListOf()
            wildcards = mutableListOf()
        } else {
            lines = if (scriptsFile.exists()) scriptsFile.readLines().toMutableList() else mutableListOf()
            wildcards = if (wildcardsFile.exists()) wildcardsFile.readLines().toMutableList() else mutableListOf()
        }
        val disposable = Disposer.newDisposable()
        val environment = createKotlinEnvironment(disposable)
        var scripts = 0
        val instance = PsiManager.getInstance(environment.project)
        val scriptClasses = mutableListOf<Pair<KtClass, String>>()
        for (change in inputChanges.getFileChanges(inputDirectory)) {
            val file = change.file
            if (change.changeType == ChangeType.REMOVED) {
                removeName(lines, file.nameWithoutExtension)
                removeName(wildcards, file.nameWithoutExtension)
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
                    if (!wildcards.removeIf { it.endsWith("$packageName.$name") }) {
                        removeName(wildcards, name)
                    }
                }
            }
            if (change.changeType == ChangeType.MODIFIED || change.changeType == ChangeType.ADDED) {
                for (ktClass in classes) {
                    val className = ktClass.name ?: "Anonymous"
                    if (ktClass.superTypeListEntries.any { it.text == "Script" }) {
                        scriptClasses.add(ktClass to "$packageName.$className")
                    }
                }
            }
        }

        for ((ktClass, packagePath) in scriptClasses) {
            for (declaration in ktClass.declarations) {
                if (declaration !is KtClassInitializer) {
                    continue
                }
                for (child in declaration.children) {
                    if (child !is KtBlockExpression) {
                        continue
                    }
                    for (expression in child.children) {
                        if (expression !is KtCallExpression) continue
                        val methodName = expression.calleeExpression?.text ?: return
                        val info = methods[methodName]
                        var index = 0
                        for (arg in expression.valueArguments) {
                            if (arg is KtLambdaArgument) {
                                continue
                            }
                            val name = arg.getArgumentName()?.asName?.asString()
                            val value = arg.getArgumentExpression()?.text?.trim('"') ?: ""
                            if (value.none { it == '*' || it == '#' || it == ',' }) {
                                index++
                                continue
                            }
                            if (info == null) {
                                println("No handling found for method $methodName with wildcard $name=$value in ${ktClass.name}")
                                continue
                            }
                            if (value == "*") { // Match all can be handled separately
                                index++
                                continue
                            }
                            // Resolve field names
                            val idx = if (name != null) info.indexOfFirst { it.first == name } else index++
                            val combined = mutableListOf<String>()
                            for (part in value.split(",")) {
                                // Expand wildcards into matches
                                if (value.contains("*") || value.contains("#")) {
                                    val type = info.getOrNull(idx)?.second ?: continue
                                    val matches = context.resolve(part, type, packagePath)
                                    combined.addAll(matches)
                                } else {
                                    combined.add(part)
                                }
                            }
                            wildcards.add("${value}|${combined.joinToString(",")}|$packagePath")
                        }
                    }
                }
            }
            lines.add(packagePath)
            scripts++
        }
        scriptsFile.writeText(lines.joinToString("\n"))
        wildcardsFile.writeText(wildcards.distinct().joinToString("\n"))
        disposable.dispose()
        println("Metadata for $scripts scripts collected in ${System.currentTimeMillis() - start} ms")
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
        fun resolve(value: String, wildcard: WildcardType, packagePath: String): List<String> {
            val set = when (wildcard) {
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
                WildcardType.None -> error("Unexpected wildcard '$value' in $packagePath")
            }
            val matches = set.filter { wildcardEquals(value, it) }
            if (matches.isEmpty()) {
                error("No matches for '${value}' $wildcard in $packagePath")
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
        for (file in dataDirectory) {
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
