package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.QUICK_CHAT_MENUS
import world.gregs.voidps.cache.Indices.QUICK_CHAT_MESSAGES
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition

class QuickChatPhraseDecoder(cache: world.gregs.voidps.cache.Cache) : DefinitionDecoder<QuickChatPhraseDefinition>(cache, QUICK_CHAT_MESSAGES) {

    override fun create() = QuickChatPhraseDefinition()

    override fun getArchive(id: Int) = 1

    override val last: Int
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

    fun method3113(c: Char, string: String): Array<String> {
        val length: Int = method257(string, c)
        val strings = arrayOfNulls<String>(length - -1)
        var index = 0
        var start = 0
        for (i in 0 until length) {
            var end: Int = start
            while (string[end] != c) {
                end++
            }
            strings[index++] = string.substring(start, end)
            start = 1 + end
        }
        strings[length] = string.substring(start)
        return strings.filterNotNull().toTypedArray()
    }

    fun method257(string: String, c: Char): Int {
        var count = 0
        val length = string.length
        var index = 0
        while (length > index) {
            if (c == string[index]) count++
            index++
        }
        return count
    }

    override fun QuickChatPhraseDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> stringParts = method3113('<', buffer.readString())
            2 -> {
                val length = buffer.readUnsignedByte()
                options = IntArray(length)
                repeat(length) { count ->
                    options!![count] = buffer.readShort()
                }
            }
            3 -> {
                val length: Int = buffer.readUnsignedByte()
                ids = Array(length) { intArrayOf() }
                types = IntArray(length)
                repeat(length) { count ->
                    val code: Int = buffer.readUnsignedShort()
                    val class138 = getTypeInstance(code)
                    if (class138 != null) {
                        types!![count] = code
                        ids!![count] = IntArray(class138.length)
                        for (i in 0 until class138.length) {
                            ids!![count][i] = buffer.readUnsignedShort()
                        }
                    }
                }
            }
        }
    }

    class Class138(var id: Int, var anInt1945: Int, var bitCount: Int, var length: Int)


    companion object {
        val enum1 = Class138(0, 2, 2, 1) // I need: option
        val item1 = Class138(1, 2, 2, 0) // bring item
        val aClass138_4062 = Class138(2, 4, 4, 0)
        val aClass138_2885 = Class138(4, 1, 1, 1) // skill level
        val enum2 = Class138(6, 0, 4, 2) // slayer assignment
        val enum3 = Class138(7, 0, 1, 1) // clan rank
        val aClass138_4082 = Class138(8, 0, 4, 1)// quest points, fog rating, credit total
        val aClass138_8553 = Class138(9, 0, 4, 1) // sc points, penguins, investment credirts, champs, raw shards, life points
        val item2 = Class138(10, 2, 2, 0) // borrow item, g.e prices
        val enum4 = Class138(11, 0, 1, 2) // big int (experience)
        val aClass138_4901 = Class138(12, 0, 1, 0)
        val aClass138_10194 = Class138(13, 0, 1, 0) // combat level avg
        val aClass138_9781 = Class138(14, 0, 4, 1) // avatar level, avatar percentage hp
        val aClass138_9860 = Class138(15, 0, 1, 0)// combat level
        private val class138s = arrayOf(enum1,
            item1,
            aClass138_4062,
            aClass138_2885,
            enum2,
            enum3,
            aClass138_4082,
            aClass138_8553,
            item2,
            enum4,
            aClass138_4901,
            aClass138_10194,
            aClass138_9781,
            aClass138_9860)
        fun getTypeInstance(index: Int): Class138? {
            return class138s.firstOrNull { it.id == index }
        }
    }

    fun getMagicString(enums: EnumDecoder, items: ItemDecoder, key: Int, ids: IntArray?, type: Class138?): String? {
        return try {
            if (enum1 === type) {
                val enum: EnumDefinition = enums.get(ids!![0])
                return enum.getString(key)
            }
            if (item1 === type || item2 === type) {
                val item = items.get(key)
                return item.name
            }
            if (type === enum2 || type === enum3 || type === enum4) {
                enums.get(ids!![0]).getString(key)
            } else {
                key.toString()
            }
        } catch (runtimeexception: RuntimeException) {
            runtimeexception.printStackTrace()
            throw RuntimeException("hu.A(" + ',' + key + ',' + (if (type != null) "{...}" else "null") + ',' + (if (ids != null) "{...}" else "null") + ')')
        }
    }

    override fun QuickChatPhraseDefinition.changeValues() {
        if (id >= 32768) {
            if (options != null) {
                repeat(options!!.size) { count ->
                    options!![count] = options!![count] or 32768
                }
            }
        }
    }
}