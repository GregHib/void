import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.spawn.ItemSpawn
import world.gregs.voidps.engine.utility.inject

val items: FloorItems by inject()

on<Unregistered>({ it.contains("respawn") }) { floorItem: FloorItem ->
    val spawn: ItemSpawn = floorItem["respawn"]
    val item = items.add(spawn.id, spawn.amount, spawn.tile, revealTicks = 0)
    item["respawn"] = spawn
}