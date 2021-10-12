package world.gregs.voidps.world.interact.dialogue

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.client.ui.event.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

val logger = InlineLogger()

fun isActiveDialogueType(player: Player, type: String): Boolean {
    if (player.dialogues.currentType() != type) {
        logger.debug { "Invalid dialogue type ${player.dialogues.currentType()} for $type." }
        return false
    }
    return true
}

on<ContinueDialogue>({ name.contains("chat") && component == "continue" }) { player: Player ->
    if (isActiveDialogueType(player, "chat")) {
        player.dialogues.resume()
    }
}

on<ContinueDialogue>({ name.contains("message") && component == "continue" }) { player: Player ->
    if (isActiveDialogueType(player, "statement")) {
        player.dialogues.resume()
    }
}

on<ContinueDialogue>({ name == "level_up_dialog" && component == "continue" }) { player: Player ->
    if (isActiveDialogueType(player, "level")) {
        player.dialogues.resume()
    }
}

on<ContinueDialogue>({ name == "obj_box" && component == "continue" }) { player: Player ->
    if (isActiveDialogueType(player, "item")) {
        player.dialogues.resume()
    }
}

on<ContinueDialogue>({ name.contains("multi") && component.startsWith("line") }) { player: Player ->
    if (isActiveDialogueType(player, "choice")) {
        val choice = component.substringAfter("line").toIntOrNull() ?: -1
        player.dialogues.resume(choice)
    }
}

on<IntEntered> { player: Player ->
    if (isActiveDialogueType(player, "int")) {
        player.dialogues.resume(value)
    }
}

on<StringEntered> { player: Player ->
    if (isActiveDialogueType(player, "string")) {
        player.dialogues.resume(value)
    }
}

on<ContinueDialogue>({ name == "confirm_destroy" }) { player: Player ->
    player.dialogues.resume(component == "confirm")
}

on<ContinueDialogue>({ name == "skill_creation" && component.startsWith("choice") }) { player: Player ->
    val choice = component.substringAfter("choice").toIntOrNull() ?: 0
    player.dialogues.resume(choice - 1)
}