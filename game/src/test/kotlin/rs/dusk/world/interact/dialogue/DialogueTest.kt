package rs.dusk.world.interact.dialogue

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player

abstract class DialogueTest {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var player: Player
    lateinit var context: DialogueContext

    @BeforeEach
    open fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        manager = spyk(Dialogues())
        context = spyk(DialogueContext(manager, player))
        every { player.open(any()) } returns true
        every { player.interfaces } returns interfaces
    }

}