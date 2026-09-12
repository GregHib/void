package world.gregs.voidps.tools.inv.item;/* Class169 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.net.InetAddress;

final class Class169 implements Runnable {
    private Class262 aClass262_2258 = new Class262();
    private Thread aThread2259 = new Thread(this);
    static int anInt2263;

    public final void run() {
        anInt2263++;
        for (; ; ) {
            Class348_Sub26 class348_sub26;
            synchronized (aClass262_2258) {
                Class348 class348;
                for (class348 = aClass262_2258.method1997(8); class348 == null; class348 = aClass262_2258.method1997(8)) {
                    try {
                        aClass262_2258.wait();
                    } catch (InterruptedException interruptedexception) {
                        /* empty */
                    }
                }
                if (!(class348 instanceof Class348_Sub26)) break;
                class348_sub26 = (Class348_Sub26) class348;
            }
            int i;
            try {
                byte[] is = InetAddress.getByName(class348_sub26.aString6888).getAddress();
//                i = jagmisc.ping(is[0], is[1], is[2], is[3], 1000L);
                 i = -1;
            } catch (Throwable throwable) {
                i = 1000;
            }
            class348_sub26.anInt6887 = i;
        }
    }

    public Class169() {
        aThread2259.setDaemon(true);
        aThread2259.start();
    }
}
