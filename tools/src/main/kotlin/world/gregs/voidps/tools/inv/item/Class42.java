package world.gregs.voidps.tools.inv.item;/* Class42 - minimal stub (missing from trimmed tree)
 * See client/src/Class42.java for the full original. Only the fields and
 * method373 needed by Class60.method589 are kept.
 */

final class Class42 {
    static int anInt594;
    boolean aBoolean574;
    private int anInt573 = -1;
    int anInt581;
    private int anInt583;
    private int anInt585;
    private int anInt586;
    private int anInt587;
    private int anInt590;
    private int anInt592;
    int anInt596;
    private int anInt606;

    final boolean method373(Interface17 interface17, int i) {
        anInt594++;
        int i_1_;
        if (anInt606 == -1) {
            if (anInt590 == -1) return true;
            i_1_ = interface17.method62(anInt590, -65536);
        } else i_1_ = interface17.method61(anInt606, (byte) -16);
        if (i_1_ < anInt585 || i_1_ > anInt592) return false;
        if (i < 26) anInt586 = 11;
        int i_2_;
        if (anInt583 == -1) {
            if (anInt573 != -1) i_2_ = interface17.method62(anInt573, -65536);
            else return true;
        } else i_2_ = interface17.method61(anInt583, (byte) -16);
        return i_2_ >= anInt587 && i_2_ <= anInt586;
    }
}
