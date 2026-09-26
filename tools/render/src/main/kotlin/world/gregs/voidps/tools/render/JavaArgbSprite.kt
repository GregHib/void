package world.gregs.voidps.tools.render

/* Class105_Sub3_Sub3 */

internal class JavaArgbSprite : Sprite_Sub3 {
    var anIntArray9936: IntArray

    constructor(var_ha_Sub1: JavaToolkit?, i: Int, i_443_: Int) : super(var_ha_Sub1, i, i_443_) {
        this.anIntArray9936 = IntArray(i * i_443_)
    }

    constructor(var_ha_Sub1: JavaToolkit?, `is`: IntArray, i: Int, i_1_: Int, i_2_: Int, i_3_: Int) : super(var_ha_Sub1, i_2_, i_3_) {
        var i = i
        var i_1_ = i_1_
        this.anIntArray9936 = IntArray(i_2_ * i_3_)
        i_1_ -= this.anInt8471
        var i_4_ = 0
        for (i_5_ in 0..<i_3_) {
            for (i_6_ in 0..<i_2_) this.anIntArray9936[i_4_++] = `is`[i++]
            i += i_1_
        }
    }
    override fun method964(i: Int, i_148_: Int, i_149_: Int, i_150_: Int, i_151_: Int) {
        throw IllegalStateException()
    }
    override fun method996(i: Int, i_444_: Int, i_445_: Int, i_446_: Int, i_447_: Int, i_448_: Int, i_449_: Int, i_450_: Int, i_451_: Int) {
        throw IllegalStateException()
    }
}
