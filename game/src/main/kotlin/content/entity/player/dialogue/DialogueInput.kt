package content.entity.player.dialogue

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.engine.suspend.NameSuspension
import world.gregs.voidps.engine.suspend.StringSuspension
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
            (dialogueSuspension as? IntSuspension)?.resume(choice)
        }

        instruction<EnterInt> { player ->
            (player.dialogueSuspension as? IntSuspension)?.resume(value)
        }

        instruction<EnterString> { player ->
            (player.dialogueSuspension as? StringSuspension)?.resume(value)
        }

        instruction<EnterName> { player ->
            (player.dialogueSuspension as? NameSuspension)?.resume(value)
        }

        continueDialogue("dialogue_confirm_destroy:*") {
            (dialogueSuspension as? StringSuspension)?.resume(it.substringAfter(":"))
        }

        continueDialogue("dialogue_skill_creation:choice*") {
            val choice = it.substringAfter(":choice").toIntOrNull() ?: 0
            (dialogueSuspension as? IntSuspension)?.resume(choice - 1)
        }
    }
}
