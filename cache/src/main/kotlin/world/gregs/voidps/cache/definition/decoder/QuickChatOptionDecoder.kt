package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.Unicode
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.QUICK_CHAT_MENUS
import world.gregs.voidps.cache.Index.QUICK_CHAT_MESSAGES
import world.gregs.voidps.cache.definition.data.QuickChatOptionDefinition

class QuickChatOptionDecoder : DefinitionDecoder<QuickChatOptionDefinition>(QUICK_CHAT_MESSAGES) {

    override fun create(size: Int) = Array(size) { QuickChatOptionDefinition(it) }

    override fun getArchive(id: Int) = 0

    override fun size(cache: Cache): Int {
        val lastArchive = cache.lastArchiveId(index)
        val lastArchive2 = cache.lastArchiveId(QUICK_CHAT_MENUS)
        return lastArchive * 256 + cache.lastFileId(index, lastArchive) + (lastArchive2 * 256 + cache.lastFileId(index, lastArchive2))
    }

    override fun load(definitions: Array<QuickChatOptionDefinition>, cache: Cache, id: Int) {
        val archive = getArchive(id)
        val file = getFile(id)
        val data = (
            if (file <= 0x7fff) {
                cache.data(index, archive, file)
            } else {
                cache.data(QUICK_CHAT_MENUS, archive, file and 0x7fff)
            }
            ) ?: return
        read(definitions, id, ArrayReader(data))
    }

    override fun QuickChatOptionDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> optionText = buffer.readString()
            2 -> {
                val length = buffer.readUnsignedByte()
                quickReplyOptions = IntArray(length)
                navigateChars = CharArray(length) { count ->
                    quickReplyOptions!![count] = buffer.readShort()
                    val b = buffer.readUnsignedByte()
                    if (b != 0) Unicode.byteToChar(b).toChar() else '\u0000'
                }
            }
            3 -> {
                val length = buffer.readUnsignedByte()
                dynamicData = IntArray(length)
                staticData = CharArray(length) { count ->
                    dynamicData!![count] = buffer.readShort()
                    val b = buffer.readUnsignedByte()
                    if (b != 0) Unicode.byteToChar(b).toChar() else '\u0000'
                }
            }
        }
    }

    override fun changeValues(definitions: Array<QuickChatOptionDefinition>, definition: QuickChatOptionDefinition) {
        if (definition.id >= 32768) {
            if (definition.dynamicData != null) {
                for (i in definition.dynamicData!!.indices) {
                    definition.dynamicData!![i] = definition.dynamicData!![i] or 32768
                }
            }
            if (definition.quickReplyOptions != null) {
                for (count in 0 until definition.quickReplyOptions!!.size) {
                    definition.quickReplyOptions!![count] = definition.quickReplyOptions!![count] or 32768
                }
            }
        }
    }
}
