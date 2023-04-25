package world.gregs.voidps.world.interact.dialogue

import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.client.ui.event.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension
import world.gregs.voidps.engine.suspend.dialogue.StringSuspension
import world.gregs.voidps.engine.suspend.resumeDialogueSuspension

on<ContinueDialogue>({ (id.startsWith("dialogue_npc_chat") || id.startsWith("dialogue_chat")) && component == "continue" }) { player: Player ->
    player.continueDialogue()
}

on<ContinueDialogue>({ id.startsWith("dialogue_message") && component == "continue" }) { player: Player ->
    player.continueDialogue()
}

on<ContinueDialogue>({ id == "dialogue_level_up" && component == "continue" }) { player: Player ->
    player.continueDialogue()
}

on<ContinueDialogue>({ id == "dialogue_obj_box" && component == "continue" }) { player: Player ->
    player.continueDialogue()
}

on<ContinueDialogue>({ id.startsWith("dialogue_multi") && component.startsWith("line") }) { player: Player ->
    val choice = component.substringAfter("line").toIntOrNull() ?: -1
    val suspension = player.dialogueSuspension as? IntSuspension ?: return@on
    suspension.int = choice
    player.resumeDialogueSuspension()
}

on<IntEntered> { player: Player ->
    val suspension = player.dialogueSuspension as? IntSuspension ?: return@on
    suspension.int = value
    player.resumeDialogueSuspension()
}

on<StringEntered> { player: Player ->
    val suspension = player.dialogueSuspension as? StringSuspension ?: return@on
    suspension.string = value
    player.resumeDialogueSuspension()
}

on<ContinueDialogue>({ id == "dialogue_confirm_destroy" }) { player: Player ->
    val suspension = player.dialogueSuspension as? StringSuspension ?: return@on
    suspension.string = component
    player.resumeDialogueSuspension()
}

on<ContinueDialogue>({ id == "dialogue_skill_creation" && component.startsWith("choice") }) { player: Player ->
    val choice = component.substringAfter("choice").toIntOrNull() ?: 0
    val suspension = player.dialogueSuspension as? IntSuspension ?: return@on
    suspension.int = choice - 1
    player.resumeDialogueSuspension()
}