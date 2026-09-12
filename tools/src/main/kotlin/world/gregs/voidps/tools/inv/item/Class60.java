package world.gregs.voidps.tools.inv.item;/* Class60 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class60 {
    private final int anInt1084;
    static int anInt1085;
    private int anInt1086;
    static int anInt1090;
    static int anInt1092;
    static int anInt1095;
    private Queue aQueue_1089 = new Queue();
    static int anInt1102;
    static int anInt1103;
    private final IterableHashTable aIterableHashTable_1100;

    Class60(int i) {
        this(i, i);
    }

    final void method580(int i, Object object, long l, int i_5_) {
        try {
            anInt1092++;
            if (i_5_ > anInt1084) throw new IllegalStateException("s>cs");
            method586(l, 0);
            anInt1086 -= i_5_;
            while (anInt1086 < 0) {
                Class348_Sub42_Sub8 class348_sub42_sub8 = ((Class348_Sub42_Sub8) aQueue_1089.method1008(i ^ 0x7c8a));
                method585(class348_sub42_sub8, i ^ ~0x7cfa);
            }
            Class348_Sub42_Sub8_Sub2 class348_sub42_sub8_sub2 = new Class348_Sub42_Sub8_Sub2(object, i_5_);
            aIterableHashTable_1100.put((byte) 54, l, class348_sub42_sub8_sub2);
            if (i != 31902) anInt1086 = -106;
            aQueue_1089.add(true, class348_sub42_sub8_sub2);
            class348_sub42_sub8_sub2.key2 = 0L;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("jr.E(" + i + ',' + (object != null ? "{...}" : "null") + ',' + l + ',' + i_5_ + ')'));
        }
    }

    final void method582(Object object, long l, byte i) {
        try {
            if (i >= -92) method589(null, -7);
            anInt1095++;
            method580(31902, object, l, 1);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("jr.B(" + (object != null ? "{...}" : "null") + ',' + l + ',' + i + ')'));
        }
    }

    final Object method583(long l, int i) {
        try {
            int i_6_ = -59 % ((i - 2) / 47);
            anInt1085++;
            Class348_Sub42_Sub8 class348_sub42_sub8 = (Class348_Sub42_Sub8) aIterableHashTable_1100.method3480(l, -6008);
            if (class348_sub42_sub8 == null) return null;
            Object object = class348_sub42_sub8.method3193(86);
            if (object == null) {
                class348_sub42_sub8.unlink((byte) 102);
                class348_sub42_sub8.unlink2(true);
                anInt1086 += class348_sub42_sub8.anInt9545;
                return null;
            }
            if (class348_sub42_sub8.method3195(-4)) {
                Class348_Sub42_Sub8_Sub2 class348_sub42_sub8_sub2 = new Class348_Sub42_Sub8_Sub2(object, (class348_sub42_sub8.anInt9545));
                aIterableHashTable_1100.put((byte) 90, (class348_sub42_sub8.aLong4291), class348_sub42_sub8_sub2);
                aQueue_1089.add(true, class348_sub42_sub8_sub2);
                class348_sub42_sub8_sub2.key2 = 0L;
                class348_sub42_sub8.unlink((byte) 112);
                class348_sub42_sub8.unlink2(true);
            } else {
                aQueue_1089.add(true, class348_sub42_sub8);
                class348_sub42_sub8.key2 = 0L;
            }
            return object;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, "jr.K(" + l + ',' + i + ')');
        }
    }

    private final void method585(Class348_Sub42_Sub8 class348_sub42_sub8, int i) {
        int i_8_ = 80 / ((i - 6) / 36);
        anInt1102++;
        if (class348_sub42_sub8 != null) {
            class348_sub42_sub8.unlink((byte) 117);
            class348_sub42_sub8.unlink2(true);
            anInt1086 += class348_sub42_sub8.anInt9545;
        }
    }

    private final void method586(long l, int i) {
        try {
            if (i != 0) aQueue_1089 = null;
            anInt1090++;
            Class348_Sub42_Sub8 class348_sub42_sub8 = (Class348_Sub42_Sub8) aIterableHashTable_1100.method3480(l, -6008);
            method585(class348_sub42_sub8, -57);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, "jr.J(" + l + ',' + i + ')');
        }
    }

    static final boolean method589(Class42 class42, int i) {
        anInt1103++;
        if (class42 == null) return false;
        if (i != -4) return false;
        if (!class42.aBoolean574) return false;
        if (!class42.method373(Class75.anInterface17_1244, i ^ ~0x2d)) return false;
        if (Class158.aIterableHashTable_4934.method3480(class42.anInt581, i ^ 0x1774) != null) return false;
        return KeyedHardReferenceNode.aIterableHashTable_10442.method3480(class42.anInt596, i + -6004) == null;
    }

    Class60(int i, int i_10_) {
        anInt1086 = i;
        anInt1084 = i;
        int i_11_;
        for (i_11_ = 1; i > i_11_ + i_11_ && i_10_ > i_11_; i_11_ += i_11_) {
            /* empty */
        }
        aIterableHashTable_1100 = new IterableHashTable(i_11_);
    }
}
