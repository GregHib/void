package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub21 - minimal stub (missing from trimmed tree)
* See client/src/Class348_Sub40_Sub21.java for the full original.
*/

internal object Class348_Sub40_Sub21 : Class348_Sub40(0, false) {
    var crc64table: LongArray

    init {
        crc64table = LongArray(256)
        for (i in 0..255) {
            var l = i.toLong()
            for (i_24_ in 0..7) {
                if ((0x1L and l) == 1L) l = 0x3693a86a2878f0bdL.inv() xor (l ushr 1)
                else l = l ushr 1
            }
            crc64table[i] = l
        }
    }
}
