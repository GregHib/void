package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.suspend.ContinueSuspension

private const val LEVEL_UP_INTERFACE_ID = "dialogue_level_up"

suspend fun DialogueContext.levelUp(text: String, skill: Skill) {
    val lines = text.trimIndent().lines()
    if (player.open(LEVEL_UP_INTERFACE_ID)) {
        for ((index, line) in lines.withIndex()) {
            player.interfaces.sendText(LEVEL_UP_INTERFACE_ID, "line${index + 1}", line)
        }
        player.setVar("level_up_icon", skill.name)
        return await("level")
    }
}

suspend fun PlayerContext.levelUp(text: String, skill: Skill) {
    val lines = text.trimIndent().lines()
    check(player.open(LEVEL_UP_INTERFACE_ID)) { "Unable to open level up interface for $player" }
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(LEVEL_UP_INTERFACE_ID, "line${index + 1}", line)
    }
    player.setVar("level_up_icon", skill.name)
    ContinueSuspension(player)
    player.close(LEVEL_UP_INTERFACE_ID)
}