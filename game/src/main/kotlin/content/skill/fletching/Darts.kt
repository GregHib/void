package content.skill.fletching

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.definition.data.FletchDarts
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.event.Script
@Script
class Darts {

    init {
        itemOnItem("feather", "*_dart_tip") {
            val darts: FletchDarts = toItem.def.getOrNull("fletch_dart") ?: return@itemOnItem
        
            if (!it.has(Skill.Fletching, darts.level, true)) {
                return@itemOnItem
            }
        
            val currentFeathers = it.inventory.count("feather")
            val currentDartTips = it.inventory.count(toItem.id)
        
            val actualAmount = minOf(currentFeathers, currentDartTips, 10)
        
            if (actualAmount < 1) {
                it.message("You don't have enough materials to fletch bolts.", ChatType.Game)
                return@itemOnItem
            }
        
            val createdDart: String = toItem.id.replace("_tip", "")
            val success = it.inventory.transaction {
                remove(toItem.id, actualAmount)
                remove("feather", actualAmount)
                add(createdDart, actualAmount)
            }
        
            if (!success) {
                return@itemOnItem
            }
        
            val totalExperience = darts.xp * actualAmount
            it.experience.add(Skill.Fletching, totalExperience)
            it.message("You finish making $actualAmount darts.", ChatType.Game)
        }

    }

}
