package rs.dusk.world.entity.npc

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import org.koin.test.mock.declareMock
import rs.dusk.cache.Cache
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.engine.Shutdown
import rs.dusk.engine.script.ScriptMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
internal class NPCSpawnsTest : ScriptMock() {

    lateinit var cache: Cache

    @BeforeEach
    override fun setup() {
        loadModules(eventBusModule, fileLoaderModule)
        cache = declareMock()
        super.setup()
    }

    @Test
    fun `Script test`() {
        val bus: EventBus = get()
        bus.emit(Shutdown)
    }

}