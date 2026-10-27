package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.serialization.ParamsSerializer
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.AreaSerializer
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
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
    override val codec = SnapshotFormat.codec(AreasStateSerializer)

    override fun load(context: LoadContext): AreasState = Areas.parse(context.paths(this))

    override fun install(state: AreasState) = Areas.install(state)
}

private object AreasStateSerializer : KSerializer<AreasState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.definition.AreasState") {
        element("name", String.serializer().descriptor)
        element("area", AreaSerializer.descriptor)
        element("tags", String.serializer().descriptor)
        element("stringId", String.serializer().descriptor)
        element("params", ParamsSerializer.descriptor)
        element("tagged", IntArraySerializer().descriptor)
        element("areas", IntArraySerializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: AreasState) {
        val definitions = value.named.values.toList()
        val indices = IdentityHashMap<AreaDefinition, Int>(definitions.size)
        encoder.encodeInt(definitions.size)
        for ((index, definition) in definitions.withIndex()) {
            indices[definition] = index
            encoder.encodeString(definition.name)
            AreaSerializer.serialize(encoder, definition.area)
            encoder.encodeInt(definition.tags.size)
            for (tag in definition.tags) {
                encoder.encodeString(tag)
            }
            encoder.encodeString(definition.stringId)
            ParamsSerializer.serialize(encoder, definition.params)
        }
        fun indices(encoder: Encoder, set: Set<AreaDefinition>) {
            encoder.encodeInt(set.size)
            for (definition in set) {
                encoder.encodeInt(indices[definition] ?: throw IllegalArgumentException("Area '${definition.name}' is not named."))
            }
        }
        encoder.encodeInt(value.tagged.size)
        for ((tag, set) in value.tagged) {
            encoder.encodeString(tag)
            indices(encoder, set)
        }
        encoder.encodeInt(value.areas.size)
        for ((zone, set) in value.areas) {
            encoder.encodeInt(zone)
            indices(encoder, set)
        }
    }

    override fun deserialize(decoder: Decoder): AreasState {
        val definitions = ArrayList<AreaDefinition>()
        val named = Object2ObjectOpenHashMap<String, AreaDefinition>()
        repeat(decoder.decodeInt()) {
            val name = decoder.decodeString()
            val area = AreaSerializer.deserialize(decoder)
            val tags = ObjectOpenHashSet<String>()
            repeat(decoder.decodeInt()) { tags.add(decoder.decodeString()) }
            val stringId = decoder.decodeString()
            val params = ParamsSerializer.deserialize(decoder)
            val definition = if (params == null) {
                AreaDefinition(name = name, area = area, tags = tags, stringId = stringId)
            } else {
                AreaDefinition(name = name, area = area, tags = tags, stringId = stringId, params = params)
            }
            definitions.add(definition)
            named[name] = definition
        }
        fun set(decoder: Decoder): ObjectOpenHashSet<AreaDefinition> {
            val size = decoder.decodeInt()
            val set = ObjectOpenHashSet<AreaDefinition>(size)
            repeat(size) { set.add(definitions[decoder.decodeInt()]) }
            return set
        }
        val tagged = Object2ObjectOpenHashMap<String, MutableSet<AreaDefinition>>()
        repeat(decoder.decodeInt()) {
            val tag = decoder.decodeString()
            tagged[tag] = set(decoder)
        }
        val areas = Int2ObjectOpenHashMap<MutableSet<AreaDefinition>>()
        repeat(decoder.decodeInt()) {
            val zone = decoder.decodeInt()
            areas[zone] = set(decoder)
        }
        return AreasState(named, tagged, areas)
    }
}
