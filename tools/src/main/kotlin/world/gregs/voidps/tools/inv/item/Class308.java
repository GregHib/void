package world.gregs.voidps.tools.inv.item;/* Class308 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class308 {
    static int anInt3881;
    static int anInt3885;
    private Class348_Sub42 aClass348_Sub42_3887 = new Class348_Sub42();
    private final IterableHashTable aIterableHashTable_3888;
    private Queue aQueue_3889 = new Queue();
    private final int anInt3890;
    private int anInt3891;

    final Class348_Sub42 method2302(long l, byte i) {
        try {
            if (i > -25) aQueue_3889 = null;
            anInt3885++;
            Class348_Sub42 class348_sub42 = (Class348_Sub42) aIterableHashTable_3888.method3480(l, -6008);
            if (class348_sub42 != null) aQueue_3889.add(true, class348_sub42);
            return class348_sub42;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, "wu.D(" + l + ',' + i + ')');
        }
    }

    final void method2305(long l, Class348_Sub42 class348_sub42, int i) {
        try {
            anInt3881++;
            if ((~anInt3891) == i) {
                Class348_Sub42 class348_sub42_0_ = aQueue_3889.method1008(20);
                class348_sub42_0_.unlink((byte) 113);
                class348_sub42_0_.unlink2(true);
                if (class348_sub42_0_ == aClass348_Sub42_3887) {
                    class348_sub42_0_ = aQueue_3889.method1008(20);
                    class348_sub42_0_.unlink((byte) 79);
                    class348_sub42_0_.unlink2(true);
                }
            } else anInt3891--;
            aIterableHashTable_3888.put((byte) 37, l, class348_sub42);
            aQueue_3889.add(true, class348_sub42);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("wu.E(" + l + ',' + (class348_sub42 != null ? "{...}" : "null") + ',' + i + ')'));
        }
    }

    Class308(int i) {
        anInt3891 = i;
        anInt3890 = i;
        int i_1_;
        for (i_1_ = 1; i_1_ + i_1_ < i; i_1_ += i_1_) {
            /* empty */
        }
        aIterableHashTable_3888 = new IterableHashTable(i_1_);
    }
}
