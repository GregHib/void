import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.world.spawn.ItemSpawns
import world.gregs.voidps.world.interact.world.spawn.loadItemSpawns

val items: FloorItems by inject()
val spawns: ItemSpawns by inject()
val definitions: ItemDefinitions by inject()

on<Registered> { _: World ->
    loadItemSpawns(items, spawns)
}

on<Command>({ prefix == "reload" && (content == "item defs" || content == "items" || content == "floor items") }) { _: Player ->
    items.clear()
    definitions.load()
    loadItemSpawns(items, spawns)
}

on<Unregistered>({ isSpawnItem(it) }) { floorItem: FloorItem ->
    val spawn = spawns.get(floorItem.tile) ?: return@on
    items.add(floorItem.tile, spawn.id, spawn.amount, spawn.delay)
}

fun isSpawnItem(item: FloorItem): Boolean {
    val spawn = spawns.get(item.tile) ?: return false
    return item.id == spawn.id && item.amount == spawn.amount && item.owner == null
}