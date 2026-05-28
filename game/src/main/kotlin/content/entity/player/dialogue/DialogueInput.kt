package content.entity.player.dialogue

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.network.client.instruction.EnterInt
import world.gregs.voidps.network.client.instruction.EnterName
import world.gregs.voidps.network.client.instruction.EnterString

class DialogueInput : Script {

    init {
        continueDialogue("dialogue_npc_chat*:continue") {
            continueDialogue()
        }

        continueDialogue("dialogue_chat*:continue") {
            continueDialogue()
        }

        continueDialogue("dialogue_message*:continue") {
            continueDialogue()
        }

        continueDialogue("dialogue_level_up:continue") {
            closeDialogue()
        }

        continueDialogue("dialogue_obj_box:continue") {
            continueDialogue()
        }

        continueDialogue("dialogue_double_obj_box:continue") {
            continueDialogue()
        }

        continueDialogue("dialogue_multi*:line*") {
            val choice = it.substringAfter(":line").toIntOrNull() ?: -1
            (suspension as? Suspension.IntEntry)?.resume(choice)
        }

        instruction<EnterInt> { player ->
            (player.suspension as? Suspension.IntEntry)?.resume(value)
            player.sendScript("close_entry")
        }

        instruction<EnterString> { player ->
            (player.suspension as? Suspension.StringEntry)?.resume(value)
            player.sendScript("close_entry")
        }

        instruction<EnterName> { player ->
            (player.suspension as? Suspension.NameEntry)?.resume(value)
            player.sendScript("close_entry")
        }

        continueDialogue("dialogue_confirm_destroy:*") {
            (suspension as? Suspension.StringEntry)?.resume(it.substringAfter(":"))
        }

        continueDialogue("dialogue_skill_creation:choice*") {
            val choice = it.substringAfter(":choice").toIntOrNull() ?: 0
            (suspension as? Suspension.IntEntry)?.resume(choice - 1)
            closeDialogue()
        }
    }
}
