package world.gregs.voidps.engine.entity.item

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.flatGroupBy
import world.gregs.voidps.engine.map.area.SpawnArea
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

val floorItemSpawnModule = module {
    single(createdAtStart = true) {
        FloorItemSpawns(get())
    }
}

class FloorItemSpawns(
    private val items: FloorItems
) {

    private lateinit var spawns: Map<Region, List<SpawnArea>>

    private val small = SmallTraversal(TraversalType.Land, false, get())
    private val logger = InlineLogger()

    fun load(region: Region) {
        val areas = spawns[region] ?: return
        for (area in areas) {
            if (area.spawned) {
                continue
            }
            area.spawned = true
            for (spawn in area.spawns) {
                repeat(spawn.limit) {
                    drop(area, spawn)
                }
            }
        }
    }

    private fun drop(area: SpawnArea, spawn: SpawnArea.Spawn) {
        val tile = area.area.random(small)
        if (tile == null) {
            logger.warn { "No free tile in item spawn area ${area.name}" }
            return
        }
        val item = items.add(spawn.name, spawn.amount, tile, revealTicks = 0) ?: return
        item.events.on<FloorItem, Unregistered> {
            delay(area.delay) {
                drop(area, spawn)
            }
        }
    }

    init {
        load()
    }

    fun load() = timedLoad("floor item spawn") {
        val data: Array<SpawnArea> = get<FileLoader>().load(getProperty("floorItemsPath"))
        this.spawns = data.flatGroupBy { it.area.regions }
        data.size
    }


}