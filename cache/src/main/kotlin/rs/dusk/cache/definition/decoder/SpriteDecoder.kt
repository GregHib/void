package rs.dusk.cache.definition.decoder

import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.SPRITES
import rs.dusk.cache.definition.data.IndexedSprite
import rs.dusk.cache.definition.data.SpriteDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class SpriteDecoder : DefinitionDecoder<SpriteDefinition>(SPRITES) {
    override fun create() = SpriteDefinition()

    override fun getFile(id: Int) = 0

    override fun readLoop(definition: SpriteDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun SpriteDefinition.read(opcode: Int, buffer: Reader) {
        buffer.buffer.position(buffer.buffer.array().size - 2)
        val size: Int = buffer.readShort()
        sprites = Array(size) { IndexedSprite() }
        buffer.buffer.position(buffer.buffer.array().size - 7 - size * 8)
        val offsetX: Int = buffer.readShort()
        val offsetY: Int = buffer.readShort()
        val paletteSize: Int = (buffer.readUnsignedByte() and 0xff) + 1
        for (index in 0 until size) {
            sprites[index]!!.offsetX = buffer.readShort()
        }
        for (index in 0 until size) {
            sprites[index]!!.offsetY = buffer.readShort()
        }
        for (index in 0 until size) {
            sprites[index]!!.width = buffer.readShort()
        }
        for (index in 0 until size) {
            sprites[index]!!.height = buffer.readShort()
        }
        for (index in 0 until size) {
            val class383 = sprites[index]
            class383!!.deltaWidth = offsetX - class383.width - class383.offsetX
            class383.deltaHeight = offsetY - class383.height - class383.offsetY
        }
        buffer.buffer.position(buffer.buffer.array().size - 7 - size * 8 - (paletteSize - 1) * 3)
        val palette = IntArray(paletteSize)
        for (index in 1 until paletteSize) {
            palette[index] = buffer.readMedium()
            if (palette[index] == 0) {
                palette[index] = 1
            }
        }
        for (index in 0 until size) {
            sprites[index]!!.palette = palette
        }
        buffer.buffer.position(0)
        for (index in 0 until size) {
            val sprite = sprites[index]
            val area = sprite!!.width * sprite.height
            sprite.raster = ByteArray(area)
            val setting: Int = buffer.readUnsignedByte()
            if (setting and 0x2 == 0) {
                if (setting and 0x1 == 0) {
                    for (pixel in 0 until area) {
                        sprite.raster[pixel] = buffer.readByte().toByte()
                    }
                } else {
                    for (x in 0 until sprite.width) {
                        for (y in 0 until sprite.height) {
                            sprite.raster[x + y * sprite.width] = buffer.readByte().toByte()
                        }
                    }
                }
            } else {
                var transparent = false
                sprite.alpha = ByteArray(area)
                if (setting and 0x1 == 0) {
                    for (pixel in 0 until area) {
                        sprite.raster[pixel] = buffer.readByte().toByte()
                    }
                    for (pixel in 0 until area) {
                        sprite.alpha!![pixel] = buffer.readByte().toByte()
                        val p = sprite.alpha!![pixel].toInt()
                        transparent = transparent or (p != -1)
                    }
                } else {
                    for (x in 0 until sprite.width) {
                        for (y in 0 until sprite.height) {
                            sprite.raster[x + y * sprite.width] = buffer.readByte().toByte()
                        }
                    }
                    for (x in 0 until sprite.width) {
                        for (y in 0 until sprite.height) {
                            sprite.alpha!![x + y * sprite.width] = buffer.readByte().toByte()
                            val pixel = sprite.alpha!![x + y * sprite.width].toInt()
                            transparent = transparent or (pixel != -1)
                        }
                    }
                }
                if (!transparent) {
                    sprite.alpha = null
                }
            }
        }
    }
}