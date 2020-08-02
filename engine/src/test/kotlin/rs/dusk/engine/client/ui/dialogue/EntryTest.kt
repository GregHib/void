package rs.dusk.engine.client.ui.dialogue

import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.open
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

internal class EntryTest : DialogueIOTest() {

    @Test
    fun `Send string input`() {
        io.sendStringEntry("question")
        verify {
            player.send(ScriptMessage(108, "question"))
        }
    }

    @Test
    fun `Send int input`() {
        io.sendIntEntry("question")
        verify {
            player.send(ScriptMessage(109, "question"))
        }
    }

    @Test
    fun `Send item destroy`() {
        every { itemDecoder.getSafe(1234) } returns ItemDefinition(name = "magic")
        io.sendItemDestroy("question", 1234)
        verify {
            player.open("confirm_destroy")
            manager.sendText("confirm_destroy", "line1", "question")
            manager.sendText("confirm_destroy", "item_name", "magic")
            manager.sendItem("confirm_destroy", "item_slot", 1234, 1)
        }
    }

    @Test
    fun `Destroy item not sent if interface not opened`() {
        every { player.open("confirm_destroy") } returns false
        io.sendItemDestroy("question", 1234)
        verify(exactly = 0) {
            manager.sendText("confirm_destroy", "line1", "question")
        }
    }

    @Test
    fun `Send item box`() {
        io.sendItemBox("question", 9009, 650, 10)
        verify {
            player.open("obj_box")
            player.send(ScriptMessage(3449, 9009, 650))
            manager.sendSprite("obj_box", "sprite", 10)
            manager.sendText("obj_box", "line1", "question")
        }
    }

    @Test
    fun `Item box not sent if interface not opened`() {
        every { player.open("obj_box") } returns false
        io.sendItemBox("question", 9009, 650, 10)
        verify(exactly = 0) {
            player.send(ScriptMessage(3449, 9009, 650))
        }
    }

}