package content.skill.firemaking

import content.entity.obj.door.Door.isDoor
import content.skill.summoning.familiarBoost
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactFloorItem
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.stepAway
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Firemaking : Script {

    fun burnable(id: String) = Tables.intOrNull("firemaking.$id.xp") != null

    init {
        itemOnItem("tinderbox*", "*logs*,*_branches") { fromItem, toItem, fromSlot, toSlot ->
            val log = if (burnable(toItem.id)) toItem else fromItem
            val logSlot = if (burnable(toItem.id)) toSlot else fromSlot
            if (!burnable(log.id)) return@itemOnItem
            closeDialogue()
            queue.clearWeak()
            val row = Rows.getOrNull("firemaking.${log.id}") ?: return@itemOnItem
            if (!canLight(log.id, row.int("level"), tile)) {
                return@itemOnItem
            }
            if (inventory.remove(logSlot, log.id)) {
                val floorItem = FloorItems.add(tile, log.id, disappearTicks = 300, owner = this)
                set("recently_dropped", true)
                interactFloorItem(floorItem, "Light")
            }
        }

        itemOnFloorItemOperate("tinderbox*", "*log*") { (target) ->
            lightFire(this, target)
        }

        floorItemOperate("Light") { (target) ->
            lightFire(this, target)
        }
    }

    suspend fun lightFire(
        player: Player,
        floorItem: FloorItem,
        skip: Boolean = player["recently_dropped", false],
    ) {
        val row = Rows.getOrNull("firemaking.${floorItem.id}") ?: return
        player.arriveDelay()
        player.softTimers.start("firemaking")
        player.clear("recently_dropped")
        var first = true
        var fireLit = false
        while (player.awaitDialogues()) {
            val level = row.int("level")
            if (!player.canLight(floorItem.id, level, floorItem, skip)) {
                break
            }
            val remaining = player.remaining("action_delay")
            if (remaining < 0) {
                if (first) {
                    player.message("You attempt to light the logs.", ChatType.Filter)
                    first = false
                }
                player.anim("light_fire")
                player.start("action_delay", 3)
                player.pause(3)
            } else if (remaining > 0) {
                player.pause(remaining)
            }
            val chance = row.intRange("chance")
            if (!Level.success(player.levels.get(Skill.Firemaking) + player.familiarBoost(Skill.Firemaking), chance)) {
                player.anim("light_fire")
                player.start("action_delay", 4)
                player.pause(4)
                continue
            }
            if (!FloorItems.remove(floorItem)) {
                break
            }
            fireLit = true
            player.start("action_delay", 4)
            player.message("The fire catches and the logs begin to burn.", ChatType.Filter)
            player.exp(Skill.Firemaking, row.int("xp") / 10.0)
            spawnFire(player, floorItem.tile, row)
            break
        }
        if (!fireLit) {
            player.start("action_delay", 1)
        }
        player.softTimers.stop("firemaking")
        player.clearAnim()
    }

    fun Player.canLight(log: String, level: Int, item: FloorItem, skipFloorCheck: Boolean): Boolean {
        return canLight(log, level, item.tile) && (skipFloorCheck || FloorItems.at(item.tile).contains(item))
    }

    private fun Player.canLight(log: String, level: Int, tile: Tile): Boolean {
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
        return canLightOn(tile)
    }

    private fun Player.canLightOn(tile: Tile): Boolean {
        val steps: StepValidator = get()
        if (
            GameObjects.getLayer(tile, ObjectLayer.GROUND) != null ||
            doorway(tile) ||
            Areas.get(tile.zone).any { it.tags.contains("bank") } ||
            !steps.canFit(tile, collision, size, blockMove)
        ) {
            message("You can't light a fire here.")
            return false
        }
        return true
    }

    private fun doorway(tile: Tile): Boolean {
        val wall = GameObjects.getLayer(tile, ObjectLayer.WALL) ?: return false
        return wall.def.isDoor()
    }

    fun spawnFire(player: Player, tile: Tile, row: RowDefinition) {
        val colour = row.string("colour")
        val life = 100 + random.nextInt(100)
        val obj = GameObjects.add("fire_$colour", tile, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0, ticks = life)
        FloorItems.add(tile, "ashes", revealTicks = life, disappearTicks = 60, owner = "")
        player.stepAway(obj, tile)
    }
}
