package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub18 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub18 private constructor(i: Int) : Class348_Sub40(0, false) {
    private var anInt9244 = 0
    private var anInt9250 = 0
    private var anInt9252 = 0

    constructor() : this(0)

    init {
        method3095(i)
    }

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_15_ = `is`!![0]
            val is_16_ = `is`[1]
            val is_17_ = `is`[2]
            for (i_18_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                is_15_[i_18_] = anInt9244
                is_16_[i_18_] = anInt9252
                is_17_[i_18_] = anInt9250
            }
        }
        return `is`
    }

    private fun method3095(i_19_: Int) {
        anInt9244 = 0xff0 and (i_19_ shr 12)
        anInt9252 = (i_19_ shr 4) and 0xff0
        anInt9250 = (i_19_ and 0xff) shl 4
    }

    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) method3095(packet!!.readMedium())
    }
}
