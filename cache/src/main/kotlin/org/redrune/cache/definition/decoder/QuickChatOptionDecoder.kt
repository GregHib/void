package org.redrune.cache.definition.decoder

import org.redrune.cache.DefinitionDecoder
import org.redrune.cache.Indices.QUICK_CHAT_MENUS
import org.redrune.cache.Indices.QUICK_CHAT_MESSAGES
import org.redrune.cache.definition.data.QuickChatOptionDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class QuickChatOptionDecoder : DefinitionDecoder<QuickChatOptionDefinition>(QUICK_CHAT_MESSAGES) {

    override fun create() = QuickChatOptionDefinition()

    override fun getArchive(id: Int) = 0

    override val size: Int
        get() {
            val lastArchive = cache.lastArchiveId(index)
            val lastArchive2 = cache.lastArchiveId(QUICK_CHAT_MENUS)
            return lastArchive * 256 + cache.lastFileId(index, lastArchive) + (lastArchive2 * 256 + cache.lastFileId(index, lastArchive2))
        }

    override fun getData(archive: Int, file: Int): ByteArray? {
        return if (file < 32768) {
            super.getData(archive, file)
        } else {
            cache.getFile(QUICK_CHAT_MENUS, archive, file and 0x7fff)
        }
    }

    override fun QuickChatOptionDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> optionText = buffer.readString()
            2 -> {
                val length = buffer.readUnsignedByte()
                quickReplyOptions = IntArray(length)
                navigateChars = CharArray(length)
                repeat(length) { count ->
                    quickReplyOptions!![count] = buffer.readShort()
                    val b = buffer.readByte().toByte()
                    navigateChars!![count] = if (b.toInt() != 0) byteToChar(b) else '\u0000'
                }
            }
            3 -> {
                val length = buffer.readUnsignedByte()
                dynamicData = IntArray(length)
                staticData = CharArray(length)
                repeat(length) { count ->
                    dynamicData!![count] = buffer.readShort()
                    val b = buffer.readByte().toByte()
                    staticData!![count] = if (b.toInt() != 0) byteToChar(b) else '\u0000'
                }
            }
        }
    }

    override fun QuickChatOptionDefinition.changeValues() {
        if (id >= 32768) {
            if (dynamicData != null) {
                for (i in dynamicData!!.indices) {
                    dynamicData!![i] = dynamicData!![i] or 32768
                }
            }
            if (quickReplyOptions != null) {
                repeat(quickReplyOptions!!.size) { count ->
                    quickReplyOptions!![count] = quickReplyOptions!![count] or 32768
                }
            }
        }
    }
}