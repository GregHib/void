package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.dialogue.ContinueSuspension
import world.gregs.voidps.world.interact.dialogue.sendLines

private const val MAXIMUM_STATEMENT_SIZE = 5

suspend fun SuspendableContext<Player>.statement(text: String, clickToContinue: Boolean = true) {
    val lines = if (text.contains("\n")) text.trimIndent().lines() else get<FontDefinitions>().get("q8_full").splitLines(text, 470)
    check(lines.size <= MAXIMUM_STATEMENT_SIZE) { "Maximum statement lines exceeded ${lines.size} for $player" }
    val id = getInterfaceId(lines.size, clickToContinue)
    check(player.open(id)) { "Unable to open statement dialogue $id for $player" }
    player.interfaces.sendLines(id, lines)
    if (clickToContinue) {
        ContinueSuspension()
        player.close(id)
    }
}

private fun getInterfaceId(lines: Int, prompt: Boolean): String {
    return "dialogue_message${if (!prompt) "_np" else ""}$lines"
}