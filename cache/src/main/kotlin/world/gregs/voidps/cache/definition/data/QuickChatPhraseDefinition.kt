package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder.Companion.getTypeInstance

@Suppress("ArrayInDataClass")
data class QuickChatPhraseDefinition(
    override var id: Int = -1,
    var stringParts: Array<String>? = null,
    var options: IntArray? = null, // cs method 5056 for length, 5057 for data at index    cs2method5056(cs2method5012(int18)),  cs2method5057(qcphrase5, VARC[128])
    var ids: Array<IntArray>? = null,
    var types: IntArray? = null,
) : Definition {


    fun method3216(enums: EnumDecoder, items: ItemDecoder, decoder: QuickChatPhraseDecoder, buffer: BufferReader): String {
        val stringBuffer = StringBuffer(80)
//        println("Read " + Arrays.toString(anIntArray9574))
//        println("Options " + Arrays.toString(aStringArray9564))
        if (types != null) {
            for (index in types!!.indices) {
                stringBuffer.append(stringParts!![index])
                val i = types!![index]
                val count = getTypeInstance(i)!!.bitCount - 1

//                println("anInt1943: $i")
                val key = buffer.readBits(count) // TODO if we're encoding a string here, won't it need to be a long?
//                println("Read: $l")
                val ids = ids!![index]
                val type = method3212(index)
                stringBuffer.append(decoder.getMagicString(enums, items, key, ids, type))
            }
        }
        stringBuffer.append(stringParts!![stringParts!!.lastIndex])
        return stringBuffer.toString()
    }

    fun method3212(index: Int): QuickChatPhraseDecoder.Class138? {
        return if (types == null || index < 0 || types!!.size < index) null else getTypeInstance(types!![index])
    }

    override fun toString(): String {
        return "QuickChatOptionDefinition(id=$id, stringParts=${stringParts?.contentToString()}, options=${options?.contentToString()}, ids=${ids?.contentDeepToString()}, types=${types?.contentToString()})"
    }


}