package world.gregs.voidps.engine.client.ui

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declare
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.client.Client

abstract class InterfaceTest : KoinMock() {

    internal lateinit var client: Client
    internal lateinit var events: EventDispatcher
    internal lateinit var interfaces: Interfaces
    internal lateinit var open: MutableMap<String, String>
    internal lateinit var definitions: InterfaceDefinitions

    @BeforeEach
    open fun setup() {
        client = mockk(relaxed = true)
        events = mockk(relaxed = true)
        definitions = declare { mockk(relaxed = true) }
        open = mutableMapOf()
        interfaces = spyk(Interfaces(events, client, definitions, open))
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
        mockkStatic("world.gregs.voidps.engine.client.ui.InterfacesKt")
    }
}
