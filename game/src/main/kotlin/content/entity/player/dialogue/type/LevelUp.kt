package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.ContinueSuspension

private const val LEVEL_UP_INTERFACE_ID = "dialogue_level_up"

suspend fun SuspendableContext<Player>.levelUp(skill: Skill, text: String) {
    val lines = text.trimIndent().lines()
    check(player.open(LEVEL_UP_INTERFACE_ID)) { "Unable to open level up interface for $player" }
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(LEVEL_UP_INTERFACE_ID, "line${index + 1}", line)
    }
    player["level_up_icon"] = skill.name
    ContinueSuspension.get(player)
    player.close(LEVEL_UP_INTERFACE_ID)
}