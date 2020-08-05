package rs.dusk.world.interact.dialogue

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.dialogue.event.ContinueDialogue
import rs.dusk.world.interact.dialogue.event.IntEntered
import rs.dusk.world.interact.dialogue.event.StringEntered

val logger = InlineLogger()

fun isActiveDialogueType(player: Player, type: String): Boolean {
    if(player.dialogues.currentType() != type) {
        logger.debug { "Invalid dialogue type ${player.dialogues.currentType()} for $type." }
        return false
    }
    return true
}

ContinueDialogue where { name.contains("chat") && component == "continue" } then {
    if(isActiveDialogueType(player, "chat")) {
        player.dialogues.resume()
    }
}

ContinueDialogue where { name.contains("message") && component == "continue" } then {
    if(isActiveDialogueType(player, "statement")) {
        player.dialogues.resume()
    }
}

ContinueDialogue where { name == "level_up_dialog" && component == "continue" } then {
    if(isActiveDialogueType(player, "level")) {
        player.dialogues.resume()
    }
}

ContinueDialogue where { name == "obj_box" && component == "continue" } then {
    if(isActiveDialogueType(player, "item")) {
        player.dialogues.resume()
    }
}

ContinueDialogue where { name.contains("multi") && component.startsWith("line") } then {
    if(isActiveDialogueType(player, "choice")) {
        val choice = component.substringAfter("line").toIntOrNull() ?: -1
        player.dialogues.resume(choice)
    }
}

IntEntered then {
    if(isActiveDialogueType(player, "int")) {
        player.dialogues.resume(value)
    }
}

StringEntered then {
    if(isActiveDialogueType(player, "string")) {
        player.dialogues.resume(value)
    }
}

ContinueDialogue where { name == "confirm_destroy" } then {
    player.dialogues.resume(component == "confirm")
}

ContinueDialogue where { name == "skill_creation" && component.startsWith("choice") } then {
    val choice = component.substringAfter("choice").toIntOrNull() ?: 0
    player.dialogues.resume(choice - 1)
}