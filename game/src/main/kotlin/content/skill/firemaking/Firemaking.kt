package content.skill.firemaking

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactFloorItem
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Firemaking : Script {

    val directions = listOf(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH)

    fun burnable(id: String) = EnumDefinitions.intOrNull("firemaking_xp", id) != null

    init {
        itemOnItem("tinderbox*", "*logs*") { fromItem, toItem, fromSlot, toSlot ->
            val log = if (burnable(toItem.id)) toItem else fromItem
            val logSlot = if (burnable(toItem.id)) toSlot else fromSlot
            closeDialogue()
            queue.clearWeak()
            if (inventory.remove(logSlot, log.id)) {
                val floorItem = FloorItems.add(tile, log.id, disappearTicks = 300, owner = this)
                interactFloorItem(floorItem, "Light")
            }
        }

        itemOnFloorItemOperate("tinderbox*", "*log*") { (target) ->
            if (burnable(target.id)) {
                arriveDelay()
                lightFire(this, target)
            }
        }

        floorItemOperate("Light") { (target) ->
            arriveDelay()
            lightFire(this, target)
        }
    }

    suspend fun lightFire(
        player: Player,
        floorItem: FloorItem,
    ) {
        if (!burnable(floorItem.id)) {
            return
        }
        player.softTimers.start("firemaking")
        val log = Item(floorItem.id)
        var first = true
        while (player.awaitDialogues()) {
            val level = EnumDefinitions.int("firemaking_level", log.id)
            if (!player.canLight(log.id, level, floorItem)) {
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
                player.pause(4)
            } else if (remaining > 0) {
                player.pause(remaining)
            }
            val chanceMin = EnumDefinitions.int("firemaking_chance_min", log.id)
            val chanceMax = EnumDefinitions.int("firemaking_chance_max", log.id)
            if (Level.success(player.levels.get(Skill.Firemaking), chanceMin..chanceMax) && FloorItems.remove(floorItem)) {
                player.message("The fire catches and the logs begin to burn.", ChatType.Filter)
                player.exp(Skill.Firemaking, EnumDefinitions.int("firemaking_xp", log.id) / 10.0)
                spawnFire(player, floorItem.tile, log.id)
                break
            }
        }
        player.start("action_delay", 1)
        player.softTimers.stop("firemaking")
    }

    fun Player.canLight(log: String, level: Int, item: FloorItem): Boolean {
        if (log.endsWith("branches") && !inventory.contains("tinderbox_dungeoneering")) {
            message("You don't have the required items to light this.")
            return false
        }
        if (log.endsWith("logs") && !inventory.contains("tinderbox")) {
            message("You don't have the required items to light this.")
            return false
        }
        if (!has(Skill.Firemaking, level, true)) {
            return false
        }
        if (GameObjects.getLayer(item.tile, ObjectLayer.GROUND) != null) {
            message("You can't light a fire here.")
            return false
        }
        return FloorItems.at(item.tile).contains(item)
    }

    fun spawnFire(player: Player, tile: Tile, id: String) {
        val colour = EnumDefinitions.string("firemaking_colour", id)
        val life = EnumDefinitions.int("firemaking_life", id)
        val obj = GameObjects.add("fire_$colour", tile, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0, ticks = life)
        FloorItems.add(tile, "ashes", revealTicks = life, disappearTicks = 60, owner = "")
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
