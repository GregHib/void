package rs.dusk.engine.model.world.map.tile

import rs.dusk.core.io.write.BufferWriter

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class TileWriter {

    fun write(data: TileSettings): ByteArray {
        val writer = BufferWriter()
        for (plane in data.indices) {
            for (localX in data[plane].indices) {
                for (localY in data[plane][localX].indices) {
                    val setting = data[plane][localX][localY].toInt()
//                    if(height != 0) {
//                        writer.writeByte(1)
//                        writer.writeByte(height)
//                    } else {
//                        if(attr != 0) {
//                            writer.writeByte(attr)
//                            writer.writeByte(overlayId)
//                        }
                    if (setting != 0) {
                        writer.writeByte(setting + 49)
                    }
//                        if(underlayId != 0) {
//                            writer.writeByte(underlayId + 81)
//                        }
//                    }
                    writer.writeByte(0)
                }
            }
        }
        return writer.toArray()
    }
}