package world.gregs.voidps.world.activity.skill.firemaking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnFloorItem
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.client.ui.interact.either
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.contain.clear
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.data.definition.data.Fire
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.engine.suspend.pause

val items: FloorItems by inject()
val objects: Objects by inject()

on<InterfaceOnInterface>({ either { from, to -> from.lighter && to.burnable } }) { player: Player ->
    val log = if (toItem.burnable) toItem else fromItem
    val logSlot = if (toItem.burnable) toSlot else fromSlot
    if (objects.getType(player.tile, 10) != null) {
        player.message("You can't light a fire here.")
        player.clearAnimation()
        return@on
    }
    if (player.remaining("skill_delay") >= 1) {
        return@on
    }
    player.closeDialogue()
    player.queue.clearWeak()
    if (player.inventory[logSlot].id == log.id && player.inventory.clear(logSlot)) {
        val floorItem = items.add(log.id, 1, player.tile, -1, 300, player)
        player.mode = Interact(player, floorItem, FloorItemOption(player, floorItem, "Light"))
    }
}

on<InterfaceOnFloorItem>({ operate && item.lighter && floorItem.def.has("firemaking") }) { player: Player ->
    arriveDelay()
    lightFire(player, floorItem)
}

on<FloorItemOption>({ operate && option == "Light" }) { player: Player ->
    arriveDelay()
    lightFire(player, item)
}

suspend fun PlayerContext.lightFire(
    player: Player,
    floorItem: FloorItem
) {
    if (!floorItem.def.has("firemaking")) {
        return
    }
    player.softTimers.start("firemaking")
    onCancel = {
        player.softTimers.stop("firemaking")
    }
    val log = Item(floorItem.id)
    val fire: Fire = log.def.getOrNull("firemaking") ?: return
    var first = true
    while (awaitDialogues()) {
        if (!player.canLight(log.id, fire, floorItem)) {
            break
        }
        val remaining = player.remaining("skill_delay")
        if (remaining < 0) {
            if (first) {
                player.message("You attempt to light the logs.", ChatType.Filter)
                first = false
            }
            player.setAnimation("light_fire")
            player.start("skill_delay", 4)
            pause(4)
        } else if (remaining > 0) {
            pause(remaining)
        }
        if (Level.success(player.levels.get(Skill.Firemaking), fire.chance) && items.remove(floorItem)) {
            player.message("The fire catches and the logs begin to burn.", ChatType.Filter)
            player.exp(Skill.Firemaking, fire.xp)
            spawnFire(player, floorItem.tile, fire)
            break
        }
    }
    player.start("skill_delay", 1)
}

fun Player.canLight(log: String, fire: Fire, item: FloorItem): Boolean {
    if (log.endsWith("branches") && !inventory.contains("tinderbox_dungeoneering")) {
        message("You don't have the required items to light this.")
        return false
    }
    if (log.endsWith("logs") && !inventory.contains("tinderbox")) {
        message("You don't have the required items to light this.")
        return false
    }
    if (!has(Skill.Firemaking, fire.level, true)) {
        return false
    }
    if (objects.getType(item.tile, 10) != null) {
        message("You can't light a fire here.")
        return false
    }
    if (!items[item.tile].contains(item)) {
        return false
    }
    return true
}

val directions = listOf(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH)

fun spawnFire(player: Player, tile: Tile, fire: Fire) {
    val obj = spawnObject("fire_${fire.colour}", tile, type = 10, rotation = 0, ticks = fire.life)
    val interact = player.mode as Interact
    for (dir in directions) {
        if (interact.canStep(dir.delta.x, dir.delta.y)) {
            player.steps.queueStep(tile.add(dir))
            break
        }
    }
    player["face_entity"] = obj
}

val Item.lighter: Boolean
    get() = id.startsWith("tinderbox")

val Item.burnable: Boolean
    get() = def.has("firemaking")

val players: Players by inject()

on<Unregistered>({ it.id.startsWith("fire_") }) { gameObject: GameObject ->
    val ownerName = gameObject.owner
    val owner = if (ownerName != null) players.get(ownerName) else null
    items.add("ashes", 1, gameObject.tile, 0, 60, owner)
}
