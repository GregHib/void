package content.area.kandarin.baxtorian_falls

import content.entity.obj.door.Door
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class GlarialsTomb : Script {
    init {
        objectOperate("Read", "glarials_tombstone") {
            open("glarials_tombstone_inscription")
            val lines = listOf(
                "Here lies Glarial, wife of Baxtorian,",
                "true friend of nature in life and death.",
                "",
                "May she now rest knowing",
                "only visitors with peaceful intent can enter.",
            )
            repeat(12) { index ->
                interfaces.sendText("glarials_tombstone_inscription", "line$index", lines.getOrElse(index) { "" })
            }
        }

        interfaceOption("Close", "glarials_tombstone_inscription:close") {
            close("glarials_tombstone_inscription")
        }

        itemOnObjectOperate("glarials_pebble", "glarials_tombstone") {
            message("You place the pebble in the gravestone's small indent.")
            delay(2)
            if (canEnterTomb()) {
                message("You hear a loud creak.")
                delay(2)
                message("The stone slab slides back revealing a ladder down.")
                delay(2)
                tele(2555, 9844)
                message("You climb down to an underground passage.")
            } else {
                message("It fits perfectly, but nothing happens.")
            }
        }

        objectOperate("Search", "crate_11") {
            message("You search the crate")
            delay(2)
            if (inventory.contains("a_key_waterfall_dungeon")) {
                // TODO message
                return@objectOperate
            }
            addOrDrop("a_key_waterfall_dungeon")
            message("and find a large key")
        }

        objectOperate("Search", "glarials_chest_opened") {
            if (inventory.contains("glarials_amulet")) {
                return@objectOperate // TODO check
            }
            addOrDrop("glarials_amulet")
            if (quest("waterfall_quest") == "found_pebble") {
                set("waterfall_quest", "found_amulet")
            }
            message("You search the chest and find a small amulet.")
        }

        objectOperate("Open", "glarials_chest_closed") { (target) ->
            message("You open the chest.")
            target.replace("glarials_chest_opened", ticks = 50)
        }

        objectOperate("Search", "glarials_tomb") {
            message("You search the coffin.")
            delay(2)
            if (inventory.contains("glarials_urn")) {
                return@objectOperate // TODO check
            }
            addOrDrop("glarials_urn")
            if (quest("waterfall_quest") == "found_amulet") {
                set("waterfall_quest", "found_urn")
            }
            message("Inside you find an urn full of ashes.")
            message("You take the urn and close the coffin.")
        }

        objectOperate("Open", "door_29_closed") { (target) -> enterChamberDoor(target) }

        itemOnObjectOperate("a_key_waterfall_dungeon", "door_29_closed") { (target) -> enterChamberDoor(target) }
    }

    internal fun Player.canEnterTomb(): Boolean = questStage("waterfall_quest") >= 4 &&
        inventory.contains("glarials_pebble") &&
        (inventory.items + equipment.items).none { item ->
            val slot: EquipSlot = item.def.getOrNull("slot") ?: EquipSlot.None
            !item.isEmpty() && (item.id.endsWith("_rune") || slot in setOf(EquipSlot.Weapon, EquipSlot.Shield, EquipSlot.Ammo) || COMBAT_BONUSES.any { item.def[it, 0] != 0 }) // TODO tidy
        }

    private suspend fun Player.enterChamberDoor(target: GameObject) {
        if (!inventory.contains("a_key_waterfall_dungeon")) {
            message("The door is locked.")
            return
        }
        val entering = tile.y <= target.tile.y
        val destination = target.tile.copy(y = if (entering) target.tile.y + 1 else target.tile.y - 1)
        if (!Door.openDoor(this, target, ticks = 10)) return
        try {
            delay()
            walkOverDelay(destination)
        } finally {
            GameObjects.timers.execute(target)
        }
        message("You open the door and walk through.")
        // The raised chamber uses a separate copy of the map.
        if (target.tile.x >= 2600) {
            tele(2566, 9900)
        } else if (target.tile.y == 9901 && entering && questStage("waterfall_quest") >= 8) {
            tele(2603, 9901)
        }
    }

    companion object {
        private val COMBAT_BONUSES = listOf("stab_attack", "slash_attack", "crush_attack", "magic_attack", "range_attack", "stab_defence", "slash_defence", "crush_defence", "magic_defence", "range_defence", "strength", "ranged_strength", "magic_strength", "prayer_bonus")
    }
}
