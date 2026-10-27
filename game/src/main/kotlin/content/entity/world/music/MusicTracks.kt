package content.entity.world.music

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.load.SnapshotCodec
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter
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

    internal fun write(writer: SnapshotWriter, state: MusicState) = with(writer) {
        val tracks = state.tracks
        val trackAreas = state.trackAreas
        val present = tracks.filterNotNull()
        int(tracks.size)
        int(present.size)
        for (track in present) {
            int(track.id)
            string(track.name)
            ints(track.indexes.toIntArray())
            list(track.area) { area(it) }
        }
        int(trackAreas.size)
        for ((region, list) in trackAreas) {
            int(region)
            list(list) { areaTrack ->
                val track = tracks[areaTrack.id]!!
                int(areaTrack.id)
                int(track.area.indexOfFirst { it === areaTrack.area })
            }
        }
    }

    internal fun read(reader: SnapshotReader): MusicState {
        val tracks = arrayOfNulls<Track>(reader.int())
        val ids = Object2IntOpenHashMap<String>(650, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(reader.int()) {
            val id = reader.int()
            val name = reader.string()
            val indexes = reader.ints().toMutableList()
            val areas = reader.list { reader.area() }
            tracks[id] = Track(id, name, indexes, areas)
            ids[name] = id
        }
        val size = reader.int()
        val regions = Int2ObjectOpenHashMap<MutableList<AreaTrack>>(size, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val region = reader.int()
            regions[region] = reader.list {
                val id = reader.int()
                AreaTrack(id, tracks[id]!!.area[reader.int()])
            }
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
    override val codec = object : SnapshotCodec<MusicState> {
        override val schema = 1L

        override fun write(writer: SnapshotWriter, value: MusicState) = instance.write(writer, value)

        override fun read(reader: SnapshotReader) = instance.read(reader)
    }

    override fun load(context: LoadContext): MusicState = instance.parse(context.paths(this).single())

    override fun install(state: MusicState) = instance.install(state)
}
