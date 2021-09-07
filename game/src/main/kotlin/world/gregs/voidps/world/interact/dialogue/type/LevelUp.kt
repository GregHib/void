package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.skill.Skill

private const val LEVEL_UP_INTERFACE_NAME = "level_up_dialog"

suspend fun DialogueContext.levelUp(text: String, skill: Skill) {
    val lines = text.trimIndent().lines()
    if (player.open(LEVEL_UP_INTERFACE_NAME)) {
        for ((index, line) in lines.withIndex()) {
            player.interfaces.sendText(LEVEL_UP_INTERFACE_NAME, "line${index + 1}", line)
        }
        player.setVar("level_up_icon", skill.name)
        return await("level")
    }
}