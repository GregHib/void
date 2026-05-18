package world.gregs.voidps.tools.search.screen.view.tab

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.data.RenderAnimationDefinition
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.data.*
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.tools.search.screen.view.detail.FieldLink
import java.io.File

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
    const val STRUCTS = "Structs"
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
            clazz = ItemDefinitionFull::class.java,
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
            decodeFull(cache, ItemDecoderFull(), ItemDefinitions)
        },
        DefinitionTab(
            label = Tabs.NPCS,
            clazz = NPCDefinitionFull::class.java,
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
                FieldLink("stackIds", Tabs.ITEMS),
            )
        ) {
            NPCDefinitions.init(NPCDecoder(true).load(cache))
            if (loadConfig) {
                NPCDefinitions.load(files.getValue(Settings["definitions.npcs"]))
            }
            decodeFull(cache, NPCDecoderFull(), NPCDefinitions)
        },
        DefinitionTab(
            label = Tabs.OBJS,
            clazz = ObjectDefinitionFull::class.java,
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
            decodeFull(cache, ObjectDecoderFull(), ObjectDefinitions)
        },
        DefinitionTab(
            label = Tabs.ANIMS,
            clazz = AnimationDefinitionFull::class.java,
            defaultColumns = listOf("id", "stringId"),
            fieldLinks = listOf(
                FieldLink("leftHandItem", Tabs.ITEMS),
                FieldLink("rightHandItem", Tabs.ITEMS),
                FieldLink("sounds", Tabs.SOUNDS),
            )
        ) {
            AnimationDefinitions.init(AnimationDecoder().load(cache))
            if (loadConfig) {
                AnimationDefinitions.load(files.getValue(Settings["definitions.animations"]))
            }
            decodeFull(cache, AnimationDecoderFull(), AnimationDefinitions)
        },
        DefinitionTab(
            label = Tabs.EMOTES,
            clazz = RenderAnimationDefinition::class.java,
            defaultColumns = listOf("id", "stringId", "primaryIdle", "primaryWalk", "run"),
            fieldLinks = listOf(
                FieldLink("primaryIdle", Tabs.ANIMS),
                FieldLink("primaryWalk", Tabs.ANIMS),
                FieldLink("secondaryWalk", Tabs.ANIMS),
                FieldLink("walkBackwards", Tabs.ANIMS),
                FieldLink("run", Tabs.ANIMS),
                FieldLink("turning", Tabs.ANIMS),
                FieldLink("sideStepLeft", Tabs.ANIMS),
                FieldLink("sideStepRight", Tabs.ANIMS),
                FieldLink("anInt3262", Tabs.ANIMS),
                FieldLink("anInt3297", Tabs.ANIMS),
                FieldLink("anInt3269", Tabs.ANIMS),
                FieldLink("anInt3304", Tabs.ANIMS),
                FieldLink("anInt3271", Tabs.ANIMS),
                FieldLink("anInt3270", Tabs.ANIMS),
                FieldLink("anInt3282", Tabs.ANIMS),
                FieldLink("anInt3253", Tabs.ANIMS),
                FieldLink("anInt3293", Tabs.ANIMS),
                FieldLink("anInt3298", Tabs.ANIMS),
                FieldLink("anInt3305", Tabs.ANIMS),
            )
        ) {
            val definitions = RenderAnimationDecoder().load(cache)
            if (loadConfig) {
                val emoteDefinitions = RenderEmoteDefinitions()
                emoteDefinitions.load(files.find(Settings["definitions.renderEmotes"]))
                definitions.map {
                    val def = emoteDefinitions.getOrNull(it.id)
                    it.copy(stringId = def?.stringId ?: "", params = def?.params)
                }
            } else {
                definitions.toList()
            }
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
            defaultColumns = listOf("id", "stringId", "type"),
            fieldLinks = listOf(
                FieldLink(
                    "components", Tabs.COMPONENTS,
                    targetFilters = listOf(
                        "id" to "\$self",
                        "parent" to "id",
                    ),
                    resolveByFields = listOf("parent"),
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
            clazz = InterfaceComponentDefinitionFull::class.java,
            defaultColumns = listOf("parent", "id", "stringId"),
            fieldLinks = listOf(
                FieldLink("parent", Tabs.IFACES),
                FieldLink("animation", Tabs.ANIMS),
                FieldLink("clientVarp", Tabs.VARS),
                FieldLink("clientVarc", Tabs.VARS),
                FieldLink("inventories", Tabs.INVS),
            ),
            dependsOn = listOf(Tabs.IFACES)
        ) {
            decodeFull(cache, InterfaceDecoderFull(), InterfaceDefinitions).flatMap { iface ->
                val def = InterfaceDefinitions.getOrNull(iface.id)
                iface.components?.mapIndexed { index, comp ->
                    val c = def?.components?.get(index)
                    val params = comp.params.orEmpty() + def?.params.orEmpty()
                    comp.copy(id = index, parent = iface.id, stringId = c?.stringId ?: "", params = params)
                }?.toList() ?: emptyList()
            }
        },
        DefinitionTab(
            label = Tabs.ENUMS,
            clazz = EnumDefinition::class.java,
            defaultColumns = listOf("id", "stringId", "keyType", "valueType", "map"),
            dependsOn = listOf(Tabs.ITEMS, Tabs.IFACES, Tabs.INVS, Tabs.NPCS, Tabs.OBJS, Tabs.STRUCTS)
        ) {
            EnumDefinitions.init(EnumDecoder().load(cache))
            if (loadConfig) {
                Tables.load(files.list(Settings["definitions.tables"]))
                EnumDefinitions.load(files.list(Settings["definitions.enums"]))
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
        DefinitionTab(
            label = Tabs.STRUCTS,
            clazz = StructDefinition::class.java,
            defaultColumns = listOf("id", "stringId", "params"),
        ) {
            StructDefinitions.init(StructDecoder().load(cache))
            if (loadConfig) {
                StructDefinitions.load(files.find(Settings["definitions.structs"]))
            }
            StructDefinitions.definitions.toList()
        },
    )
}

private fun <T, F> decodeFull(
    cache: CacheDelegate,
    decoder: DefinitionDecoder<F>,
    definitions: DefinitionsDecoder<T>,
): List<F> where T : Definition, T : Parameterized, F : Definition, F : Parameterized {
    return decoder.load(cache).map { full ->
        val def = definitions.getOrNull(full.id)
        val loaded = def?.params ?: return@map full
        val params = full.params
        if (params != null) {
            (params as MutableMap).putAll(loaded)
        } else {
            full.params = params
        }
        full.stringId = def.stringId
        full
    }
}

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
