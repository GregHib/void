package rs.dusk.engine.model.world.map

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.get
import org.koin.test.mock.declare
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.entity.factory.ObjectFactory
import rs.dusk.engine.model.entity.factory.entityFactoryModule
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.map.collision.collisionModule
import rs.dusk.engine.model.world.map.location.*
import rs.dusk.engine.model.world.map.location.LocationWriter.Companion.localId
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
internal class MapReaderTest : KoinMock() {


    @BeforeEach
    fun setup() {
        setProperty("fsRsaModulus", "1")
        setProperty("fsRsaPrivate", "1")
        setProperty("cachePath", "../cache/data/cache/")
        loadModules(
            xteaModule,
            cacheModule,
            cacheDefinitionModule,
            tileModule,
            collisionModule,
            eventBusModule,
            entityListModule,
            entityFactoryModule,
            locationModule
        )
    }


    @Test
    fun `Just a test`() {
        val region = Region(12342)
        declare { Xteas(mutableMapOf(region.id to intArrayOf(733680141, -1440926564, 447905675, 1806603117))) }
        val loader = MapReader()

        loader.load(region)
        val objects: Objects = get()
        val map = mutableMapOf<Int, MutableList<Location>>()
        objects.delegate.keys.forEach { tile ->
            val values = objects[tile]
            values?.forEach { loc ->
                val list = map.getOrPut(loc.id) { mutableListOf() }
                list.add(loc)
            }
        }
        map.forEach { (_, list) ->
            list.sortBy { localId(it.tile) }
        }
        val result = LocationWriter().write(map.toSortedMap())

        println(result.toList())

        val newLocations = ObjectFactory()

        val tileSettings = Array(4) { Array(64) { ByteArray(64) } }
        LocationReader(newLocations).read(region.tile, result, tileSettings)
    }

}