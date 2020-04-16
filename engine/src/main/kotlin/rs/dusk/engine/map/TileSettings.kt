package rs.dusk.engine.map

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
typealias TileSettings = Array<Array<ByteArray>>

val tileModule = module {
    single { TileLoader() }
}

fun TileSettings.isTile(plane: Int, localX: Int, localY: Int, flag: Int) =
    this[plane][localX][localY].toInt() and flag == flag

const val BLOCKED_TILE = 0x1
const val BRIDGE_TILE = 0x2