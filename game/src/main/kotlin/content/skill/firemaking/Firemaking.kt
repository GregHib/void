package content.skill.firemaking

import content.entity.obj.door.Door.isDoor
import content.entity.player.dialogue.type.statement
import content.quest.miniquest.barbarian_training.BarbarianTraining
import content.quest.miniquest.barbarian_training.barbarianTaskDone
import content.quest.miniquest.barbarian_training.barbarianTrainingStarted
import content.skill.summoning.familiarBoost
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactFloorItem
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
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
import world.gregs.voidps.engine.entity.obj.stepAway
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.type.Tile

class Firemaking : Script {

    fun burnable(id: String) = Tables.intOrNull("firemaking.$id.xp") != null

    init {
        itemOnItem("tinderbox*", "*logs*") { fromItem, toItem, fromSlot, toSlot ->
            val log = if (burnable(toItem.id)) toItem else fromItem
            val logSlot = if (burnable(toItem.id)) toSlot else fromSlot
            closeDialogue()
            queue.clearWeak()
            if (!canLightOn(tile)) {
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

        itemOnItem(Tables.get("firemaking_bows").rows().joinToString(",") { it.rowId }, "*logs") { fromItem, toItem, fromSlot, toSlot ->
            val bow = if (burnable(toItem.id)) fromItem else toItem
            val log = if (burnable(toItem.id)) toItem else fromItem
            val logSlot = if (burnable(toItem.id)) toSlot else fromSlot
            closeDialogue()
            queue.clearWeak()
            if (!barbarianTrainingStarted("bow")) {
                statement(BarbarianTraining.NOT_STARTED_MESSAGE)
                return@itemOnItem
            }
            if (!canLightOn(tile) || Rows.getOrNull("firemaking.${log.id}") == null) {
                return@itemOnItem
            }
            if (inventory.remove(logSlot, log.id)) {
                val floorItem = FloorItems.add(tile, log.id, disappearTicks = 300, owner = this)
                lightFire(this, floorItem, skip = true, bow = bow.id)
            }
        }

        itemOnItem("crystal_bow*,new_crystal_bow", "*logs") { _, _ ->
            message("The bow resists all attempts to light the fire. It seems that the sentient tools of the elves don't approve of you burning down forests.")
        }

        itemOnItem("dark_bow", "*logs") { _, _ ->
            message("The innate darkness of the bow sucks all the heat from your firemaking attempt. You realise that this type of bow is useless for firelighting.")
        }

        floorItemOperate("Light") { (target) ->
            lightFire(this, target)
        }
    }

    suspend fun lightFire(
        player: Player,
        floorItem: FloorItem,
        skip: Boolean = player["recently_dropped", false],
        bow: String? = null,
    ) {
        val row = Rows.getOrNull("firemaking.${floorItem.id}") ?: return
        player.arriveDelay()
        player.softTimers.start("firemaking")
        player.clear("recently_dropped")
        val log = Item(floorItem.id)
        var first = true
        while (player.awaitDialogues()) {
            // Lighting with a bow takes 20 more levels than a tinderbox
            val level = row.int("level") + if (bow != null) 20 else 0
            if (!player.canLight(log.id, level, floorItem, skip, bow)) {
                break
            }
            val remaining = player.remaining("action_delay")
            if (remaining < 0) {
                if (first) {
                    player.message("You attempt to light the logs.", ChatType.Filter)
                    first = false
                }
                player.anim(if (bow != null) Tables.string("firemaking_bows.$bow.anim") else "light_fire")
                player.start("action_delay", 4)
                player.pause(4)
            } else if (remaining > 0) {
                player.pause(remaining)
            }
            val chance = row.intRange("chance")
            if (Level.success(player.levels.get(Skill.Firemaking) + player.familiarBoost(Skill.Firemaking), chance) && FloorItems.remove(floorItem)) {
                player.message("The fire catches and the logs begin to burn.", ChatType.Filter)
                player.exp(Skill.Firemaking, row.int("xp") / 10.0)
                spawnFire(player, floorItem.tile, row)
                if (bow != null && log.id == "oak_logs") {
                    player.barbarianTaskDone("bow")
                }
                break
            }
        }
        player.start("action_delay", 1)
        player.softTimers.stop("firemaking")
        player.clearAnim()
    }

    fun Player.canLight(log: String, level: Int, item: FloorItem, skipFloorCheck: Boolean, bow: String? = null): Boolean {
        if (log.endsWith("branches") && !inventory.contains("tinderbox_dungeoneering")) {
            message("You don't have the required items to light this.")
            return false
        }
        if (bow != null && !inventory.contains(bow)) {
            message("You don't have the required items to light this.")
            return false
        }
        if (bow == null && log.endsWith("logs") && !inventory.contains("tinderbox")) {
            message("You don't have the required items to light this.")
            return false
        }
        if (!has(Skill.Firemaking, level, true)) {
            return false
        }
        if (!canLightOn(item.tile)) {
            return false
        }
        return skipFloorCheck || FloorItems.at(item.tile).contains(item)
    }

    private fun Player.canLightOn(tile: Tile): Boolean {
        if (GameObjects.getLayer(tile, ObjectLayer.GROUND) != null || GameObjects.getLayer(tile, ObjectLayer.GROUND_DECORATION) != null || doorway(tile)) {
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
        val life = row.int("life")
        val obj = GameObjects.add("fire_$colour", tile, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0, ticks = life)
        FloorItems.add(tile, "ashes", revealTicks = life, disappearTicks = 60, owner = "")
        player.stepAway(obj, tile)
    }
}
