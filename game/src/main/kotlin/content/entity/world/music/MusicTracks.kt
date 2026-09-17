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

class MusicTracks {

    private lateinit var trackAreas: Map<Int, List<AreaTrack>>
    private lateinit var ids: Map<String, Int>
    lateinit var tracks: Array<Track?>

    fun get(id: Int): Track? = tracks.getOrNull(id)

    fun get(name: String): Track? = get(ids[name] ?: -1)

    operator fun get(region: Region): List<AreaTrack> = trackAreas[region.id] ?: emptyList()

    fun load(path: String): MusicTracks {
        timedLoad("music track") {
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
            this.trackAreas = regions
            this.ids = ids
            this.tracks = tracks
            ids.size
        }
        return this
    }
}
