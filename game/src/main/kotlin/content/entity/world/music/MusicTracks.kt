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

    private lateinit var tracks: Map<Int, List<Track>>
    private lateinit var trackNames: Map<String, Int>

    fun get(name: String): Int = trackNames.getOrDefault(name, -1)

    operator fun get(region: Region): List<Track> = tracks[region.id] ?: emptyList()

    fun load(path: String): MusicTracks {
        timedLoad("music track") {
            val tracks = Int2ObjectOpenHashMap<MutableList<Track>>(900)
            val names = Object2IntOpenHashMap<String>(650)
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section().trim('"')
                    var index = -1
                    while (nextPair()) {
                        when (val key = key()) {
                            "index" -> index = int()
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
                                val track = Track(stringId, index, area)
                                for (r in area.toRegions()) {
                                    tracks.getOrPut(r.id) { ObjectArrayList(1) }.add(track)
                                }
                            }
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    names[stringId] = index
                }
            }
            // Prioritise smaller shape checks over larger region checks
            for (entry in tracks) {
                entry.value.sortBy { it.area.area }
            }
            this.tracks = tracks
            this.trackNames = names
            names.size
        }
        return this
    }

    data class Track(val name: String, val index: Int, val area: Area)
}
