package world.gregs.voidps.world.interact.entity.player.music

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Region
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import kotlin.collections.set

class MusicTracks {

    private lateinit var tracks: Map<Int, List<Track>>
    private lateinit var trackNames: Map<String, Int>

    fun get(name: String): Int = trackNames.getOrDefault(name, -1)

    operator fun get(region: Region): List<Track> {
        return tracks[region.id] ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["map.music"]): MusicTracks {
        timedLoad("music track") {
            var count = 0
            val tracks = Int2ObjectOpenHashMap<MutableList<Track>>()
            val names = Object2IntOpenHashMap<String>()
            val config = object : YamlReaderConfiguration(2, 2) {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0) {
                        count++
                        value as Map<String, Any>
                        val index = value["index"] as Int
                        names[key] = index
                        for (element in value["areas"] as? List<Map<String, Any>> ?: return) {
                            val area = if (element.containsKey("region")) {
                                val level = element["level"] as? Int ?: -1
                                val region = Region(element["region"] as Int)
                                if (level != -1) {
                                    region.toLevel(level).toCuboid()
                                } else {
                                    region.toCuboid()
                                }
                            } else {
                                Area.fromMap(element, 4)
                            }
                            val track = Track(key, index, area)
                            for (region in area.toRegions()) {
                                tracks.getOrPut(region.id) { ObjectArrayList(1) }.add(track)
                            }
                        }
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            // Prioritise smaller shape checks over larger region checks
            for (entry in tracks) {
                entry.value.sortBy { it.area.area }
            }
            this.tracks = tracks
            this.trackNames = names
            count
        }
        return this
    }

    data class Track(val name: String, val index: Int, val area: Area)
}