package world.gregs.voidps.tools.inv.item;/* Class348_Sub42 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

class Class348_Sub42 extends Class348 {
    Class348_Sub42 aClass348_Sub42_7060;
    static int anInt7062;
    Class348_Sub42 aClass348_Sub42_7063;
    static int anInt7064;
    long aLong7057;

    final void method3162(boolean bool) {
        anInt7064++;
        if (bool != true) method3163((byte) 50);
        if (this.aClass348_Sub42_7060 != null) {
            this.aClass348_Sub42_7060.aClass348_Sub42_7063 = this.aClass348_Sub42_7063;
            this.aClass348_Sub42_7063.aClass348_Sub42_7060 = this.aClass348_Sub42_7060;
            this.aClass348_Sub42_7060 = null;
            this.aClass348_Sub42_7063 = null;
        }
    }

    // Trimmed for item_renderer_standalone: the original body reset a
    // handful of unrelated static packet/cache buffers not present in this
    // standalone item-icon renderer. Not in the genuine-members list for
    // this class -- every reachable call site passes method3162(true),
    // which never triggers the "if (bool != true) method3163(...)" guard
    // below, so the body is elided.
    static final void method3163(byte i) {
        anInt7062++;
    }

    public Class348_Sub42() {
        /* empty */
    }
}
