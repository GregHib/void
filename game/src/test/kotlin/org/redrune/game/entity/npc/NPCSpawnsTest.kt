package org.redrune.game.entity.npc

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import org.koin.test.mock.declareMock
import org.redrune.cache.Cache
import org.redrune.engine.Shutdown
import org.redrune.engine.data.file.fileLoaderModule
import org.redrune.engine.event.EventBus
import org.redrune.engine.event.eventBusModule
import org.redrune.engine.script.ScriptMock

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
        bus.emit(Shutdown())
    }

}