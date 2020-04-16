package rs.dusk.engine.map

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import rs.dusk.cache.cacheModule
import rs.dusk.engine.map.collision.collisionModule
import rs.dusk.engine.map.location.Xteas
import rs.dusk.engine.map.location.locationModule
import rs.dusk.engine.map.location.xteaModule
import rs.dusk.engine.model.Region
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
internal class MapLoaderTest : KoinMock() {


    @BeforeEach
    fun setup() {
        setProperty("fsRsaModulus", "1")
        setProperty("fsRsaPrivate", "1")
        setProperty("cachePath", "../cache/data/cache/")
        loadModules(xteaModule, cacheModule, tileModule, collisionModule, locationModule)
    }


    @Test
    fun `Just a test`() {
        val region = Region(12342)
        declare { Xteas(mutableMapOf(region.id to intArrayOf(733680141, -1440926564, 447905675, 1806603117))) }
        val loader = MapLoader()

        loader.load(region)
        println("Hmm")
    }

}