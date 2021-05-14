package world.gregs.voidps.world.interact.dialogue

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.world.script.KoinMock

abstract class DialogueTest : KoinMock() {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var player: Player
    lateinit var context: DialogueContext
    lateinit var definitions: InterfaceDefinitions

    @BeforeEach
    open fun setup() {
        mockkStatic("world.gregs.voidps.engine.client.ui.InterfacesKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        definitions = declareMock()
        manager = spyk(Dialogues())
        context = spyk(DialogueContext(manager, player))
        every { player.open(any()) } returns true
        every { player.interfaces } returns interfaces
        every { definitions.get(any<String>()) } returns InterfaceDefinition()
    }

}