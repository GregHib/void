package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.QUICK_CHAT_MENUS
import world.gregs.voidps.cache.Index.QUICK_CHAT_MESSAGES
import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.cache.definition.data.QuickChatType

class QuickChatPhraseDecoder : DefinitionDecoder<QuickChatPhraseDefinition>(QUICK_CHAT_MESSAGES) {

    override fun getArchive(id: Int) = 1

    override fun create(size: Int) = Array(size) { QuickChatPhraseDefinition(it) }

    override fun readId(reader: Reader): Int {
        return reader.readShort()
    }

    override fun size(cache: Cache): Int {
        val lastArchive = cache.lastArchiveId(index)
        val lastArchive2 = cache.lastArchiveId(QUICK_CHAT_MENUS)
        return lastArchive * 256 + cache.lastFileId(index, lastArchive) + (lastArchive2 * 256 + cache.lastFileId(index, lastArchive2))
    }

    /*override fun getData(archive: Int, file: Int): ByteArray? {
        return if (file < 32768) {
            super.getData(archive, file)
        } else {
            cache.getFile(QUICK_CHAT_MENUS, archive, file and 0x7fff)
        }
    }*/

    override fun QuickChatPhraseDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> stringParts = buffer.readString().split('<').toTypedArray()
            2 -> responses = IntArray(buffer.readUnsignedByte()) { buffer.readShort() }
            3 -> {
                val length: Int = buffer.readUnsignedByte()
                ids = Array(length) { intArrayOf() }
                types = IntArray(length) { count ->
                    val type: Int = buffer.readUnsignedShort()
                    val size = QuickChatType.getType(type)?.length ?: 0
                    ids!![count] = IntArray(size) { buffer.readUnsignedShort() }
                    type
                }
            }
        }
    }

    override fun changeValues(definitions: Array<QuickChatPhraseDefinition>, definition: QuickChatPhraseDefinition) {
        val options = definition.responses
        if (definition.id >= 32768 && options != null) {
            for (index in options.indices) {
                options[index] = options[index] or 32768
            }
        }
    }
}