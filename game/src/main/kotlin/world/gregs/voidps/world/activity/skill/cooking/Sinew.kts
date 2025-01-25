package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import content.entity.player.dialogue.type.choice

itemOnObjectOperate("raw_beef", "cooking_range*", arrive = false, override = false) {
    val choice = choice(listOf("Dry the meat into sinew.", "Cook the meat."))
    player["sinew"] = choice == 1
}