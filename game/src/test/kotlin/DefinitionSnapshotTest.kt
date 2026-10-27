import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.FontDecoder
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.snapshot.Snapshot
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.QuickChatPhraseDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.type.area.Polygon
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Definitions restored from a binary snapshot must be identical to those parsed from the toml files.
 */
internal class DefinitionSnapshotTest {

    @TempDir
    lateinit var temp: Path

    private lateinit var files: ConfigFiles
    private lateinit var cache: Cache

    @BeforeEach
    fun setup() {
        val properties = Properties()
        properties.load(DefinitionSnapshotTest::class.java.getResourceAsStream("/game.properties")!!)
        for ((key, value) in properties) {
            if (value is String && value.startsWith("./")) {
                properties[key] = value.replace("./", "../")
            }
        }
        properties["storage.caching.path"] = "$temp/"
        properties["storage.snapshots.active"] = "true"
        Settings.load(properties)
        files = configFiles()
        cache = MemoryCache(Settings["storage.cache.path"])
    }

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    @Test
    fun `Item snapshot matches toml`() {
        val paths = files.list(Settings["definitions.items"])
        val snapshot = { files.snapshot("items", paths) }
        fun load(paths: List<String>, snapshot: Snapshot?): String {
            ItemDefinitions.init(ItemDecoder().load(cache)).load(paths, snapshot)
            return dump(ItemDefinitions.definitions.asList(), ItemDefinitions.ids)
        }

        val parsed = load(paths, null)
        assertEquals(parsed, load(paths, snapshot()), "Writing a snapshot shouldn't change the result")
        assertTrue(temp.resolve("items.snapshot").exists())
        // No paths, so it can only be loaded from the snapshot
        assertEquals(parsed, load(emptyList(), snapshot()))
    }

    @Test
    fun `Npc snapshot matches toml`() {
        val paths = files.list(Settings["definitions.npcs"])
        val snapshot = { files.snapshot("npcs", paths) }
        fun load(paths: List<String>, snapshot: Snapshot?): String {
            NPCDefinitions.init(NPCDecoder(member = true).load(cache)).load(paths, null, snapshot)
            return dump(NPCDefinitions.definitions.asList(), NPCDefinitions.ids) { definition ->
                "${definition.hitpoints} ${definition.regenRate} ${definition.huntRange} ${definition.huntMode} ${definition.allowedUnder} ${definition.solid} ${definition.blocksPlayers}"
            }
        }

        val parsed = load(paths, null)
        assertEquals(parsed, load(paths, snapshot()), "Writing a snapshot shouldn't change the result")
        assertTrue(temp.resolve("npcs.snapshot").exists())
        assertEquals(parsed, load(emptyList(), snapshot()))
    }

    @Test
    fun `Object snapshot matches toml`() {
        val paths = files.list(Settings["definitions.objects"])
        val snapshot = { files.snapshot("objects", paths) }
        fun load(paths: List<String>, snapshot: Snapshot?): String {
            ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(paths, snapshot)
            return dump(ObjectDefinitions.definitions.asList(), ObjectDefinitions.ids)
        }

        val parsed = load(paths, null)
        assertEquals(parsed, load(paths, snapshot()), "Writing a snapshot shouldn't change the result")
        assertTrue(temp.resolve("objects.snapshot").exists())
        assertEquals(parsed, load(emptyList(), snapshot()))
    }

    @Test
    fun `Table snapshot matches toml`() {
        ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
        ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(files.list(Settings["definitions.objects"]))
        NPCDefinitions.init(NPCDecoder(member = true).load(cache)).load(files.list(Settings["definitions.npcs"]))
        AnimationDefinitions.init(AnimationDecoder().load(cache)).load(files.list(Settings["definitions.animations"]))
        GraphicDefinitions.init(GraphicDecoder().load(cache)).load(files.list(Settings["definitions.graphics"]))
        VariableDefinitions.load(files)
        val paths = files.list(Settings["definitions.tables"])
        val snapshot = { files.snapshot("tables", paths) }

        Tables.load(paths)
        val parsed = dumpTables()
        Tables.load(paths, snapshot())
        assertEquals(parsed, dumpTables(), "Writing a snapshot shouldn't change the result")
        assertTrue(temp.resolve("tables.snapshot").exists())
        Tables.clear()
        Rows.clear()
        Tables.load(emptyList(), snapshot())
        assertEquals(parsed, dumpTables())
    }

    @Test
    fun `Full definition snapshots match the loaded definitions`() {
        val items = files.list(Settings["definitions.items"])
        ItemDefinitions.init(ItemDecoder().load(cache)).load(items)
        val itemDump = dump(ItemDefinitions.definitions.asList(), ItemDefinitions.ids)
        val itemDefinitions = ItemDefinitions.definitions.toList()
        ItemDefinitions.saveDefinitions(files.snapshot("items.all", items)!!)
        ItemDefinitions.clear()
        assertTrue(ItemDefinitions.restoreDefinitions(files.snapshot("items.all", items)!!))
        assertEquals(itemDefinitions, ItemDefinitions.definitions.toList())
        assertEquals(itemDump, dump(ItemDefinitions.definitions.asList(), ItemDefinitions.ids))

        val npcs = files.list(Settings["definitions.npcs"])
        NPCDefinitions.init(NPCDecoder(member = true).load(cache)).load(npcs)
        val npcDump = dump(NPCDefinitions.definitions.asList(), NPCDefinitions.ids)
        val npcDefinitions = NPCDefinitions.definitions.toList()
        NPCDefinitions.saveDefinitions(files.snapshot("npcs.all", npcs)!!)
        NPCDefinitions.clear()
        assertTrue(NPCDefinitions.restoreDefinitions(files.snapshot("npcs.all", npcs)!!))
        assertEquals(npcDefinitions, NPCDefinitions.definitions.toList())
        assertEquals(npcDump, dump(NPCDefinitions.definitions.asList(), NPCDefinitions.ids))

        val objects = files.list(Settings["definitions.objects"])
        ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(objects)
        val objectDump = dump(ObjectDefinitions.definitions.asList(), ObjectDefinitions.ids)
        val objectDefinitions = ObjectDefinitions.definitions.toList()
        ObjectDefinitions.saveDefinitions(files.snapshot("objects.all", objects)!!)
        ObjectDefinitions.clear()
        assertTrue(ObjectDefinitions.restoreDefinitions(files.snapshot("objects.all", objects)!!))
        assertEquals(objectDefinitions, ObjectDefinitions.definitions.toList())
        assertEquals(objectDump, dump(ObjectDefinitions.definitions.asList(), ObjectDefinitions.ids))
    }

    @Test
    fun `Variable snapshot matches toml`() {
        val paths = listOf("players", "bits", "clients", "strings", "customs").flatMap { files.list(Settings["definitions.variables.$it"]) }
        VariableDefinitions.load(files)
        val parsed = dumpVariables()
        assertTrue(temp.resolve("variables.snapshot").exists())
        VariableDefinitions.clear()
        // No paths, so it can only be loaded from the snapshot
        VariableDefinitions.load(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), files.snapshot("variables", paths))
        assertEquals(parsed, dumpVariables())
    }

    @Test
    fun `Other full definition snapshots match the loaded definitions`() {
        ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))

        val animations = files.list(Settings["definitions.animations"])
        AnimationDefinitions.init(AnimationDecoder().load(cache)).load(animations)
        val animationDefinitions = AnimationDefinitions.definitions.toList()
        val animationIds = AnimationDefinitions.ids.toSortedMap()
        AnimationDefinitions.saveDefinitions(files.snapshot("animations", animations)!!)
        AnimationDefinitions.clear()
        assertTrue(AnimationDefinitions.restoreDefinitions(files.snapshot("animations", animations)!!))
        assertEquals(animationDefinitions, AnimationDefinitions.definitions.toList())
        assertEquals(animationIds, AnimationDefinitions.ids.toSortedMap())

        val graphics = files.list(Settings["definitions.graphics"])
        GraphicDefinitions.init(GraphicDecoder().load(cache)).load(graphics)
        val graphicDefinitions = GraphicDefinitions.definitions.toList()
        val graphicIds = GraphicDefinitions.ids.toSortedMap()
        GraphicDefinitions.saveDefinitions(files.snapshot("graphics", graphics)!!)
        GraphicDefinitions.clear()
        assertTrue(GraphicDefinitions.restoreDefinitions(files.snapshot("graphics", graphics)!!))
        assertEquals(graphicDefinitions, GraphicDefinitions.definitions.toList())
        assertEquals(graphicIds, GraphicDefinitions.ids.toSortedMap())

        val interfaces = files.list(Settings["definitions.interfaces"])
        val types = files.find(Settings["definitions.interfaces.types"])
        InterfaceDefinitions.init(InterfaceDecoder().load(cache)).load(interfaces, types)
        val interfaceDump = dumpInterfaces()
        val interfaceIds = InterfaceDefinitions.ids.toSortedMap()
        val componentIds = InterfaceDefinitions.componentIds.toSortedMap()
        InterfaceDefinitions.saveDefinitions(files.snapshot("interfaces", interfaces + types)!!)
        InterfaceDefinitions.clear()
        assertTrue(InterfaceDefinitions.restoreDefinitions(files.snapshot("interfaces", interfaces + types)!!))
        assertEquals(interfaceDump, dumpInterfaces())
        assertEquals(interfaceIds, InterfaceDefinitions.ids.toSortedMap())
        assertEquals(componentIds, InterfaceDefinitions.componentIds.toSortedMap())

        val structs = files.find(Settings["definitions.structs"])
        StructDefinitions.init(StructDecoder().load(cache)).load(structs)
        val structDefinitions = StructDefinitions.definitions.toList()
        val structIds = StructDefinitions.ids.toSortedMap()
        StructDefinitions.saveDefinitions(files.snapshot("structs", listOf(structs))!!)
        StructDefinitions.clear()
        assertTrue(StructDefinitions.restoreDefinitions(files.snapshot("structs", listOf(structs))!!))
        assertEquals(structDefinitions, StructDefinitions.definitions.toList())
        assertEquals(structIds, StructDefinitions.ids.toSortedMap())

        val inventories = files.list(Settings["definitions.inventories"])
        val shops = files.list(Settings["definitions.shops"])
        InventoryDefinitions.init(InventoryDecoder().load(cache)).load(inventories, shops)
        val inventoryDefinitions = InventoryDefinitions.definitions.toList()
        val inventoryIds = InventoryDefinitions.ids.toSortedMap()
        InventoryDefinitions.saveDefinitions(files.snapshot("inventories", inventories + shops)!!)
        InventoryDefinitions.clear()
        assertTrue(InventoryDefinitions.restoreDefinitions(files.snapshot("inventories", inventories + shops)!!))
        assertEquals(inventoryDefinitions, InventoryDefinitions.definitions.toList())
        assertEquals(inventoryIds, InventoryDefinitions.ids.toSortedMap())

        val fontPath = files.find(Settings["definitions.fonts"])
        val fonts = FontDefinitions(FontDecoder().load(cache)).load(fontPath)
        fonts.save(files.snapshot("fonts", listOf(fontPath))!!)
        val restoredFonts = FontDefinitions.restore(files.snapshot("fonts", listOf(fontPath))!!)!!
        assertEquals(fonts.definitions.toList(), restoredFonts.definitions.toList())
        assertEquals(fonts.ids.toSortedMap(), restoredFonts.ids.toSortedMap())

        val phrases = QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(cache)).load()
        phrases.save(files.snapshot("quick_chat", emptyList())!!)
        val restoredPhrases = QuickChatPhraseDefinitions.restore(files.snapshot("quick_chat", emptyList())!!)!!
        assertEquals(phrases.definitions.toList(), restoredPhrases.definitions.toList())
    }

    @Test
    fun `Sound combat and area snapshots match toml`() {
        val sounds = files.list(Settings["definitions.sounds"])
        val parsedSounds = SoundDefinitions().load(sounds)
        SoundDefinitions().load(sounds, files.snapshot("sounds", sounds))
        val restoredSounds = SoundDefinitions().load(emptyList(), files.snapshot("sounds", sounds))
        assertTrue(temp.resolve("sounds.snapshot").exists())
        assertEquals(parsedSounds.definitions.toList(), restoredSounds.definitions.toList())
        assertEquals(parsedSounds.ids.toSortedMap(), restoredSounds.ids.toSortedMap())

        val combat = files.list(Settings["definitions.combatAttacks"])
        val parsedCombat = CombatDefinitions().load(combat)
        CombatDefinitions().load(combat, files.snapshot("combat", combat))
        assertTrue(temp.resolve("combat.snapshot").exists())
        val restoredCombat = CombatDefinitions().load(emptyList(), files.snapshot("combat", combat))
        assertEquals(parsedCombat.all.toSortedMap(), restoredCombat.all.toSortedMap())

        val areas = files.list(Settings["map.areas"])
        Areas.load(areas)
        val parsedAreas = dumpAreas()
        Areas.load(areas, files.snapshot("areas", areas))
        assertTrue(temp.resolve("areas.snapshot").exists())
        Areas.clear()
        Areas.load(emptyList(), files.snapshot("areas", areas))
        assertEquals(parsedAreas, dumpAreas())
    }

    @Test
    fun `Snapshot is ignored when a file changes`() {
        val paths = files.list(Settings["definitions.objects"])
        ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(paths, files.snapshot("objects", paths))
        assertTrue(temp.resolve("objects.snapshot").fileSize() > 0)

        val changed = files.copy(stamps = files.stamps + (paths.first() to -1L))
        assertTrue(changed.snapshot("objects", paths)!!.reader() == null)
    }

    private fun <T : Parameterized> dump(definitions: List<T>, ids: Map<String, Int>, extra: (T) -> String = { "" }): String {
        val builder = StringBuilder()
        for (entry in ids.entries.sortedBy { it.key }) {
            builder.append("id ").append(entry.key).append('=').append(entry.value).append('\n')
        }
        for (definition in definitions) {
            builder.append(definition.stringId).append(' ').append(extra(definition)).append(' ')
            builder.append(render(definition.params)).append('\n')
        }
        return builder.toString()
    }

    /**
     * Component params can contain arrays which don't have a content based equals.
     */
    private fun dumpInterfaces(): String {
        val builder = StringBuilder()
        for (definition in InterfaceDefinitions.definitions) {
            builder.append(definition.id).append(' ').append(definition.type).append(' ').append(definition.fixed).append(' ').append(definition.resizable).append(' ')
                .append(definition.permanent).append(' ').append(definition.stringId).append(' ').append(render(definition.params)).append('\n')
            for ((index, component) in definition.components.orEmpty().entries.sortedBy { it.key }) {
                builder.append("  ").append(index).append(' ').append(component.id).append(' ').append(render(component.options)).append(' ').append(render(component.information)).append(' ')
                    .append(component.baseX).append(' ').append(component.baseY).append(' ').append(component.defaultImage).append(' ').append(component.stringId).append(' ').append(render(component.params)).append('\n')
            }
        }
        return builder.toString()
    }

    private fun dumpAreas(): String {
        val builder = StringBuilder()
        val tags = sortedSetOf<String>()
        for (definition in Areas.named.values.sortedBy { it.name }) {
            val area = definition.area
            val shape = if (area is Polygon) "Polygon ${area.xPoints.toList()} ${area.yPoints.toList()} ${area.level} ${area.bounds.maxLevel}" else area.toString()
            builder.append(definition.name).append(' ').append(shape).append(' ').append(definition.tags.sorted()).append(' ').append(definition.stringId).append(' ').append(render(definition.params)).append(" |")
            tags.addAll(definition.tags)
            for (level in 0..3) {
                for (zone in area.toZones(level)) {
                    builder.append(' ').append(zone.id).append(':').append(Areas.get(zone).map { it.name }.sorted())
                }
            }
            builder.append(" | ")
        }
        for (tag in tags) {
            builder.append(tag).append('=').append(Areas.tagged(tag).map { it.name }.sorted())
        }
        return builder.toString()
    }

    private fun dumpVariables(): String {
        val builder = StringBuilder()
        for ((name, definition) in VariableDefinitions.definitions.entries.sortedBy { it.key }) {
            val values = when (val values = definition.values) {
                is ListValues -> "List ${render(values.values)}"
                is MapValues -> "Map ${render(values.values)}"
                is BitwiseValues -> "Bitwise ${render(values.values)}"
                else -> values.toString()
            }
            builder.append(name).append(' ').append(definition.javaClass.simpleName).append(' ').append(definition.id).append(' ')
                .append(values).append(' ').append(render(definition.defaultValue)).append(' ')
                .append(definition.persistent).append(' ').append(definition.transmit).append(' ')
                .append(VariableDefinitions.getVarbit(definition.id)).append(' ').append(VariableDefinitions.getVarp(definition.id)).append('\n')
        }
        return builder.toString()
    }

    private fun dumpTables(): String {
        val builder = StringBuilder()
        for ((name, table) in Tables.definitions.entries.sortedBy { it.key }) {
            builder.append(name).append(' ').append(table.columns).append(' ').append(table.types.toList())
                .append(' ').append(table.default.map { render(it) }).append(' ').append(table.rows.toList()).append('\n')
        }
        for (row in Rows.definitions) {
            builder.append(row.id).append(' ').append(row.stringId).append(' ').append(row.data.map { render(it) }).append('\n')
        }
        for (entry in Rows.ids.entries.sortedBy { it.key }) {
            builder.append(entry.key).append('=').append(entry.value).append('\n')
        }
        return builder.toString()
    }

    /**
     * Renders including types so that ints, longs and different collection kinds can't be mistaken for each other.
     */
    private fun render(value: Any?): String = when (value) {
        null -> "null"
        is Map<*, *> -> value.entries.sortedBy { it.key.toString() }.joinToString(",", "{", "}") { "${it.key}:${render(it.value)}" }
        is Array<*> -> value.joinToString(",", "Array[", "]") { render(it) }
        is Set<*> -> value.joinToString(",", "Set[", "]") { render(it) }
        is Collection<*> -> value.joinToString(",", "List[", "]") { render(it) }
        else -> "${value.javaClass.simpleName}($value)"
    }
}
