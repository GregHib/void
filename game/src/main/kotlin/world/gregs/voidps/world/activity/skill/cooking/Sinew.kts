package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.dialogue.type.choice

itemOnObjectOperate("raw_beef", "cooking_range*", priority = Priority.HIGH) {
    val choice = choice(listOf("Dry the meat into sinew.", "Cook the meat."))
    player["sinew"] = choice == 1
}