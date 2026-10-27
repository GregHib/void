package content.entity.world.music

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.config.Config
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.AreaSerializer
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Polygon
import world.gregs.voidps.type.area.Rectangle

class MusicTracks {

    private lateinit var trackAreas: Map<Int, List<AreaTrack>>
    private lateinit var ids: Map<String, Int>
    lateinit var tracks: Array<Track?>

    fun get(id: Int): Track? = tracks.getOrNull(id)

    fun get(name: String): Track? = get(ids[name] ?: -1)

    operator fun get(region: Region): List<AreaTrack> = trackAreas[region.id] ?: emptyList()

    fun install(state: MusicState) {
        trackAreas = state.trackAreas
        ids = state.ids
        tracks = state.tracks
    }

    fun load(path: String): MusicTracks {
        install(parse(path))
        return this
    }

    internal fun parse(path: String): MusicState {
        val regions = Int2ObjectOpenHashMap<MutableList<AreaTrack>>(900)
        val ids = Object2IntOpenHashMap<String>(650)
        val tracks = arrayOfNulls<Track>(1000)
        Config.fileReader(path) {
            while (nextSection()) {
                var id = -1
                val stringId = section().trim('"')
                val indexes = mutableListOf<Int>()
                val areas = mutableListOf<Area>()
                while (nextPair()) {
                    when (val key = key()) {
                        "id" -> id = int()
                        // Not sure why a song can have multiple indexes
                        "indexes" -> while (nextElement()) {
                            indexes.add(int())
                        }
                        "areas" -> while (nextElement()) {
                            var region = -1
                            val x = IntArrayList()
                            val y = IntArrayList()
                            var level: Int? = null
                            while (nextEntry()) {
                                when (val k = key()) {
                                    "region" -> region = int()
                                    "x" -> while (nextElement()) {
                                        x.add(int())
                                    }
                                    "y" -> while (nextElement()) {
                                        y.add(int())
                                    }
                                    "level" -> level = int()
                                    else -> throw IllegalArgumentException("Unexpected key: '$k' ${exception()}")
                                }
                            }
                            if (region == -1) {
                                if (x.size <= 2) {
                                    if (level == null) {
                                        areas.add(Rectangle(x.first(), y.first(), x.last(), y.last()))
                                    } else {
                                        areas.add(Cuboid(x.first(), y.first(), x.last(), y.last(), level, level))
                                    }
                                } else {
                                    areas.add(Polygon(x.toIntArray(), y.toIntArray(), level ?: 0, level ?: 4))
                                }
                            } else if (level != null) {
                                areas.add(Region(region).toLevel(level).toCuboid())
                            } else {
                                areas.add(Region(region).toCuboid())
                            }
                        }
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                val track = Track(id, stringId, indexes, areas)
                tracks[id] = track
                require(!ids.containsKey(stringId)) { "Music track with name '$stringId' already found. Index: ${ids.getInt(stringId)}" }
                ids[stringId] = id
                for (area in areas) {
                    for (r in area.toRegions()) {
                        regions.getOrPut(r.id) { ObjectArrayList(1) }.add(AreaTrack(track.id, area))
                    }
                }
            }
        }
        // Prioritise smaller shape checks over larger region checks
        for (entry in regions) {
            entry.value.sortBy { it.area.area }
        }
        return MusicState(regions, ids, tracks)
    }
}

class MusicState(
    val trackAreas: Map<Int, List<AreaTrack>>,
    val ids: Map<String, Int>,
    val tracks: Array<Track?>,
)

object MusicTrackLoader : Loader<MusicState> {
    val instance = MusicTracks()
    override val name = "music_tracks"
    override val file = listOf("map.music")
    override val codec = SnapshotFormat.codec(MusicStateSerializer)

    override fun load(context: LoadContext): MusicState = instance.parse(context.paths(this).single())

    override fun install(state: MusicState) = instance.install(state)
}

private object MusicStateSerializer : KSerializer<MusicState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("content.entity.world.music.MusicState") {
        element("id", Int.serializer().descriptor)
        element("name", String.serializer().descriptor)
        element("indexes", IntArraySerializer().descriptor)
        element("area", AreaSerializer.descriptor)
        element("region", Int.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: MusicState) {
        val present = value.tracks.filterNotNull()
        encoder.encodeInt(value.tracks.size)
        encoder.encodeInt(present.size)
        for (track in present) {
            encoder.encodeInt(track.id)
            encoder.encodeString(track.name)
            encoder.encodeInt(track.indexes.size)
            for (index in track.indexes) {
                encoder.encodeInt(index)
            }
            encoder.encodeInt(track.area.size)
            for (area in track.area) {
                AreaSerializer.serialize(encoder, area)
            }
        }
        encoder.encodeInt(value.trackAreas.size)
        for ((region, list) in value.trackAreas) {
            encoder.encodeInt(region)
            encoder.encodeInt(list.size)
            for (areaTrack in list) {
                val track = value.tracks[areaTrack.id]!!
                encoder.encodeInt(areaTrack.id)
                encoder.encodeInt(track.area.indexOfFirst { it === areaTrack.area })
            }
        }
    }

    override fun deserialize(decoder: Decoder): MusicState {
        val tracks = arrayOfNulls<Track>(decoder.decodeInt())
        val ids = Object2IntOpenHashMap<String>(650, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(decoder.decodeInt()) {
            val id = decoder.decodeInt()
            val name = decoder.decodeString()
            val indexes = IntArray(decoder.decodeInt()) { decoder.decodeInt() }.toMutableList()
            val areas = List(decoder.decodeInt()) { AreaSerializer.deserialize(decoder) }
            tracks[id] = Track(id, name, indexes, areas)
            ids[name] = id
        }
        val size = decoder.decodeInt()
        val regions = Int2ObjectOpenHashMap<MutableList<AreaTrack>>(size, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val region = decoder.decodeInt()
            regions[region] = MutableList(decoder.decodeInt()) {
                val id = decoder.decodeInt()
                AreaTrack(id, tracks[id]!!.area[decoder.decodeInt()])
            }
        }
        return MusicState(regions, ids, tracks)
    }
}
