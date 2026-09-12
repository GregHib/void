package world.gregs.voidps.tools.inv.item;/* Class105_Sub3_Sub3 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 *
 * Trimmed for item_renderer_standalone: constructed by ha_Sub1.method3629 and
 * ha_Sub1.method3711 when the "has alpha" branch is taken, but per JaCoCo
 * coverage that branch is never actually hit while dumping item icons (0
 * coverage on this whole class). Only the two constructors those call sites
 * use are kept verbatim; the abstract method964 override is a stub since it
 * is unreachable in practice.
 */

final class Class105_Sub3_Sub3 extends Class105_Sub3 {
    int[] anIntArray9936;

    Class105_Sub3_Sub3(ha_Sub1 var_ha_Sub1, int i, int i_443_) {
        super(var_ha_Sub1, i, i_443_);
        this.anIntArray9936 = new int[i * i_443_];
    }

    Class105_Sub3_Sub3(ha_Sub1 var_ha_Sub1, int[] is, int i, int i_1_, int i_2_, int i_3_, boolean bool) {
        super(var_ha_Sub1, i_2_, i_3_);
        if (bool) this.anIntArray9936 = new int[i_2_ * i_3_];
        else this.anIntArray9936 = is;
        i_1_ -= this.anInt8471;
        int i_4_ = 0;
        for (int i_5_ = 0; i_5_ < i_3_; i_5_++) {
            for (int i_6_ = 0; i_6_ < i_2_; i_6_++)
                this.anIntArray9936[i_4_++] = is[i++];
            i += i_1_;
        }
    }

    // Unreachable per JaCoCo coverage (0 hits for this whole class).
    final void method964(int i, int i_148_, int i_149_, int i_150_, int i_151_) {
        throw new IllegalStateException();
    }

    // Unreachable per JaCoCo coverage (0 hits) - see Class105_Sub3.method996.
    final void method996(int i, int i_444_, int i_445_, int i_446_, int i_447_, int i_448_, int i_449_, int i_450_, int i_451_) {
        throw new IllegalStateException();
    }
}
