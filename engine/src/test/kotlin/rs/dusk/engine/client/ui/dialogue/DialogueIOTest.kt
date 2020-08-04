package rs.dusk.engine.client.ui.dialogue

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.name

abstract class DialogueIOTest {

    lateinit var manager: Interfaces
    lateinit var player: Player
    lateinit var io: DialogueIO
    lateinit var itemDecoder: ItemDecoder

    @BeforeEach
    open fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        mockkStatic("rs.dusk.engine.client.SessionsKt")
        mockkStatic("rs.dusk.engine.entity.character.update.visual.player.AppearanceKt")
        mockkStatic("rs.dusk.engine.client.variable.VariablesKt")
        player = mockk(relaxed = true)
        manager = mockk(relaxed = true)
        every { player.open(any()) } returns true
        every { player.send(any<Message>()) } just Runs
        every { player.setVar(any(), any<Message>()) } just Runs
        every { player.name } returns "name"
        every { player.interfaces } returns manager
        itemDecoder = mockk(relaxed = true)
        io = PlayerDialogueIO(player)
    }

}