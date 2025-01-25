package content.entity.player.dialogue.type

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.suspend.StringSuspension

internal class QuestStartTest : DialogueTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        declareMock<QuestDefinitions> {
            every { this@declareMock.getOrNull(any<String>()) } returns null
            every { this@declareMock.getOrNull("test_quest") } returns QuestDefinition(id = 1234, name = "magic", extras = mapOf(
                "name" to "quest_name"
            ))
        }
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendScript(any(), any<String>()) } just Runs
    }

    @Test
    fun `Send quest interface`() {
        dialogue {
            startQuest("test_quest")
        }
        verify {
            player.open("quest_intro")
            interfaces.sendText("quest_intro", "status_field", "Not started")
            interfaces.sendText("quest_intro", "quest_field", "quest_name")
            interfaces.sendVisibility("quest_intro", "start_choice_layer", true)
            interfaces.sendVisibility("quest_intro", "progress_status_layer", false)
        }
    }

    @Test
    fun `Completed quest shows status`() {
        player["test_quest"] = "completed"
        dialogue {
            startQuest("test_quest")
        }
        verify {
            player.open("quest_intro")
            interfaces.sendVisibility("quest_intro", "start_choice_layer", false)
            interfaces.sendVisibility("quest_intro", "progress_status_layer", true)
        }
    }

    @Test
    fun `Quest info not sent if interface not opened`() {
        every { player.open("quest_intro") } returns false
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                startQuest("test_quest")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("quest_intro", "quest_field", "quest_name")
        }
    }

    @Test
    fun `Quest info not sent if quest not found`() {
        assertThrows<IllegalStateException> {
            dialogueBlocking {
                startQuest("unknown_quest")
            }
        }
        coVerify(exactly = 0) {
            interfaces.sendText("quest_intro", "quest_field", any())
        }
    }

    @Test
    fun `Quest start returns boolean`() {
        every { player.open("quest_intro") } returns true
        var start = false
        dialogue {
            start = startQuest( "test_quest")
        }
        val suspend = player.dialogueSuspension as StringSuspension
        suspend.resume("yes")
        assertTrue(start)
    }
}
