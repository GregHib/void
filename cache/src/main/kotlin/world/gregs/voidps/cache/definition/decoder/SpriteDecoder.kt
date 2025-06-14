package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.SPRITES
import world.gregs.voidps.cache.definition.data.IndexedSprite
import world.gregs.voidps.cache.definition.data.SpriteDefinition

class SpriteDecoder : DefinitionDecoder<SpriteDefinition>(SPRITES) {

    override fun size(cache: Cache): Int = cache.lastArchiveId(index)

    override fun create(size: Int) = Array(size) { SpriteDefinition(it) }

    override fun getFile(id: Int) = 0

    override fun readLoop(definition: SpriteDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun SpriteDefinition.read(opcode: Int, buffer: Reader) {
        buffer.position(buffer.array().size - 2)
        val size: Int = buffer.readShort()
        buffer.position(buffer.array().size - 7 - size * 8)

        val offsetX: Int = buffer.readShort()
        val offsetY: Int = buffer.readShort()

        val paletteSize: Int = buffer.readUnsignedByte() + 1

        val sprites = Array(size) { IndexedSprite() }
        this.sprites = sprites
        for (index in 0 until size) {
            sprites[index].offsetX = buffer.readShort()
        }
        for (index in 0 until size) {
            sprites[index].offsetY = buffer.readShort()
        }
        for (index in 0 until size) {
            sprites[index].width = buffer.readShort()
        }
        for (index in 0 until size) {
            sprites[index].height = buffer.readShort()
        }
        for (index in 0 until size) {
            val sprite = sprites[index]
            sprite.deltaWidth = offsetX - sprite.width - sprite.offsetX
            sprite.deltaHeight = offsetY - sprite.height - sprite.offsetY
        }

        buffer.position(buffer.array().size - 7 - size * 8 - (paletteSize - 1) * 3)
        val palette = IntArray(paletteSize)
        for (index in 1 until paletteSize) {
            palette[index] = buffer.readUnsignedMedium()
            if (palette[index] == 0) {
                palette[index] = 1
            }
        }
        for (index in 0 until size) {
            sprites[index].palette = palette
        }

        buffer.position(0)
        for (index in 0 until size) {
            val sprite = sprites[index]
            val area = sprite.width * sprite.height

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
                val alpha = ByteArray(area)
                if (setting and 0x1 == 0) {
                    for (pixel in 0 until area) {
                        sprite.raster[pixel] = buffer.readByte().toByte()
                    }
                    for (pixel in 0 until area) {
                        alpha[pixel] = buffer.readByte().toByte()
                        val p = alpha[pixel].toInt()
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
                            alpha[x + y * sprite.width] = buffer.readByte().toByte()
                            val pixel = alpha[x + y * sprite.width].toInt()
                            transparent = transparent or (pixel != -1)
                        }
                    }
                }
                if (transparent) {
                    sprite.alpha = alpha
                }
            }
        }
    }
}
