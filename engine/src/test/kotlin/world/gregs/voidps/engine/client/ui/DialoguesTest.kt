package world.gregs.voidps.engine.client.ui

import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class DialoguesTest {

    @Nested
    inner class ContinueDialogueTest : ScriptTest {
        override val checks = listOf(
            listOf("id"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            continueDialogue(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Dialogues.continueDialogue(Player(), "id")
        }

        override val apis = listOf(Dialogues)

    }

    @Nested
    inner class ContinueItemDialogueTest : ScriptTest {
        override val checks = listOf(
            listOf("id"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            continueItemDialogue {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Dialogues.continueItem(Player(), "id")
        }

        override val apis = listOf(Dialogues)

    }

}