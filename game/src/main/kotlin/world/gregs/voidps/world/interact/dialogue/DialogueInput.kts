package world.gregs.voidps.world.interact.dialogue

import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.client.ui.event.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension
import world.gregs.voidps.engine.suspend.dialogue.StringSuspension
import world.gregs.voidps.engine.suspend.resumeDialogueSuspension

continueDialogue("dialogue_npc_chat*", "continue") { player ->
    player.continueDialogue()
}

continueDialogue("dialogue_chat*", "continue") { player ->
    player.continueDialogue()
}

continueDialogue("dialogue_message*", "continue") { player ->
    player.continueDialogue()
}

continueDialogue("dialogue_level_up", "continue") { player ->
    player.continueDialogue()
}

continueDialogue("dialogue_obj_box", "continue") { player ->
    player.continueDialogue()
}

continueDialogue("dialogue_double_obj_box", "continue") { player ->
    player.continueDialogue()
}

continueDialogue("dialogue_multi*", "line*") { player ->
    val choice = component.substringAfter("line").toIntOrNull() ?: -1
    val suspension = player.dialogueSuspension as? IntSuspension ?: return@continueDialogue
    suspension.int = choice
    player.resumeDialogueSuspension()
}

onEvent<Player, IntEntered> { player ->
    val suspension = player.dialogueSuspension as? IntSuspension ?: return@onEvent
    suspension.int = value
    player.resumeDialogueSuspension()
}

onEvent<Player, StringEntered> { player ->
    val suspension = player.dialogueSuspension as? StringSuspension ?: return@onEvent
    suspension.string = value
    player.resumeDialogueSuspension()
}

continueDialogue("dialogue_confirm_destroy") { player ->
    val suspension = player.dialogueSuspension as? StringSuspension ?: return@continueDialogue
    suspension.string = component
    player.resumeDialogueSuspension()
}

continueDialogue("dialogue_skill_creation", "choice*") { player ->
    val choice = component.substringAfter("choice").toIntOrNull() ?: 0
    val suspension = player.dialogueSuspension as? IntSuspension ?: return@continueDialogue
    suspension.int = choice - 1
    player.resumeDialogueSuspension()
}