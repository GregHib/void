package world.gregs.voidps.tools.icon

/* Class42 - minimal stub (missing from trimmed tree)
* See client/src/Class42.java for the full original. Only the fields and
* method373 needed by Class60.method589 are kept.
*/

internal class Class42 {
    var aBoolean574: Boolean = false
    private val anInt573 = -1
    var anInt581: Int = 0
    private val anInt583 = 0
    private val anInt585 = 0
    private var anInt586 = 0
    private val anInt587 = 0
    private val anInt590 = 0
    private val anInt592 = 0
    var anInt596: Int = 0
    private val anInt606 = 0

    fun method373(interface17: Interface17, i: Int): Boolean {
        anInt594++
        val i_1_: Int
        if (anInt606 == -1) {
            if (anInt590 == -1) return true
            i_1_ = interface17.method62(anInt590, -65536)
        } else i_1_ = interface17.method61(anInt606, (-16).toByte())
        if (i_1_ < anInt585 || i_1_ > anInt592) return false
        if (i < 26) anInt586 = 11
        val i_2_: Int
        if (anInt583 == -1) {
            if (anInt573 != -1) i_2_ = interface17.method62(anInt573, -65536)
            else return true
        } else i_2_ = interface17.method61(anInt583, (-16).toByte())
        return i_2_ >= anInt587 && i_2_ <= anInt586
    }

    companion object {
        var anInt594: Int = 0
    }
}
