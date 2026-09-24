package world.gregs.voidps.tools.icon

import java.net.InetAddress

/* Class169 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class169 : Runnable {
    private val aClass262_2258 = Class262()
    private val aThread2259 = Thread(this)
    override fun run() {
        anInt2263++
        while (true) {
            val class348_sub26: Class348_Sub26?
            synchronized(aClass262_2258) {
                var class348: Class348?
                class348 = aClass262_2258.method1997(8)
                while (class348 == null) {
                    try {
                        (aClass262_2258 as Object).wait()
                    } catch (interruptedexception: InterruptedException) {
                        /* empty */
                    }
                    class348 = aClass262_2258.method1997(8)
                }
                if (class348 !is Class348_Sub26) break
                class348_sub26 = class348
            }
            var i: Int
            try {
                val `is` = InetAddress.getByName(class348_sub26!!.aString6888).getAddress()
                //                i = jagmisc.ping(is[0], is[1], is[2], is[3], 1000L);
                i = -1
            } catch (throwable: Throwable) {
                i = 1000
            }
            class348_sub26!!.anInt6887 = i
        }
    }

    init {
        aThread2259.setDaemon(true)
        aThread2259.start()
    }

    companion object {
        var anInt2263: Int = 0
    }
}
