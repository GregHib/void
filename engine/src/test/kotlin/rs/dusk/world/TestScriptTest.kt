package rs.dusk.world

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import rs.dusk.cache.Cache
import rs.dusk.cache.cacheModule
import rs.dusk.engine.script.ScriptMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
internal class TestScriptTest : ScriptMock() {

    lateinit var cache: Cache

    @BeforeEach
    override fun setup() {
        loadModules(cacheModule)
        cache = declareMock()
        super.setup()
    }

    @Test
    fun `Script test`() {

    }

}