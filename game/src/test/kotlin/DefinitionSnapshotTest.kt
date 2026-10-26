import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Snapshot
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
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
        is Set<*> -> value.joinToString(",", "Set[", "]") { render(it) }
        is Collection<*> -> value.joinToString(",", "List[", "]") { render(it) }
        else -> "${value.javaClass.simpleName}($value)"
    }
}
