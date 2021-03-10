package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.SPRITES
import world.gregs.voidps.cache.definition.data.IndexedSprite
import world.gregs.voidps.cache.definition.data.SpriteDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class SpriteDecoder(cache: world.gregs.voidps.cache.Cache) : DefinitionDecoder<SpriteDefinition>(cache, SPRITES) {

    override val size: Int
        get() = cache.lastArchiveId(index)

    override fun create() = SpriteDefinition()

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
        repeat(size) { index ->
            sprites[index].offsetX = buffer.readShort()
        }
        repeat(size) { index ->
            sprites[index].offsetY = buffer.readShort()
        }
        repeat(size) { index ->
            sprites[index].width = buffer.readShort()
        }
        repeat(size) { index ->
            sprites[index].height = buffer.readShort()
        }
        repeat(size) { index ->
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
        repeat(size) { index ->
            sprites[index].palette = palette
        }

        buffer.position(0)
        repeat(size) { index ->
            val sprite = sprites[index]
            val area = sprite.width * sprite.height

            sprite.raster = ByteArray(area)

            val setting: Int = buffer.readUnsignedByte()
            if (setting and 0x2 == 0) {
                if (setting and 0x1 == 0) {
                    repeat(area) { pixel ->
                        sprite.raster[pixel] = buffer.readByte().toByte()
                    }
                } else {
                    repeat(sprite.width) { x ->
                        repeat(sprite.height) { y ->
                            sprite.raster[x + y * sprite.width] = buffer.readByte().toByte()
                        }
                    }
                }
            } else {
                var transparent = false
                val alpha = ByteArray(area)
                if (setting and 0x1 == 0) {
                    repeat(area) { pixel ->
                        sprite.raster[pixel] = buffer.readByte().toByte()
                    }
                    repeat(area) { pixel ->
                        alpha[pixel] = buffer.readByte().toByte()
                        val p = alpha[pixel].toInt()
                        transparent = transparent or (p != -1)
                    }
                } else {
                    repeat(sprite.width) { x ->
                        repeat(sprite.height) { y ->
                            sprite.raster[x + y * sprite.width] = buffer.readByte().toByte()
                        }
                    }
                    repeat(sprite.width) { x ->
                        repeat(sprite.height) { y ->
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