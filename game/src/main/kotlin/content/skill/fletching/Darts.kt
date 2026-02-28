package content.skill.fletching

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Darts : Script {

    init {
        itemOnItem("feather", "*_dart_tip") { _, toItem ->
            val xp = EnumDefinitions.intOrNull("dart_fletching_xp", toItem.id) ?: return@itemOnItem
            val level = EnumDefinitions.int("dart_fletching_level", toItem.id)

            if (!has(Skill.Fletching, level, true)) {
                return@itemOnItem
            }

            val currentFeathers = inventory.count("feather")
            val currentDartTips = inventory.count(toItem.id)

            val actualAmount = minOf(currentFeathers, currentDartTips, 10)

            if (actualAmount < 1) {
                message("You don't have enough materials to fletch bolts.", ChatType.Game)
                return@itemOnItem
            }

            val createdDart: String = toItem.id.replace("_tip", "")
            val success = inventory.transaction {
                remove(toItem.id, actualAmount)
                remove("feather", actualAmount)
                add(createdDart, actualAmount)
            }

            if (!success) {
                return@itemOnItem
            }

            val totalExperience = (xp / 10.0) * actualAmount
            experience.add(Skill.Fletching, totalExperience)
            message("You finish making $actualAmount darts.", ChatType.Game)
        }
    }
}
