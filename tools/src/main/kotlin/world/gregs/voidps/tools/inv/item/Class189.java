package world.gregs.voidps.tools.inv.item;/* Class189 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class189 {
    /* Minimal stub: Class64_Sub1's kept code (method623 area) only reads
     * these instance fields off a Class189 obtained from Class73.method742(...)
     * and constructs a Class6 from them. method1418/method1419 are restored
     * below because Class73.method742 (genuine) itself calls method1419 to
     * decode the cached data before returning it. */
    boolean aBoolean2522 = false;
    static int anInt2528;
    int anInt2525;
    int anInt2526;
    int anInt2530;
    boolean aBoolean2531 = false;
    static int anInt2532;
    int anInt2533 = 1;
    int anInt2534;

    private final void method1418(int i, int i_0_, Class348_Sub49 class348_sub49, byte i_1_) {
        if (i_1_ != 94) this.anInt2526 = -81;
        anInt2532++;
        if (i == 1) {
            this.anInt2525 = class348_sub49.readUnsignedShort(842397944);
            if (this.anInt2525 == 65535) this.anInt2525 = -1;
        } else if (i == 2) {
            this.anInt2526 = 1 + class348_sub49.readUnsignedShort(842397944);
            this.anInt2530 = class348_sub49.readUnsignedShort(842397944) - -1;
        } else if (i != 3) {
            if (i == 4) this.anInt2534 = class348_sub49.readUnsignedByte(255);
            else if (i != 5) {
                if (i == 6) this.aBoolean2522 = true;
                else if (i == 7) this.aBoolean2531 = true;
            } else this.anInt2533 = class348_sub49.readUnsignedByte(255);
        } else class348_sub49.readByte(-106);
    }

    final void method1419(int i, Class348_Sub49 class348_sub49, byte i_2_) {
        anInt2528++;
        for (; ; ) {
            int i_4_ = class348_sub49.readUnsignedByte(255);
            if (i_4_ == 0) break;
            method1418(i_4_, i, class348_sub49, (byte) 94);
        }
    }

    public Class189() {
        this.anInt2526 = 64;
        this.anInt2530 = 64;
        this.anInt2525 = -1;
        this.anInt2534 = 2;
    }
}
