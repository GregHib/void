package world.gregs.voidps.tools.icon

import java.util.zip.Inflater

/* Class152 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class152 private constructor(i: Int, i_6_: Int, i_7_: Int) {
    private var anInflater2072: Inflater? = null

    constructor() : this(-1, 1000000, 1000000)

    fun method1218(`is`: ByteArray?, i: Int, packet: Packet?) {
        try {
            if (i != 29123) Companion.method1217(-91, null)
            anInt2073++
            if ((packet!!.aByteArray7154[packet.pos]).toInt() != 31 || (packet.aByteArray7154[1 + packet.pos]).toInt() != -117) throw RuntimeException("Invalid GZIP header!")
            if (anInflater2072 == null) anInflater2072 = Inflater(true)
            try {
                anInflater2072!!.setInput(packet.aByteArray7154, packet.pos - -10, -8 - (10 + packet.pos - (packet.aByteArray7154).size))
                anInflater2072!!.inflate(`is`)
            } catch (exception: Exception) {
                anInflater2072!!.reset()
                throw RuntimeException("Invalid GZIP compressed data!")
            }
            anInflater2072!!.reset()
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("ol.A(" + (if (`is` != null) "{...}" else "null") + ',' + i + ',' + (if (packet != null) "{...}" else "null") + ')'))
        }
    }

    companion object {
        var anInt2070: Int = 0
        var anInt2073: Int = 0

        fun method1217(i: Int, class357s: Array<Array<Array<Class357?>?>?>?) {
            anInt2070++
            for (i_2_ in i..<class357s!!.size) {
                val class357s_3_: Array<Array<Class357?>?> = class357s[i_2_]!!
                for (i_4_ in class357s_3_.indices) {
                    var i_5_ = 0
                    while ((class357s_3_[i_4_]!!.size > i_5_)) {
                        val class357 = class357s_3_[i_4_]!![i_5_]
                        if (class357 != null) {
                            var class148 = class357.aClass148_4396
                            while (class148 != null) {
                                val class318_sub1_sub3 = (class148.aClass318_Sub1_Sub3_2040)
                                if (class318_sub1_sub3 is Interface10) (class318_sub1_sub3 as Interface10).method40(i xor 0x2efe.inv())
                                class148 = class148.aClass148_2038
                            }
                        }
                        i_5_++
                    }
                }
            }
        }
    }
}
