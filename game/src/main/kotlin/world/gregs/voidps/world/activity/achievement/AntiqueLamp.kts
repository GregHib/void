package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem

inventoryItem("Rub", "antique_lamp_easy_lumbridge_tasks", "inventory") {
    val skill = skillLamp()
    if (player.inventory.remove(slot, item.id)) {
        player.exp(skill, 500.0)
        statement("<blue>Your wish has been granted!<br><black>You have been awarded 500 ${skill.name} experience!")
    }
}