package world.gregs.voidps.world.interact.entity.player.music

import org.koin.dsl.module
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Polygon
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.encode.playMusicTrack
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

val musicModule = module {
    single(createdAtStart = true) { MusicTracks() }
}

class MusicTracks {

    private lateinit var tracks: Map<Region, List<Track>>

    init {
        load()
    }

    operator fun get(region: Region): List<Track> {
        return tracks[region] ?: emptyList()
    }

    fun load() = timedLoad("music track") {
        val data: Map<String, Map<String, Any>> = get<FileLoader>().load(getProperty("musicPath"))
        val map = mutableMapOf<Region, MutableList<Track>>()
        for ((_, m) in data) {
            val index = m["index"] as Int
            val areas = (m["areas"] as List<Map<String, List<Int>>>).map {
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
                    val plane = it["plane"] as? Int ?: 0
                    if (x.size <= 2) {
                        Cuboid(x.first(), y.first(), x.last(), y.last(), plane)
                    } else {
                        Polygon(x, y, plane)
                    }
                }
            }
            for (area in areas) {
                val track = Track(index, area)
                for (region in area.toRegions()) {
                    val tracks = map.getOrPut(region) { mutableListOf() }
                    tracks.add(track)
                    // Prioritise smaller shape checks over larger region checks
                    tracks.sortBy { it.area.area }
                }
            }
        }
        this.tracks = map
        data.size
    }

    data class Track(val index: Int, val area: Area)
}

fun Player.playTrack(trackIndex: Int) {
    val enumDefs: EnumDecoder = get()
    playMusicTrack(enumDefs.get(1351).getInt(trackIndex))
    interfaces.sendText("music_player", "currently_playing", enumDefs.get(1345).getString(trackIndex))
    this["current_track"] = trackIndex
}