package world.gregs.voidps.world.interact.dialogue

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

abstract class DialogueTest {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var player: Player
    lateinit var context: DialogueContext

    @BeforeEach
    open fun setup() {
        mockkStatic("world.gregs.voidps.engine.client.ui.InterfacesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        manager = spyk(Dialogues())
        context = spyk(DialogueContext(manager, player))
        every { player.open(any()) } returns true
        every { player.interfaces } returns interfaces
    }

}