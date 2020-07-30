package rs.dusk.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declare
import rs.dusk.engine.model.entity.character.player.PlayerGameFrame
import rs.dusk.engine.script.KoinMock

abstract class InterfaceTest : KoinMock() {

    internal lateinit var manager: InterfaceManager
    internal lateinit var io: InterfaceIO
    internal lateinit var interfaces: MutableMap<Int, Interface>
    internal lateinit var lookup: InterfacesLookup
    internal lateinit var gameframe: PlayerGameFrame
    internal lateinit var names: MutableMap<String, Int>

    @BeforeEach
    open fun setup() {
        io = mockk(relaxed = true)
        every { io.sendOpen(any())} answers {
            val inter: Interface = arg(0)
            inter.getParent(gameframe.resizable)
            inter.getIndex(gameframe.resizable)
        }
        interfaces = mutableMapOf()
        names = mutableMapOf()
        lookup = declare { spyk(InterfacesLookup(interfaces, names)) }
        gameframe = spyk(PlayerGameFrame())
        manager = spyk(InterfaceManager(io, lookup, gameframe))
    }
}
