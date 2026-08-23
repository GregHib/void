package content.skill.prayer.bone

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class Ectofuntus : Script {

    init {
        objectOperate("Worship", "ectofuntus") { (target) ->
            worship(target)
        }
    }

    private fun Player.worship(target: GameObject) {
        if (get("ectofuntus_charges", 0) >= MAXIMUM_CHARGES) {
            message("The Ectofuntus is full.")
            return
        }
        val row = bonemealRow()
        val slime = inventory.contains("bucket_of_slime")
        if (row == null && !slime) {
            message("You don't have any ectoplasm or crushed bones to put into the Ectofuntus.")
            return
        }
        if (row == null) {
            message("You need a pot of crushed bones to put into the Ectofuntus.")
            return
        }
        if (!slime) {
            message("You need ectoplasm to put into the Ectofuntus.")
            return
        }
        val bonemeal = row.item("bonemeal")
        inventory.transaction {
            replace("bucket_of_slime", "bucket")
            replace(bonemeal, "empty_pot")
        }
        if (inventory.transaction.error != TransactionError.None) {
            return
        }
        face(target)
        anim("worship_ectofuntus")
        sound("worship_ectofuntus")
        exp(Skill.Prayer, row.int("xp") * MULTIPLIER / 10.0)
        inc("ectofuntus_charges")
        message("You put some ectoplasm and bonemeal into the Ectofuntus, and worship it.")
    }

    private fun Player.bonemealRow(): RowDefinition? = Tables.get("bones").rows().firstOrNull {
        val bonemeal = it.itemOrNull("bonemeal") ?: return@firstOrNull false
        inventory.contains(bonemeal)
    }

    companion object {
        /** Worshipping gives four times the experience of burying the same bones. */
        private const val MULTIPLIER = 4

        /** Worships allowed before the tokens must be collected from a ghost disciple. */
        const val MAXIMUM_CHARGES = 53
    }
}
