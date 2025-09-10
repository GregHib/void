package content.skill.firemaking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.interact.itemOnFloorItemOperate
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.data.Fire
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.floorItemOperate
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.event.Script
@Script
class Firemaking {

    val floorItems: FloorItems by inject()
    val objects: GameObjects by inject()
    
    val directions = listOf(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH)
    
    val Item.burnable: Boolean
        get() = def.contains("firemaking")

    init {
        itemOnItem("tinderbox*", "*logs*") { player ->
            val log = if (toItem.burnable) toItem else fromItem
            val logSlot = if (toItem.burnable) toSlot else fromSlot
            player.closeDialogue()
            player.queue.clearWeak()
            if (player.inventory.remove(logSlot, log.id)) {
                val floorItem = floorItems.add(player.tile, log.id, disappearTicks = 300, owner = player)
                player.mode = Interact(player, floorItem, FloorItemOption(player, floorItem, "Light"))
            }
        }

        itemOnFloorItemOperate("tinderbox*", "*log*") {
            if (floorItem.def.contains("firemaking")) {
                lightFire(player, floorItem)
            }
        }

        floorItemOperate("Light") {
            lightFire(player, target)
        }

    }

    suspend fun Interaction<Player>.lightFire(
        player: Player,
        floorItem: FloorItem,
    ) {
        if (!floorItem.def.contains("firemaking")) {
            return
        }
        player.softTimers.start("firemaking")
        val log = Item(floorItem.id)
        val fire: Fire = log.def.getOrNull("firemaking") ?: return
        var first = true
        while (awaitDialogues()) {
            if (!player.canLight(log.id, fire, floorItem)) {
                break
            }
            val remaining = player.remaining("action_delay")
            if (remaining < 0) {
                if (first) {
                    player.message("You attempt to light the logs.", ChatType.Filter)
                    first = false
                }
                player.anim("light_fire")
                player.start("action_delay", 4)
                pause(4)
            } else if (remaining > 0) {
                pause(remaining)
            }
            if (Level.success(player.levels.get(Skill.Firemaking), fire.chance) && floorItems.remove(floorItem)) {
                player.message("The fire catches and the logs begin to burn.", ChatType.Filter)
                player.exp(Skill.Firemaking, fire.xp)
                spawnFire(player, floorItem.tile, fire)
                break
            }
        }
        player.start("action_delay", 1)
        player.softTimers.stop("firemaking")
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
        if (objects.getLayer(item.tile, ObjectLayer.GROUND) != null) {
            message("You can't light a fire here.")
            return false
        }
        return floorItems[item.tile].contains(item)
    }
    
    fun spawnFire(player: Player, tile: Tile, fire: Fire) {
        val obj = objects.add("fire_${fire.colour}", tile, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0, ticks = fire.life)
        floorItems.add(tile, "ashes", revealTicks = fire.life, disappearTicks = 60, owner = "")
        val interact = player.mode as Interact
        for (dir in directions) {
            if (interact.canStep(dir.delta.x, dir.delta.y)) {
                player.steps.queueStep(tile.add(dir))
                break
            }
        }
        player["face_entity"] = obj
    }
    
}
