package world.gregs.voidps.tools.inv.item;/* Class62 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class62 {
    static int anInt1113;

    static final synchronized long method599(int i) {
        if (i > -52) return -121L;
        anInt1113++;
        long l = System.currentTimeMillis();
        if (Matrix_Sub1.aLong5663 > l) Class318_Sub1_Sub1.aLong8728 += Matrix_Sub1.aLong5663 - l;
        Matrix_Sub1.aLong5663 = l;
        return l + Class318_Sub1_Sub1.aLong8728;
    }
}
