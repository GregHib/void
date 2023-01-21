package world.gregs.voidps.world.interact.dialogue.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.event.suspend.ContinueSuspension
import world.gregs.voidps.world.interact.dialogue.sendLines

private const val MAXIMUM_STATEMENT_SIZE = 5
private val logger = InlineLogger()

suspend fun DialogueContext.statement(text: String, clickToContinue: Boolean = true) {
    val lines = text.trimIndent().lines()

    if (lines.size > MAXIMUM_STATEMENT_SIZE) {
        logger.debug { "Maximum statement lines exceeded ${lines.size} for $player" }
        return
    }

    val id = getInterfaceId(lines.size, clickToContinue)
    if (player.open(id)) {
        player.interfaces.sendLines(id, lines)
        await<Unit>("statement")
    }
}

context(PlayerContext) suspend fun statement(text: String, clickToContinue: Boolean = true) {
    val lines = text.trimIndent().lines()
    check(lines.size <= MAXIMUM_STATEMENT_SIZE) { "Maximum statement lines exceeded ${lines.size} for $player" }
    val id = getInterfaceId(lines.size, clickToContinue)
    check(player.open(id)) { "Unable to open statement dialogue $id for $player" }
    player.interfaces.sendLines(id, lines)
    ContinueSuspension(player)
    player.close(id)
}

private fun getInterfaceId(lines: Int, prompt: Boolean): String {
    return "dialogue_message${if (!prompt) "_np" else ""}$lines"
}