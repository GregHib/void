package world.gregs.voidps.engine.client.ui

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declare
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.Client

abstract class InterfaceTest : KoinMock() {

    internal lateinit var client: Client
    internal lateinit var events: Events
    internal lateinit var interfaces: Interfaces
    internal lateinit var open: MutableSet<String>
    internal lateinit var definitions: InterfaceDefinitions
    internal lateinit var gameframe: PlayerGameFrame

    @BeforeEach
    open fun setup() {
        client = mockk(relaxed = true)
        events = mockk(relaxed = true)
        definitions = declare { mockk(relaxed = true) }
        gameframe = spyk(PlayerGameFrame())
        open = mutableSetOf()
        interfaces = spyk(Interfaces(events, client, definitions, gameframe, open))
        mockkStatic("world.gregs.voidps.network.encode.InterfaceEncodersKt")
    }
}
