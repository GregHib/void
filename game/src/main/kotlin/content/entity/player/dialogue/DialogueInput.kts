package content.entity.player.dialogue

import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.client.ui.event.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.engine.suspend.StringSuspension

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
    (player.dialogueSuspension as? IntSuspension)?.resume(choice)
}

onEvent<Player, IntEntered> { player ->
    (player.dialogueSuspension as? IntSuspension)?.resume(value)
}

onEvent<Player, StringEntered> { player ->
    (player.dialogueSuspension as? StringSuspension)?.resume(value)
}

continueDialogue("dialogue_confirm_destroy") { player ->
    (player.dialogueSuspension as? StringSuspension)?.resume(component)
}

continueDialogue("dialogue_skill_creation", "choice*") { player ->
    val choice = component.substringAfter("choice").toIntOrNull() ?: 0
    (player.dialogueSuspension as? IntSuspension)?.resume(choice - 1)
}