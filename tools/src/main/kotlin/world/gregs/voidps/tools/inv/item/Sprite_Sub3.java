package world.gregs.voidps.tools.inv.item;/* Class105_Sub3 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class Sprite_Sub3 extends Sprite {
    int anInt8471;
    int anInt8470;
    int anInt8461;
    int anInt8464;
    JavaToolkit aHa_Sub1_8460;

    // Only reachable via ha_Sub1.method3712, which is itself unreachable per
    // JaCoCo coverage (0 hits) in the real item-icon dumper run.
    abstract void method996(int i, int i_40_, int i_41_, int i_42_, int i_43_, int i_44_, int i_45_, int i_46_, int i_47_);

    final int method971() {
        return this.anInt8471;
    }

    final int method969() {
        return this.anInt8470;
    }

    Sprite_Sub3(JavaToolkit var_ha_Sub1, int i, int i_55_) {
        this.aHa_Sub1_8460 = var_ha_Sub1;
        this.anInt8471 = i;
        this.anInt8470 = i_55_;
    }
}
