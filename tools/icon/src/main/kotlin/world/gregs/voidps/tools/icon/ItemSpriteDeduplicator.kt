package world.gregs.voidps.tools.icon

import java.io.File
import javax.imageio.ImageIO

/**
 * Collapses the output of [CacheItemSpriteDumper] down to unique sprites.
 *
 * Reads every `<itemId>_<name>.png` in the input directory, compares decoded
 * pixels (not file bytes) and writes each distinct image once as
 * `<spriteId>.png`, plus a `item_sprites.txt` mapping of `itemId=spriteId`.
 * Sprite ids are assigned in ascending order of the lowest item id using them.
 *
 * Usage: `ItemSpriteDeduplicator [input directory] [output directory]`
 */
object ItemSpriteDeduplicator {

    private class Pixels(val width: Int, val height: Int, val data: IntArray) {
        private val hash = 31 * (31 * width + height) + data.contentHashCode()

        override fun hashCode() = hash

        override fun equals(other: Any?) = other is Pixels && other.width == width && other.height == height && other.data.contentEquals(data)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val input = File(if (args.isNotEmpty()) args[0] else "item_sprites")
        val output = File(if (args.size > 1) args[1] else "item_sprites_unique")
        val files = input.listFiles { file -> file.extension == "png" }
            ?.mapNotNull { file -> file.name.substringBefore('_').toIntOrNull()?.let { it to file } }
            ?.sortedBy { it.first }
        if (files == null) {
            System.err.println("No sprites found in ${input.absolutePath}")
            return
        }
        output.mkdirs()
        val sprites = HashMap<Pixels, Int>()
        val mapping = StringBuilder()
        for ((itemId, file) in files) {
            val image = ImageIO.read(file) ?: continue
            val pixels = Pixels(image.width, image.height, image.getRGB(0, 0, image.width, image.height, null, 0, image.width))
            val spriteId = sprites.getOrPut(pixels) {
                val id = sprites.size
                file.copyTo(File(output, "$id.png"), overwrite = true)
                id
            }
            mapping.append(itemId).append('=').append(spriteId).append('\n')
        }
        File(output, "item_sprites.txt").writeText(mapping.toString())
        println("Reduced ${files.size} item sprites to ${sprites.size} unique sprites in ${output.absolutePath}")
    }
}
