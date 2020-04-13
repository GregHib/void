package org.redrune.script

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import org.redrune.cache.Cache
import org.redrune.cache.cacheModule
import org.redrune.engine.script.ScriptMock

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