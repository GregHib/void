import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.client.ui.interact.either
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.awaitWalk
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Level
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Fire
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.inject

val items: FloorItems by inject()
val objects: Objects by inject()

on<FloorItemOption>({ floorItem.def.has("firemaking") && option == "Light" }) { player: Player ->
    if (player.hasEffect("skilling_delay")) {
        return@on
    }
    light(player, Item(floorItem.id), -1, floorItem)
}

on<InterfaceOnInterface>({ either { from, to -> from.lighter && to.burnable } }) { player: Player ->
    if (player.hasEffect("skilling_delay")) {
        return@on
    }
    val log = if (toItem.burnable) toItem else fromItem
    val logSlot = if (toItem.burnable) toSlot else fromSlot
    light(player, log, logSlot, null)
}

fun light(player: Player, log: Item, logSlot: Int, floorItem: FloorItem? = null) {
    player.action(ActionType.FireMaking) {
        val fire: Fire = log.def.getOrNull("firemaking") ?: return@action
        if (!player.canLight(log.id, fire, floorItem?.tile ?: player.tile)) {
            return@action
        }
        try {
            player.message("You attempt to light the logs.", ChatType.Filter)
            if (floorItem == null) {
                player.inventory.remove(logSlot, log.id)
            }
            val floorItem = floorItem ?: items.add(log.id, 1, player.tile, -1, 300, player)
            val delay = 4
            player.setAnimation("light_fire")
            player.start("skilling_delay", delay)
            delay(delay)
            while (!Level.success(player.levels.get(Skill.Firemaking), fire.chance)) {
                player.setAnimation("light_fire")
                delay(delay)
            }
            if (!items.remove(floorItem)) {
                return@action
            }
            player.message("The fire catches and the logs begin to burn.", ChatType.Filter)
            player.exp(Skill.Firemaking, fire.xp)
            spawnFire(player, fire)
        } finally {
            player.clearAnimation()
        }
    }
}

fun Player.canLight(log: String, fire: Fire, tile: Tile): Boolean {
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
    if (objects.getType(tile, 10) != null) {
        message("You can't light a fire here.")
        return false
    }
    return true
}

suspend fun spawnFire(player: Player, fire: Fire) {
    val obj = spawnObject("fire_${fire.colour}", player.tile, type = 10, rotation = 0, ticks = fire.life)
    obj["owner"] = player
    player.awaitWalk(obj)
    player.face(obj)
}

val Item.lighter: Boolean
    get() = id.startsWith("tinderbox")

val Item.burnable: Boolean
    get() = def.has("firemaking")

on<Unregistered>({ it.id.startsWith("fire_") }) { gameObject: GameObject ->
    items.add("ashes", 1, gameObject.tile, 0, 60, gameObject.getOrNull("owner"))
}
