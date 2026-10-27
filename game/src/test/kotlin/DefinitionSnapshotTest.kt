import content.bot.behaviour.NavigationGraphLoader
import content.entity.obj.ObjectTeleportLoader
import content.entity.world.music.MusicTrackLoader
import gameLoaders
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.CombatLoader
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.FontLoader
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ItemLoader
import world.gregs.voidps.engine.data.definition.ItemOnItemDefinitions
import world.gregs.voidps.engine.data.definition.ItemOnItemLoader
import world.gregs.voidps.engine.data.definition.MapLoader
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.QuickChatPhraseLoader
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.SoundLoader
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.data.load.LoaderGraph
import world.gregs.voidps.engine.engineLoaders
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.area.Polygon
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Definitions restored from a binary snapshot must be identical to those loaded from the cache and toml files.
 */
internal class DefinitionSnapshotTest {

    @TempDir
    lateinit var temp: Path

    private lateinit var files: ConfigFiles
    private lateinit var cache: Cache
    private lateinit var graph: LoaderGraph

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
        properties["world.members"] = "true"
        Settings.load(properties)
        files = configFiles()
        cache = MemoryCache(Settings["storage.cache.path"])
        // Maps keep their own files
        graph = LoaderGraph((engineLoaders() + gameLoaders()).filter { it != MapLoader })
    }

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    @Test
    fun `Every snapshot matches what is loaded without one`() {
        graph.run(files, cache, null)
        val loaded = capture()

        graph.run(files, cache, temp)
        assertEquals(emptySet(), graph.restored)
        compare(loaded, capture(), "Writing a snapshot shouldn't change the result")

        val snapshotted = graph.loaders.filter { it.codec != null }.map { it.name }
        for (name in snapshotted) {
            assertTrue(temp.resolve("$name.snapshot").exists(), "Missing snapshot $name")
        }
        graph.run(files, cache, temp)
        assertEquals(snapshotted.toSet(), graph.restored)
        compare(loaded, capture(), "Restoring a snapshot shouldn't change the result")
    }

    @Test
    fun `Snapshots are ignored when a file they depend on changes`() {
        val before = graph.keys(files)
        val items = files.list(Settings["definitions.items"]).first()
        val after = graph.keys(files.copy(stamps = files.stamps + (items to -1L)))
        assertNotEquals(before[ItemLoader], after[ItemLoader])
        // Inventories hold item ids so depend on them
        assertNotEquals(before[graph.get("inventories")], after[graph.get("inventories")])
        assertEquals(before[SoundLoader], after[SoundLoader])
    }

    private fun compare(expected: Map<String, Any?>, actual: Map<String, Any?>, message: String) {
        assertEquals(expected.keys, actual.keys)
        for ((name, value) in expected) {
            assertEquals(value, actual[name], "$message: $name")
        }
    }

    /**
     * Everything the loaders install, in a form which can be compared.
     */
    private fun capture(): Map<String, Any?> {
        val captured = linkedMapOf<String, Any?>()
        captured["items"] = ItemDefinitions.definitions.toList() to dump(ItemDefinitions.definitions.asList(), ItemDefinitions.ids)
        captured["npcs"] = NPCDefinitions.definitions.toList() to dump(NPCDefinitions.definitions.asList(), NPCDefinitions.ids) { definition ->
            "${definition.hitpoints} ${definition.regenRate} ${definition.huntRange} ${definition.huntMode} ${definition.allowedUnder} ${definition.solid} ${definition.blocksPlayers}"
        }
        captured["objects"] = ObjectDefinitions.definitions.toList() to dump(ObjectDefinitions.definitions.asList(), ObjectDefinitions.ids)
        captured["animations"] = AnimationDefinitions.definitions.toList() to AnimationDefinitions.ids.toSortedMap()
        captured["graphics"] = GraphicDefinitions.definitions.toList() to GraphicDefinitions.ids.toSortedMap()
        captured["interfaces"] = Triple(dumpInterfaces(), InterfaceDefinitions.ids.toSortedMap(), InterfaceDefinitions.componentIds.toSortedMap())
        captured["structs"] = StructDefinitions.definitions.toList() to StructDefinitions.ids.toSortedMap()
        captured["inventories"] = InventoryDefinitions.definitions.toList() to InventoryDefinitions.ids.toSortedMap()
        captured["enums"] = EnumDefinitions.definitions.toList() to EnumDefinitions.ids.toSortedMap()
        captured["tables"] = dumpTables()
        captured["variables"] = dumpVariables()
        captured["areas"] = dumpAreas()
        captured["sounds"] = SoundLoader.instance.definitions.toList() to SoundLoader.instance.ids.toSortedMap()
        captured["fonts"] = FontLoader.instance.definitions.toList() to FontLoader.instance.ids.toSortedMap()
        captured["quick_chat"] = QuickChatPhraseLoader.instance.definitions.toList()
        captured["combat"] = CombatLoader.instance.all.toSortedMap()
        captured["item_on_item"] = ItemOnItemLoader.instance.dump()
        val teleports = ObjectTeleportLoader.instance
        captured["object_teleports"] = teleports.options().sorted().associateWith { option -> teleports.get(option).toSortedMap() }
        val music = MusicTrackLoader.instance
        captured["music_tracks"] = music.tracks.map { track -> track?.let { "${it.id} ${it.name} ${it.indexes} ${it.area.map(::shape)}" } } to
            (0..20000).map { region -> music[Region(region)].map { "${it.id} ${shape(it.area)}" } }
        captured["nav_graph"] = navGraph()
        return captured
    }

    private fun navGraph(): List<String> {
        val graph = NavigationGraphLoader.graph
        val nodes = ArrayList<String>()
        for (node in 0 until graph.nodeCount) {
            val builder = StringBuilder("${graph.tile(node)}:")
            for (edge in graph.edges(node) ?: continue) {
                builder.append(" ${graph.weight(edge)} ${graph.endTile(edge)} ${graph.actions(edge)}")
            }
            nodes.add(builder.toString())
        }
        return nodes
    }

    @Suppress("UNCHECKED_CAST")
    private fun ItemOnItemDefinitions.dump(): Map<String, List<ItemOnItemDefinition>> {
        val field = ItemOnItemDefinitions::class.java.getDeclaredField("definitions")
        field.isAccessible = true
        return (field.get(this) as Map<String, List<ItemOnItemDefinition>>).toSortedMap()
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

    private fun shape(area: Area): String = if (area is Polygon) "Polygon ${area.xPoints.toList()} ${area.yPoints.toList()} ${area.level} ${area.bounds.maxLevel}" else area.toString()
    private fun dumpAreas(): String {
        val builder = StringBuilder()
        val tags = sortedSetOf<String>()
        for (definition in Areas.named.values.sortedBy { it.name }) {
            val area = definition.area
            val shape = shape(area)
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
