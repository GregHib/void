package world.gregs.voidps.engine.entity.item

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.map.area.SpawnArea
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.utility.get

val floorItemSpawnModule = module {
    single(createdAtStart = true) {
        FloorItemSpawns(get(), get()).run(getProperty("floorItemsPath"))
    }
}

class FloorItemSpawns(
    private val factory: FloorItemFactory,
    private val files: FileLoader
) : TimedLoader<FloorItemSpawns>("floor item spawn") {

    private lateinit var spawns: Map<Region, List<SpawnArea>>

    override fun load(args: Array<out Any?>): FloorItemSpawns {
        val path = args.first() as String
        val spawns: MutableMap<Region, MutableList<SpawnArea>> = mutableMapOf()
        val points: Array<SpawnArea> = files.load(path)
        points.forEach { spawn ->
            for (region in spawn.area.regions) {
                spawns.getOrPut(region) { mutableListOf() }.add(spawn)
            }
            count++
        }
        this.spawns = spawns
        return this
    }

    private val small = SmallTraversal(TraversalType.Land, false, get())
    private val logger = InlineLogger()

    fun load(region: Region) {
        val areas = spawns[region] ?: return
        for (area in areas) {
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
        val item = factory.spawn(spawn.name, spawn.amount, tile, revealTicks = 0) ?: return
        item.events.on<FloorItem, Unregistered> {
            delay(area.delay) {
                drop(area, spawn)
            }
        }
    }
}