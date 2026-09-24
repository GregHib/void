package world.gregs.voidps.tools.icon

/* Class348_Sub17 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class348_Sub17 {
    var anInt6789: Int = 0

    fun method2929(throwable: Throwable, string: String?): RuntimeException_Sub1 {
        anInt6789++
        throwable.printStackTrace()
        val runtimeexception_sub1: RuntimeException_Sub1
        if (throwable is RuntimeException_Sub1) {
            runtimeexception_sub1 = throwable
            runtimeexception_sub1.aString4594 += ' '.toString() + string
        } else runtimeexception_sub1 = RuntimeException_Sub1(throwable, string)
        return runtimeexception_sub1
    }
}
