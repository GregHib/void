package world.gregs.voidps.world.interact.entity.player.music

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.koin.dsl.module
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.FastUtilConfiguration
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.emptyList
import kotlin.collections.getOrPut
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.sortBy

val musicModule = module {
    single(createdAtStart = true) { MusicTracks(get()) }
}

class MusicTracks(private val parser: YamlParser) {

    private lateinit var tracks: Map<Int, List<Track>>
    private lateinit var trackNames: Map<String, Int>

    fun get(name: String): Int = trackNames.getOrDefault(name, -1)

    init {
        load()
    }

    operator fun get(region: Region): List<Track> {
        return tracks[region.id] ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    fun load() = timedLoad("music track") {
        var count = 0
        val tracks = Int2ObjectOpenHashMap<MutableList<Track>>()
        val names = Object2IntOpenHashMap<String>()
        val config = object : FastUtilConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                if (indent == 0) {
                    count++
                    value as Map<String, Any>
                    val index = value["index"] as Int
                    names[key] = index
                    for (element in value["areas"] as? List<Map<String, Any>> ?: return) {
                        val area = if (element.containsKey("region")) {
                            val plane = element["plane"] as? Int ?: -1
                            val region = Region(element["region"] as Int)
                            if (plane != -1) {
                                region.toPlane(plane).toCuboid()
                            } else {
                                region.toCuboid()
                            }
                        } else {
                            Area.fromMap(element, 4)
                        }
                        val track = Track(key, index, area)
                        for (region in area.toRegions()) {
                            tracks.getOrPut(region.id) { ObjectArrayList() }.add(track)
                        }
                    }
                } else {
                    super.set(map, key, value, indent, parentMap)
                }
            }
        }
        parser.load<Any>(getProperty("musicPath"), config)
        // Prioritise smaller shape checks over larger region checks
        for (entry in tracks) {
            entry.value.sortBy { it.area.area }
        }
        this.tracks = tracks
        this.trackNames = names
        count
    }

    data class Track(val name: String, val index: Int, val area: Area)
}