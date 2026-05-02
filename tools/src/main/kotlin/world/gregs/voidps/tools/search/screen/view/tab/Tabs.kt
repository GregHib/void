package world.gregs.voidps.tools.search.screen.view.tab

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.data.RenderAnimationDefinition
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.tools.search.screen.view.detail.FieldLink
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2

object Tabs {
    const val ITEMS = "Items"
    const val NPCS = "NPCs"
    const val OBJS = "Objs"
    const val ANIMS = "Anims"
    const val EMOTES = "Emotes"
    const val GFX = "Gfx"
    const val SOUNDS = "Sounds"
    const val IFACES = "Ifaces"
    const val COMPONENTS = "Components"
    const val ENUMS = "Enums"
    const val VARS = "Vars"
    const val INVS = "Invs"
}

fun buildTabs(path: String): Result<List<DefinitionTab<*>>> = runCatching {
    val file = File(path)
    val cachePath: String = when {
        file.resolve("cache").exists() -> file.resolve("cache").absolutePath
        file.resolve("main_file_cache.dat2").exists() -> path
        else -> error("No cache found in dir: '$path'")
    }
    var loadConfig = false
    val files = if (file.resolve("dirs.txt").exists()) {
        loadConfig = true
        configFiles(path, "${path}/.temp/modified.dat")
    } else {
        configFiles()
    }
    val cache = CacheDelegate(cachePath)

    listOf(
        DefinitionTab(
            label = Tabs.ITEMS,
            clazz = ItemDefinition::class.java,
            defaultColumns = listOf("id", "stringId", "name"),
            fieldLinks = listOf(
                FieldLink("noteId", Tabs.ITEMS),
                FieldLink("lendId", Tabs.ITEMS),
            )
        ) {
            ItemDefinitions.init(ItemDecoder().load(cache))
            if (loadConfig) {
                ItemDefinitions.load(files.list(Settings["definitions.items"]))
            }
            ItemDefinitions.definitions.toList()
        },
        DefinitionTab(
            label = Tabs.NPCS,
            clazz = NPCDefinition::class.java,
            defaultColumns = listOf("id", "stringId", "name"),
            fieldLinks = listOf(
                FieldLink("renderEmote", Tabs.EMOTES),
                FieldLink("idleSound", Tabs.SOUNDS),
                FieldLink("crawlSound", Tabs.SOUNDS),
                FieldLink("walkSound", Tabs.SOUNDS),
                FieldLink("runSound", Tabs.SOUNDS),
                FieldLink("transforms", Tabs.NPCS),
                FieldLink("varbit", Tabs.VARS),
                FieldLink("varp", Tabs.VARS),
            )
        ) {
            NPCDefinitions.init(NPCDecoder(true).load(cache))
            if (loadConfig) {
                NPCDefinitions.load(files.getValue(Settings["definitions.npcs"]))
            }
            NPCDefinitions.definitions.toList()
        },
        DefinitionTab(
            label = Tabs.OBJS,
            clazz = ObjectDefinition::class.java,
            defaultColumns = listOf("id", "stringId", "name", "varbit", "varp"),
            fieldLinks = listOf(
                FieldLink("transforms", Tabs.OBJS),
                FieldLink("varbit", Tabs.VARS),
                FieldLink("varp", Tabs.VARS),
            )
        ) {
            ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache))
            if (loadConfig) {
                ObjectDefinitions.load(files.getValue(Settings["definitions.objects"]))
            }
            ObjectDefinitions.definitions.toList()
        },
        DefinitionTab(
            label = Tabs.ANIMS,
            clazz = AnimationDefinition::class.java,
            defaultColumns = listOf("id", "stringId", "priority")
        ) {
            AnimationDefinitions.init(AnimationDecoder().load(cache))
            if (loadConfig) {
                AnimationDefinitions.load(files.getValue(Settings["definitions.animations"]))
            }
            AnimationDefinitions.definitions.toList()
        },
        DefinitionTab(
            label = Tabs.EMOTES,
            clazz = RenderAnimationDefinition::class.java,
            defaultColumns = listOf("id", "primaryIdle", "primaryWalk", "run"),
            fieldLinks = listOf(
                FieldLink("primaryIdle", Tabs.ANIMS),
                FieldLink("primaryWalk", Tabs.ANIMS),
                FieldLink("secondaryWalk", Tabs.ANIMS),
                FieldLink("run", Tabs.ANIMS),
                FieldLink("turning", Tabs.ANIMS),
                FieldLink("sideStepLeft", Tabs.ANIMS),
                FieldLink("sideStepRight", Tabs.ANIMS),
            )
        ) {
            RenderAnimationDecoder().load(cache).toList()
        },
        DefinitionTab(
            label = Tabs.GFX,
            clazz = GraphicDefinition::class.java,
            defaultColumns = listOf("id", "stringId"),
            fieldLinks = listOf(
                FieldLink("animationId", Tabs.ANIMS)
            )
        ) {
            GraphicDefinitions.init(GraphicDecoder().load(cache))
            if (loadConfig) {
                GraphicDefinitions.load(files.list(Settings["definitions.graphics"]))
            }
            GraphicDefinitions.definitions.toList()
        },
        DefinitionTab(
            label = Tabs.SOUNDS,
            clazz = SoundDefinition::class.java,
            defaultColumns = listOf("id", "stringId")
        ) {
            if (loadConfig) {
                SoundDefinitions().load(files.list(Settings["definitions.sounds"])).definitions.toList()
            } else {
                emptyList()
            }
        },
        DefinitionTab(
            label = Tabs.IFACES,
            clazz = InterfaceWrapper::class.java,
            defaultColumns = listOf("id", "stringId"),
            fieldLinks = listOf(
                FieldLink(
                    "components", Tabs.COMPONENTS,
                    targetFilters = listOf(
                        "id" to "\$self",    // component.id == clicked value
                        "parent" to "id",        // component.parent == interfaceDefinition.id
                    ),
                    resolveByFields = listOf("id", "parent"),
                )
            ),
        ) {
            InterfaceDefinitions.init(InterfaceDecoder().load(cache))
            if (loadConfig) {
                InterfaceDefinitions.load(
                    files.list(Settings["definitions.interfaces"]),
                    files.find(Settings["definitions.interfaces.types"])
                )
            }
            InterfaceDefinitions.definitions.map {
                InterfaceWrapper(
                    id = it.id,
                    components = it.components?.keys?.toIntArray(),
                    type = it.type,
                    fixed = it.fixed,
                    resizable = it.resizable,
                    permanent = it.permanent,
                    stringId = it.stringId,
                    params = it.params
                )
            }
        },
        DefinitionTab(
            label = Tabs.COMPONENTS,
            clazz = ComponentWrapper::class.java,
            defaultColumns = listOf("parent", "id", "stringId"),
            fieldLinks = listOf(FieldLink("parent", Tabs.IFACES)),
            dependsOn = listOf(Tabs.IFACES)
        ) {
            InterfaceDefinitions.definitions.flatMap { iface ->
                iface.components?.map { (id, comp) ->
                    ComponentWrapper(
                        id = InterfaceDefinition.componentId(id),
                        parent = iface.id,
                        options = comp.options,
                        information = comp.information,
                        stringId = comp.stringId,
                        params = comp.params
                    )
                } ?: emptyList()
            }
        },
        DefinitionTab(
            label = Tabs.ENUMS,
            clazz = EnumDefinition::class.java,
            defaultColumns = listOf("id", "stringId"),
            dependsOn = listOf(Tabs.ITEMS, Tabs.IFACES, Tabs.INVS, Tabs.NPCS, Tabs.OBJS)
        ) {
            EnumDefinitions.init(EnumDecoder().load(cache))
            if (loadConfig) {
                // EnumDefinitions.load(files.list(Settings["definitions.enums"]))
            }
            EnumDefinitions.definitions.toList()
        },
        DefinitionTab(Tabs.VARS, VariableWrapper::class.java, listOf("id", "stringId", "type", "values")) {
            if (loadConfig) {
                VariableDefinitions.load(files)
            }
            VariableDefinitions.definitions.map { (stringId, def) ->
                VariableWrapper(
                    id = def.id,
                    type = when (def) {
                        is VariableDefinition.VarbitDefinition -> "varbit"
                        is VariableDefinition.VarpDefinition -> "varp"
                        is VariableDefinition.VarcDefinition -> "varc"
                        is VariableDefinition.VarcStrDefinition -> "varcstr"
                        is VariableDefinition.CustomVariableDefinition -> "custom"
                        else -> "unknown"
                    },
                    valueType = def.values::class.simpleName?.removeSuffix("Values")?.lowercase() ?: "",
                    values = when (val vals = def.values) {
                        is ListValues -> vals.values
                        is MapValues -> vals.values
                        is BitwiseValues -> vals.values
                        else -> null
                    },
                    default = def.defaultValue,
                    persist = def.persistent,
                    transmit = def.transmit,
                    stringId = stringId,
                )
            }
        },
        DefinitionTab(
            label = Tabs.INVS,
            clazz = InventoryDefinition::class.java,
            defaultColumns = listOf("id", "stringId"),
            dependsOn = listOf(Tabs.ITEMS),
            fieldLinks = listOf(FieldLink("ids", Tabs.ITEMS))
        ) {
            InventoryDefinitions.init(InventoryDecoder().load(cache))
            if (loadConfig) {
                InventoryDefinitions.load(
                    files.list(Settings["definitions.inventories"]),
                    files.list(Settings["definitions.shops"])
                )
            }
            InventoryDefinitions.definitions.toList()
        },
    )
}

data class ComponentWrapper(
    override var id: Int = -1,
    var parent: Int = -1,
    var options: Array<String?>? = null,
    var information: Array<Any>? = null,
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Definition, Parameterized

data class InterfaceWrapper(
    override var id: Int = -1,
    var components: IntArray? = null,
    var type: String? = null,
    var fixed: Int = -1,
    var resizable: Int = -1,
    var permanent: Boolean = true,
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Definition, Parameterized

data class VariableWrapper(
    override var id: Int,
    val type: String,
    val valueType: String,
    val values: Any? = null,
    val default: Any? = null,
    val persist: Boolean,
    val transmit: Boolean,
    override var stringId: String,
    override var params: Map<Int, Any>? = null,
) : Definition, Parameterized
