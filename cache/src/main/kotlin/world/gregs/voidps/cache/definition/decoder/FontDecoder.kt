package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.FONT_METRICS
import world.gregs.voidps.cache.definition.data.FontDefinition
import kotlin.math.max
import kotlin.math.min

class FontDecoder : DefinitionDecoder<FontDefinition>(FONT_METRICS) {

    override fun size(cache: Cache): Int = cache.lastArchiveId(index)

    override fun create(size: Int) = Array(size) { FontDefinition(it) }

    override fun getFile(id: Int) = 0

    override fun readLoop(definition: FontDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun FontDefinition.read(opcode: Int, buffer: Reader) {
        if (buffer.readUnsignedByte() != 0) {
            return
        }
        val hasTable = buffer.readUnsignedBoolean()
        glyphWidths = ByteArray(256)
        buffer.readBytes(glyphWidths)
        if (hasTable) {
            val rightOffsets = IntArray(256) { buffer.readUnsignedByte() }
            val leftOffsets = IntArray(256) { buffer.readUnsignedByte() }
            val rightGlyphData = glyphData(buffer, rightOffsets)
            val leftGlyphData = glyphData(buffer, leftOffsets)
            kerningAdjustments = Array(256) { left ->
                ByteArray(256) { right ->
                    calculateKerning(glyphWidths, right, left, rightGlyphData, leftGlyphData, leftOffsets, rightOffsets).toByte()
                }
            }
            verticalSpacing = leftOffsets[32] + rightOffsets[32]
        } else {
            verticalSpacing = buffer.readUnsignedByte()
        }
        buffer.skip(2)
        topPadding = buffer.readUnsignedByte()
        bottomPadding = buffer.readUnsignedByte()
    }

    companion object {
        private fun calculateKerning(
            widths: ByteArray,
            rightGlyphIndex: Int,
            leftGlyphIndex: Int,
            rightGlyphData: Array<ByteArray>,
            leftGlyphData: Array<ByteArray>,
            leftOffsets: IntArray,
            rightOffsets: IntArray,
        ): Int {
            if (leftGlyphIndex == 32 || leftGlyphIndex == 160 || rightGlyphIndex == 32 || rightGlyphIndex == 160) {
                return 0
            }
            val leftOffset = leftOffsets[leftGlyphIndex]
            val leftLimit = rightOffsets[leftGlyphIndex] + leftOffset
            val rightOffset = leftOffsets[rightGlyphIndex]
            val rightLimit = rightOffsets[rightGlyphIndex] + rightOffset
            val minOffset = max(leftOffset, rightOffset)
            val maxLimit = min(rightLimit, leftLimit)
            var minDelta = widths[leftGlyphIndex].toInt() and 0xff
            if (minDelta > widths[rightGlyphIndex].toInt() and 0xff) {
                minDelta = widths[rightGlyphIndex].toInt() and 0xff
            }
            val leftGlyph = leftGlyphData[leftGlyphIndex]
            val rightGlyph = rightGlyphData[rightGlyphIndex]
            var leftIndex = leftOffset - minOffset
            var rightIndex = rightOffset - minOffset
            for (index in minOffset until maxLimit) {
                val delta = rightGlyph[rightIndex++] + leftGlyph[leftIndex++]
                if (delta < minDelta) {
                    minDelta = delta
                }
            }
            return -minDelta
        }
        private fun glyphData(buffer: Reader, offsets: IntArray) = Array(256) { index ->
            var total = 0
            ByteArray(offsets[index]) {
                total += buffer.readByte()
                total.toByte()
            }
        }
    }
}
