package world.gregs.voidps.world.interact.world.map

import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.area.Polygon
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.region.Region

val musicModule = module {
    single(createdAtStart = true) { MusicTracks(getProperty("musicPath"), get()) }
}

class MusicTracks(
    private val path: String,
    private val files: FileLoader
) {

    private lateinit var tracks: Map<Region, Set<Track>>

    init {
        load()
    }

    fun load() {
        tracks = load(files.load(path))
    }

    operator fun get(region: Region): Set<Track> {
        return tracks[region] ?: emptySet()
    }

    private fun load(data: Map<String, Map<String, Any>>): Map<Region, Set<Track>> {
        val map = mutableMapOf<Region, MutableSet<Track>>()
        for ((_, m) in data) {
            val index = m["index"] as Int
            val areas = (m["areas"] as List<Map<String, List<Int>>>).map {
                val x = (it["x"] as List<Int>).toIntArray()
                val y = (it["y"] as List<Int>).toIntArray()
                val plane = it["plane"] as? Int ?: 0
                if (x.size <= 2) {
                    Rectangle(x.first(), y.first(), x.last(), y.last(), plane)
                } else {
                    Polygon(x, y, plane)
                }
            }
            for (area in areas) {
                val track = Track(index, area)
                for (region in area.regions) {
                    map.getOrPut(region) { mutableSetOf() }.add(track)
                }
            }
        }
        return map
    }

    data class Track(val index: Int, val area: Area)
}