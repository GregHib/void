package world.gregs.voidps.engine.map.area

import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.flatGroupBy
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

val areasModule = module {
    single(createdAtStart = true) { Areas(get(), get()).load() }
}

class Areas(
    private val npcs: NPCs?,
    private val items: FloorItems?
) {

    private var named: Map<String, MapArea> = mutableMapOf()
    private var tagged: Map<String, Set<MapArea>> = mutableMapOf()
    private lateinit var spawns: Map<Region, List<MapArea>>
    private val respawns = mutableMapOf<Entity, EventHandler>()

    fun load(region: Region) {
        if (npcs == null || items == null) {
            return
        }
        val areas = spawns[region] ?: return
        for (area in areas) {
            if (area.loaded) {
                continue
            }
            area.loaded = true
            for (spawn in area.npcs) {
                repeat(spawn.limit) {
                    npcs.add(spawn.name, area.area, spawn.direction, spawn.delay)
                }
            }
            for (spawn in area.items) {
                repeat(spawn.limit) {
                    drop(area, spawn)
                }
            }
        }
    }

    fun clear() {
        if (npcs == null || items == null) {
            return
        }
        respawns.forEach { (entity, handler) ->
            entity.events.remove(handler)
        }
        respawns.clear()
        npcs.forEach { npcs.remove(it) }
        items.clear()
    }

    private fun drop(area: MapArea, spawn: MapArea.Spawn) {
        val item = items?.add(spawn.name, spawn.amount, area.area, revealTicks = 0) ?: return
        respawns[item] = item.events.on<FloorItem, Unregistered> {
            delay(spawn.delay) {
                drop(area, spawn)
            }
        }
    }

    operator fun get(name: String): MapArea? {
        return named[name]
    }

    fun getValue(name: String): MapArea {
        return named[name] ?: empty
    }

    fun getTagged(tag: String): Set<MapArea> {
        return tagged[tag] ?: emptySet()
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("areaPath")): Areas {
        timedLoad("map area") {
            val data: Map<String, Map<String, Any>> = storage.load(path)
            val areas = data.mapValues { (key, value) -> toArea(key, value) }

            val tagged = mutableMapOf<String, MutableSet<MapArea>>()
            for (key in data.keys) {
                val area = areas.getValue(key)
                for (tag in area.tags) {
                    tagged.getOrPut(tag) { mutableSetOf() }.add(area)
                }
            }
            this.named = areas
            this.tagged = tagged
            this.spawns = areas.values.toTypedArray().flatGroupBy { it.area.toRegions() }
            areas.size
        }
        return this
    }

    fun getAll() = named.values

    private fun toArea(name: String, map: Map<String, Any>): MapArea {
        val area = map["area"] as Map<String, Any>
        val x = area["x"] as List<Int>
        val y = area["y"] as List<Int>
        val plane = area["plane"] as? Int ?: 0
        val shape = when {
            x.size <= 2 -> Cuboid(x.first(), y.first(), x.last(), y.last(), plane)
            else -> {
                Polygon(x.toIntArray(), y.toIntArray(), plane)
            }
        }
        return MapArea(
            name = name,
            area = shape,
            tags = (map["tags"] as? List<String>)?.toSet() ?: emptySet(),
            npcs = toSpawn(map["npcs"] as? List<Map<String, Any>>),
            items = toSpawn(map["items"] as? List<Map<String, Any>>)
        )
    }

    private fun toSpawn(data: List<Map<String, Any>>?): List<MapArea.Spawn> {
        val list = mutableListOf<MapArea.Spawn>()
        for (map in data ?: return emptyList()) {
            list.add(MapArea.Spawn(
                name = map["name"] as String,
                weight = map["weight"] as? Int ?: 1,
                limit = map["limit"] as? Int ?: 1,
                amount = map["amount"] as? Int ?: 1,
                delay = map["delay"] as? Int ?: 60,
                direction = Direction.valueOf(map["direction"] as? String ?: "NONE")
            ))
        }
        return list
    }

    companion object {
        private val empty = MapArea("", Rectangle(0, 0, 0, 0), emptySet(), emptyList(), emptyList())
    }
}