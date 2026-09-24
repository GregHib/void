package world.gregs.voidps.tools.icon

/* Class348_Sub41 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub41 : Class348() {
    var anInt7050: Int = 0

    companion object {
        var anInt7046: Int = 0
        var aClass114_7052: Class114? = Class114(86, 6)
        var anInt7055: Int = 0

        /* NOTE: method3156 is NOT in the genuine-methods list (0 JaCoCo hits) -
     * its only caller in method3158 is guarded by "if (i > -74)" which is
     * never true for the (i == -105 / i == -120) call sites reachable from
     * this renderer. Stubbed rather than pulling in Class239_Sub26 for a
     * call that never actually executes. */
        fun method3156(bool: Boolean, string: String?): Int {
            anInt7055++
            if (bool != true) aClass114_7052 = null
            throw IllegalStateException() // unreachable per JaCoCo coverage
        }

        fun decodeContainer(`is`: ByteArray?, i: Int): ByteArray {
            anInt7046++
            val packet = Packet(`is`!!)
            val i_37_ = packet.readUnsignedByte(255)
            if (i > -74) method3156(true, null)
            val i_38_ = packet.readInt((-126).toByte())
            if (i_38_ < 0 || (Class29.anInt401 != 0 && i_38_ > Class29.anInt401)) {
                throw RuntimeException()
            }
            if (i_37_ != 0) {
                /* Corrected: this branch IS reachable in practice (the
             * materials/idx26 archive is compressed) - confirmed by an
             * actual run against the real cache, which hit this path and
             * threw the placeholder IllegalStateException a prior pass left
             * here on the (incorrect) assumption this was dead per JaCoCo.
             * Restored the real gzip/bzip2 dispatch verbatim. */
                val i_39_ = packet.readInt((-126).toByte())
                if (i_39_ < 0 || (Class29.anInt401 != 0 && i_39_ > Class29.anInt401) || i_39_ > 10000000) {
                    return ByteArray(4)
                }
                val is_40_ = ByteArray(i_39_)
                if (i_37_ == 1) Class212.method1547(is_40_, i_39_, `is`, i_38_, 9)
                else {
                    synchronized(Class348_Sub33.aClass152_6955) {
                        Class348_Sub33.aClass152_6955.method1218(is_40_, 29123, packet)
                    }
                }
                return is_40_
            }
            val is_41_ = ByteArray(i_38_)
            packet.gdata(2147483647, 0, i_38_, is_41_)
            return is_41_
        }
    }
}
