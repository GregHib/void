package content.entity.world.music

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Polygon
import world.gregs.voidps.type.area.Rectangle
import kotlin.collections.set

class MusicTracks {

    private lateinit var trackAreas: Map<Int, List<Track>>
    private lateinit var ids: Map<String, Int>
    lateinit var tracks: Array<Track?>

    fun get(id: Int): Track? = tracks.getOrNull(id)

    fun get(name: String): Track? = get(ids[name] ?: -1)

    operator fun get(region: Region): List<Track> = trackAreas[region.id] ?: emptyList()

    fun load(path: String): MusicTracks {
        timedLoad("music track") {
            val areas = Int2ObjectOpenHashMap<MutableList<Track>>(900)
            val ids = Object2IntOpenHashMap<String>(650)
            val tracks = arrayOfNulls<Track>(1000)
            Config.fileReader(path) {
                while (nextSection()) {
                    var id = -1
                    val stringId = section().trim('"')
                    val indexes = mutableListOf<Int>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
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
                                val area = if (region == -1) {
                                    if (x.size <= 2) {
                                        if (level == null) {
                                            Rectangle(x.first(), y.first(), x.last(), y.last())
                                        } else {
                                            Cuboid(x.first(), y.first(), x.last(), y.last(), level, level)
                                        }
                                    } else {
                                        Polygon(x.toIntArray(), y.toIntArray(), level ?: 0, level ?: 4)
                                    }
                                } else {
                                    if (level != null) {
                                        Region(region).toLevel(level).toCuboid()
                                    } else {
                                        Region(region).toCuboid()
                                    }
                                }
                                var track = tracks[id]
                                if (track == null) {
                                    track = Track(id, stringId, indexes.firstOrNull() ?: -1, area)
                                    tracks[id] = track
                                }
                                for (r in area.toRegions()) {
                                    areas.getOrPut(r.id) { ObjectArrayList(1) }.add(track)
                                }
                            }
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    if (tracks[id] == null) {
                        tracks[id] = Track(id, stringId, indexes.firstOrNull() ?: -1)
                    }
                    assert(!ids.containsKey(stringId)) { "Music track with name '$stringId' already found. Index: ${ids.getInt(stringId)}" }
                    ids[stringId] = id
                }
            }
            // Prioritise smaller shape checks over larger region checks
            for (entry in areas) {
                entry.value.sortBy { it.area?.area }
            }
            this.trackAreas = areas
            this.ids = ids
            this.tracks = tracks
            ids.size
        }
        return this
    }

}
