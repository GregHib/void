package content.entity.player.dialogue

import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.instruction.onInstruction
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.network.client.instruction.EnterInt
import world.gregs.voidps.network.client.instruction.EnterString

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

onInstruction<EnterInt> { player ->
    (player.dialogueSuspension as? IntSuspension)?.resume(value)
}

onInstruction<EnterString> { player ->
    (player.dialogueSuspension as? StringSuspension)?.resume(value)
}

continueDialogue("dialogue_confirm_destroy") { player ->
    (player.dialogueSuspension as? StringSuspension)?.resume(component)
}

continueDialogue("dialogue_skill_creation", "choice*") { player ->
    val choice = component.substringAfter("choice").toIntOrNull() ?: 0
    (player.dialogueSuspension as? IntSuspension)?.resume(choice - 1)
}