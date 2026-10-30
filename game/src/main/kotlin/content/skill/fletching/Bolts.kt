package content.skill.fletching

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Bolts : Script {

    init {
        itemOnItem("feather", "*_bolts_unf,unfinished_broad_bolts") { _, toItem ->
            val bolt = Rows.getOrNull("fletching_bolts.${toItem.id}") ?: return@itemOnItem

            val variable = bolt.stringOrNull("variable")
            if (variable != null && !contains(variable)) {
                message(bolt.string("variable_message"))
                return@itemOnItem
            }

            if (!has(Skill.Fletching, bolt.int("level"), true)) {
                return@itemOnItem
            }

            val currentFeathers = inventory.count("feather")
            val currentBoltUnf = inventory.count(toItem.id)

            val actualAmount = minOf(currentFeathers, currentBoltUnf, 10)

            if (actualAmount < 1) {
                message("You don't have enough materials to fletch bolts.", ChatType.Game)
                return@itemOnItem
            }

            val createdBolt: String = bolt.itemOrNull("product") ?: toItem.id.replace("_unf", "")
            val success = inventory.transaction {
                remove(toItem.id, actualAmount)
                remove("feather", actualAmount)
                add(createdBolt, actualAmount)
            }

            if (!success) {
                return@itemOnItem
            }

            val totalExperience = (bolt.int("xp") / 10.0) * actualAmount
            exp(Skill.Fletching, totalExperience)
            message("You fletch $actualAmount bolts.", ChatType.Game)
        }
    }
}
