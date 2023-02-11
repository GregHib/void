import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.world.spawn.ItemSpawn

val items: FloorItems by inject()

on<Unregistered>({ it.contains("respawn") }) { floorItem: FloorItem ->
    val spawn: ItemSpawn = floorItem["respawn"]
    World.timer(spawn.delay) {
        val item = items.add(spawn.id, spawn.amount, spawn.tile, revealTicks = 0)
        item["respawn"] = spawn
    }
}