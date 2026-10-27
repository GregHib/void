package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.load.SnapshotCodec
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Polygon
import world.gregs.voidps.type.area.Rectangle
import java.util.IdentityHashMap

object Areas {

    var named: Map<String, AreaDefinition> = Object2ObjectOpenHashMap()
    private var tagged: Map<String, Set<AreaDefinition>> = Object2ObjectOpenHashMap()
    private var areas: Map<Int, Set<AreaDefinition>> = Int2ObjectOpenHashMap()

    val names: Set<String>
        get() = named.keys

    fun getAll() = named.values

    fun getOrNull(name: String): AreaDefinition? = named[name]

    operator fun get(name: String): Area = named[name]?.area ?: AreaDefinition.EMPTY.area

    fun get(zone: Zone): Set<AreaDefinition> = areas[zone.id] ?: emptySet()

    fun tagged(tag: String): Set<AreaDefinition> = tagged[tag] ?: emptySet()

    fun install(state: AreasState) {
        named = state.named
        tagged = state.tagged
        areas = state.areas
    }

    fun load(paths: List<String>): Areas {
        install(parse(paths))
        return this
    }

    internal fun parse(paths: List<String>): AreasState {
        val named = Object2ObjectOpenHashMap<String, AreaDefinition>()
        val tagged = Object2ObjectOpenHashMap<String, MutableSet<AreaDefinition>>()
        val areas = Int2ObjectOpenHashMap<MutableSet<AreaDefinition>>()
        for (path in paths) {
            Config.fileReader(path) {
                val x = IntArrayList()
                val y = IntArrayList()
                while (nextSection()) {
                    val name = section()
                    x.clear()
                    y.clear()
                    var level: Int? = null
                    val tags = ObjectOpenHashSet<String>()
                    val params = Int2ObjectOpenHashMap<Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                    while (nextPair()) {
                        when (val key = key()) {
                            "x" -> while (nextElement()) {
                                x.add(int())
                            }
                            "y" -> while (nextElement()) {
                                y.add(int())
                            }
                            "level" -> level = int()
                            "tags" -> while (nextElement()) {
                                tags.add(string())
                            }
                            else -> params[Params.id(key)] = value()
                        }
                    }
                    require(x.size == y.size) { "Area '$name' must have the same number of x and y values (x=${x.size}, y=${y.size}) in '$path'." }
                    val area: Area = if (x.size <= 2) {
                        if (level == null) {
                            Rectangle(x.min(), y.min(), x.max(), y.max())
                        } else {
                            Cuboid(x.min(), y.min(), x.max(), y.max(), level, level)
                        }
                    } else {
                        Polygon(x.toIntArray(), y.toIntArray(), level ?: 0, level ?: 3)
                    }
                    val definition = if (params.isEmpty()) {
                        AreaDefinition(name = name, area = area, tags = tags, stringId = name)
                    } else {
                        AreaDefinition(name = name, area = area, tags = tags, stringId = name, params = params)
                    }
                    named[name] = definition
                    for (tag in tags) {
                        tagged.getOrPut(tag) { ObjectOpenHashSet(2) }.add(definition)
                    }
                    if (level != null) {
                        for (zone in area.toZones(level)) {
                            areas.getOrPut(zone.id) { ObjectOpenHashSet(2) }.add(definition)
                        }
                    } else {
                        for (lvl in 0..3) {
                            for (zone in area.toZones(lvl)) {
                                areas.getOrPut(zone.id) { ObjectOpenHashSet(2) }.add(definition)
                            }
                        }
                    }
                }
            }
        }
        return AreasState(named, tagged, areas)
    }

    internal fun write(writer: SnapshotWriter, state: AreasState) = with(writer) {
        val definitions = state.named.values.toList()
        val indices = IdentityHashMap<AreaDefinition, Int>(definitions.size)
        int(definitions.size)
        for ((index, definition) in definitions.withIndex()) {
            indices[definition] = index
            string(definition.name)
            area(definition.area)
            list(definition.tags.toList()) { string(it) }
            string(definition.stringId)
            params(definition.params)
        }
        fun indices(set: Set<AreaDefinition>) = IntArray(set.size).also { array ->
            for ((i, definition) in set.withIndex()) {
                array[i] = indices[definition] ?: throw IllegalArgumentException("Area '${definition.name}' isn't named.")
            }
        }
        int(state.tagged.size)
        for ((tag, set) in state.tagged) {
            string(tag)
            ints(indices(set))
        }
        int(state.areas.size)
        for ((zone, set) in state.areas) {
            int(zone)
            ints(indices(set))
        }
    }

    internal fun read(reader: SnapshotReader): AreasState {
        val definitions = ArrayList<AreaDefinition>()
        val named = Object2ObjectOpenHashMap<String, AreaDefinition>()
        repeat(reader.int()) {
            val name = reader.string()
            val area = reader.area()
            val tags = ObjectOpenHashSet<String>()
            repeat(reader.int()) { tags.add(reader.string()) }
            val stringId = reader.string()
            val params = reader.params()
            val definition = if (params == null) {
                AreaDefinition(name = name, area = area, tags = tags, stringId = stringId)
            } else {
                AreaDefinition(name = name, area = area, tags = tags, stringId = stringId, params = params)
            }
            definitions.add(definition)
            named[name] = definition
        }
        val tagged = Object2ObjectOpenHashMap<String, MutableSet<AreaDefinition>>()
        repeat(reader.int()) {
            val tag = reader.string()
            val indices = reader.ints()
            val set = ObjectOpenHashSet<AreaDefinition>(indices.size)
            for (index in indices) {
                set.add(definitions[index])
            }
            tagged[tag] = set
        }
        val areas = Int2ObjectOpenHashMap<MutableSet<AreaDefinition>>()
        repeat(reader.int()) {
            val zone = reader.int()
            val indices = reader.ints()
            val set = ObjectOpenHashSet<AreaDefinition>(indices.size)
            for (index in indices) {
                set.add(definitions[index])
            }
            areas[zone] = set
        }
        return AreasState(named, tagged, areas)
    }

    @TestOnly
    fun set(named: Map<String, AreaDefinition>, tagged: Map<String, Set<AreaDefinition>> = emptyMap(), areas: Map<Int, Set<AreaDefinition>> = emptyMap()) {
        this.named = named
        this.tagged = tagged
        this.areas = areas
    }

    fun clear() {
        named = Object2ObjectOpenHashMap()
        tagged = Object2ObjectOpenHashMap()
        areas = Int2ObjectOpenHashMap()
    }
}

class AreasState(
    val named: Map<String, AreaDefinition>,
    val tagged: Map<String, Set<AreaDefinition>>,
    val areas: Map<Int, Set<AreaDefinition>>,
)

object AreaLoader : Loader<AreasState> {
    override val name = "areas"
    override val files = listOf("map.areas")
    override val codec = object : SnapshotCodec<AreasState> {
        override val schema = 1L

        override fun write(writer: SnapshotWriter, value: AreasState) = Areas.write(writer, value)

        override fun read(reader: SnapshotReader) = Areas.read(reader)
    }

    override fun load(context: LoadContext): AreasState = Areas.parse(context.paths(this))

    override fun install(state: AreasState) = Areas.install(state)
}
