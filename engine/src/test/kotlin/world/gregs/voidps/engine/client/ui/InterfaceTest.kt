package world.gregs.voidps.engine.client.ui

import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declare
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetail
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.script.KoinMock

abstract class InterfaceTest : KoinMock() {

    internal lateinit var manager: Interfaces
    internal lateinit var interfaces: MutableMap<String, InterfaceDetail>
    internal lateinit var details: InterfaceDefinitions
    internal lateinit var gameframe: PlayerGameFrame
    internal lateinit var names: MutableMap<Int, String>

    @BeforeEach
    open fun setup() {
        interfaces = mutableMapOf()
        names = mutableMapOf()
        details = declare { spyk(InterfaceDetails().apply {
            load(interfaces, names)
        }) }
        gameframe = spyk(PlayerGameFrame())
        manager = spyk(Interfaces(details, gameframe))
    }
}
