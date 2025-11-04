package world.gregs.voidps.engine.client.ui

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declare
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.InterfaceInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.client.Client

abstract class InterfaceTest : KoinMock() {

    internal lateinit var client: Client
    internal lateinit var events: Player
    internal lateinit var interfaces: Interfaces
    internal lateinit var open: MutableMap<String, String>
    internal lateinit var definitions: InterfaceDefinitions

    @BeforeEach
    open fun setup() {
        client = mockk(relaxed = true)
        events = mockk(relaxed = true)
        every { events.client } returns client
        definitions = declare { mockk(relaxed = true) }
        open = mutableMapOf()
        interfaces = spyk(Interfaces(events, definitions, open))
        mockkObject(InterfaceInteraction)
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.client.ui.InterfacesKt")
    }

    @AfterEach
    fun teardown() {
        unmockkObject(InterfaceInteraction)
    }
}
