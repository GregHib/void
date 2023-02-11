package world.gregs.voidps.world.interact.dialogue

import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.client.ui.event.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.engine.suspend.resumeSuspension

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
    val suspension = player.suspension as? IntSuspension ?: return@on
    suspension.int = choice
    player.resumeSuspension()
}

on<IntEntered> { player: Player ->
    val suspension = player.suspension as? IntSuspension ?: return@on
    suspension.int = value
    player.resumeSuspension()
}

on<StringEntered> { player: Player ->
    val suspension = player.suspension as? StringSuspension ?: return@on
    suspension.string = value
    player.resumeSuspension()
}

on<ContinueDialogue>({ id == "dialogue_confirm_destroy" }) { player: Player ->
    val suspension = player.suspension as? StringSuspension ?: return@on
    suspension.string = component
    player.resumeSuspension()
}

on<ContinueDialogue>({ id == "dialogue_skill_creation" && component.startsWith("choice") }) { player: Player ->
    val choice = component.substringAfter("choice").toIntOrNull() ?: 0
    val suspension = player.suspension as? IntSuspension ?: return@on
    suspension.int = choice - 1
    player.resumeSuspension()
}