package content.skill.prayer.bone

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.queue.weakQueue

class BoneGrinder : Script {

    init {
        objectOperate("Fill", "ectofuntus_hopper") {
            fill(null)
        }

        itemOnObjectOperate(obj = "ectofuntus_hopper") { (_, item) ->
            fill(item)
        }

        objectOperate("Wind", "ectofuntus_bone_grinder") {
            wind()
        }

        objectOperate("Empty", "ectofuntus_bin") {
            empty()
        }

        objectOperate("Status", "ectofuntus_bone_grinder") {
            status()
        }

        objectOperate("Settings", "ectofuntus_bone_grinder") {
            settings()
        }
    }

    private suspend fun Player.fill(item: Item?) {
        if (get("bone_grinder_stage", 0) != EMPTY) {
            message("You already have some bones in the hopper.")
            return
        }
        if (item != null && Tables.intOrNull("bones.${item.id}.xp") != null && Tables.itemOrNull("bones.${item.id}.bonemeal") == null) {
            statement("These bones could break the bone grinder. Perhaps I should find some different bones.")
            return
        }
        val row = boneRow(item)
        if (row == null) {
            message("You have no bones to grind.")
            return
        }
        if (get("bone_grinder_auto", false)) {
            grind(row)
            return
        }
        load(row)
    }

    private fun Player.load(row: RowDefinition) {
        anim("fill_bone_hopper")
        sound("fill_grinder")
        if (!inventory.remove(row.rowId)) {
            return
        }
        set("bone_grinder_bones", row.rowId)
        set("bone_grinder_stage", HOPPER)
        message("You fill the hopper with bones.")
    }

    private fun Player.wind() {
        when (get("bone_grinder_stage", 0)) {
            EMPTY -> {
                message("You have no bones loaded to grind.")
                return
            }
            BIN -> {
                message("You already have some bonemeal that needs to be collected.")
                return
            }
        }
        anim("wind_bone_grinder")
        sound("grinder_grinding")
        set("bone_grinder_stage", BIN)
        message("You wind the grinder handle.")
        message("Some crushed bones pour into the bin.")
    }

    private fun Player.empty() {
        if (get("bone_grinder_stage", 0) != BIN) {
            message("You have no bonemeal to collect.")
            return
        }
        if (!inventory.contains("empty_pot")) {
            message("You don't have any pots to take the bonemeal with.")
            return
        }
        val bones = get("bone_grinder_bones", "")
        val bonemeal = Tables.itemOrNull("bones.$bones.bonemeal") ?: return
        anim("empty_bone_bin")
        sound("grinder_empty")
        if (!inventory.replace("empty_pot", bonemeal)) {
            return
        }
        set("bone_grinder_stage", EMPTY)
        set("bone_grinder_bones", "")
        message("You empty the bin into the pot.")
    }

    private fun Player.status() {
        val mode = if (get("bone_grinder_auto", false)) "automatic" else "manual"
        val state = when (get("bone_grinder_stage", 0)) {
            HOPPER -> "There are ${get("bone_grinder_bones", "").toLowerSpaceCase()} in the hopper."
            BIN -> "There is bonemeal waiting in the bin."
            else -> "The grinder is empty."
        }
        message("$state The grinder is set to $mode.")
    }

    private suspend fun Player.settings() {
        choice("How should the grinder operate?") {
            option("Automatic.") {
                set("bone_grinder_auto", true)
                message("The grinder is now set to automatic.")
            }
            option("Manual.") {
                set("bone_grinder_auto", false)
                message("The grinder is now set to manual.")
            }
        }
    }

    /**
     * Automatic mode; runs the fill, wind and empty steps unattended, repeating until the
     * player runs out of either bones or empty pots.
     */
    private fun Player.grind(row: RowDefinition) {
        if (!inventory.contains("empty_pot")) {
            message("You don't have any pots to take the bonemeal with.")
            return
        }
        anim("fill_bone_hopper")
        sound("fill_grinder")
        weakQueue("bone_grinder", GRIND_TICKS) {
            anim("wind_bone_grinder")
            sound("grinder_grinding")
            weakQueue("bone_grinder", GRIND_TICKS) {
                collect(row)
            }
        }
    }

    private fun Player.collect(row: RowDefinition) {
        val bonemeal = row.item("bonemeal")
        inventory.transaction {
            remove(row.rowId)
            replace("empty_pot", bonemeal)
        }
        if (inventory.transaction.error != TransactionError.None) {
            return
        }
        anim("empty_bone_bin")
        sound("grinder_empty")
        message("You grind the ${row.rowId.toLowerSpaceCase()} into the pot.")
        val next = boneRow(null) ?: return
        grind(next)
    }

    private fun Player.boneRow(item: Item?): RowDefinition? {
        if (item != null) {
            val row = Tables.get("bones").rows().firstOrNull { it.rowId == item.id } ?: return null
            row.itemOrNull("bonemeal") ?: return null
            return row
        }
        return Tables.get("bones").rows().firstOrNull {
            it.itemOrNull("bonemeal") ?: return@firstOrNull false
            inventory.contains(it.rowId)
        }
    }

    companion object {
        private const val EMPTY = 0
        private const val HOPPER = 1
        private const val BIN = 2
        private const val GRIND_TICKS = 3
    }
}
