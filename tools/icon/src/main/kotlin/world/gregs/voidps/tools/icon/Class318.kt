package world.gregs.voidps.tools.icon

/* Class318 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal open class Class318 {
    var aClass318_3970: Class318? = null
    var aClass318_3976: Class318? = null

    fun method2373(bool: Boolean) {
        anInt3975++
        if (this.aClass318_3976 != null) {
            this.aClass318_3976!!.aClass318_3970 = this.aClass318_3970
            this.aClass318_3970!!.aClass318_3976 = this.aClass318_3976
            this.aClass318_3970 = null
            if (bool == false) this.aClass318_3976 = null
        }
    }

    companion object {
        var anInt3975: Int = 0
    }
}
