package rs.dusk.engine.map

import org.koin.dsl.module
import rs.dusk.engine.client.send
import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.map.location.Xtea
import rs.dusk.engine.map.location.Xteas
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.utility.inject

val loginTestModule = module {
    single(createdAtStart = true) { LoginTest() }
}

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class LoginTest {

    val xteas: Xteas by inject()

    private fun forNearbyRegions(chunkX: Int, chunkY: Int, mapHash: Int, action: (Int) -> Unit) {
        for (regionX in (chunkX - mapHash) / 8..(chunkX + mapHash) / 8) {
            for (regionY in (chunkY - mapHash) / 8..(chunkY + mapHash) / 8) {
                action(regionY + (regionX shl 8))
            }
        }
    }

    init {
        GameLoginMessage verify {
            val list = mutableListOf<Xtea>()

            val chunkX = 235
            val chunkY = 397

            forNearbyRegions(chunkX, chunkY, 6) { regionId ->
                val xtea = xteas[regionId] ?: IntArray(4)
                list.add(xtea)
            }
            it.send(
                MapRegionMessage(
                    chunkX = chunkX,
                    chunkY = chunkY,
                    forceReload = false,
                    mapSize = 0,
                    xteas = list.toTypedArray(),
                    positions = intArrayOf(12342),
                    location = 30903402
                )
            )
        }
    }
}