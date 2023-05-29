package world.gregs.voidps.world.interact.entity.player.music

import org.koin.dsl.module
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Polygon
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad

val musicModule = module {
    single(createdAtStart = true) { MusicTracks() }
}

class MusicTracks {

    private lateinit var tracks: Map<Region, List<Track>>
    private lateinit var trackNames: Map<String, Int>

    fun get(name: String): Int = trackNames.getOrDefault(name, -1)

    init {
        load()
    }

    operator fun get(region: Region): List<Track> {
        return tracks[region] ?: emptyList()
    }

    fun load() = timedLoad("music track") {
        val data: Map<String, Map<String, Any>> = get<FileStorage>().load(getProperty("musicPath"))
        val map = mutableMapOf<Region, MutableList<Track>>()
        val names = mutableMapOf<String, Int>()
        for ((name, m) in data) {
            val index = m["index"] as Int
            val areas = (m["areas"] as? List<Map<String, List<Int>>>)?.map {
                if (it.containsKey("region")) {
                    val plane = it["plane"] as? Int ?: -1
                    val region = Region(it["region"] as Int)
                    if (plane != -1) {
                        region.toPlane(plane).toCuboid()
                    } else {
                        region.toCuboid()
                    }
                } else {
                    val x = (it["x"] as List<Int>).toIntArray()
                    val y = (it["y"] as List<Int>).toIntArray()
                    val plane = it["plane"] as? Int
                    if (x.size <= 2) {
                        Cuboid(x.first(), y.first(), x.last(), y.last(), plane ?: 0, plane ?: 4)
                    } else {
                        Polygon(x, y, plane ?: 0, plane ?: 4)
                    }
                }
            } ?: emptyList()
            for (area in areas) {
                val track = Track(name, index, area)
                for (region in area.toRegions()) {
                    val tracks = map.getOrPut(region) { mutableListOf() }
                    tracks.add(track)
                    // Prioritise smaller shape checks over larger region checks
                    tracks.sortBy { it.area.area }
                }
            }
            names[name] = index
        }
        this.tracks = map
        this.trackNames = names
        data.size
    }

    data class Track(val name: String, val index: Int, val area: Area)
}