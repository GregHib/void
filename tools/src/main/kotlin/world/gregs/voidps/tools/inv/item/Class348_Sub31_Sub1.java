package world.gregs.voidps.tools.inv.item;/* Class348_Sub31_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;

final class Class348_Sub31_Sub1 extends Class348_Sub31 {
    private Canvas aCanvas9065;
    private Image anImage9066;
    private Rectangle aRectangle9067;

    final void method3008(Canvas canvas, int i, int i_6_, int i_7_) {
        aCanvas9065 = canvas;
        aRectangle9067 = new Rectangle();
        this.anInt6917 = i;
        this.anInt6920 = i_7_;
        this.anIntArray6916 = new int[(this.anInt6920 * this.anInt6917)];
        DataBufferInt databufferint = new DataBufferInt(this.anIntArray6916, (this.anIntArray6916).length);
        if (i_6_ > -42) method3008(null, 6, -14, 63);
        DirectColorModel directcolormodel = new DirectColorModel(32, 16711680, 65280, 255);
        WritableRaster writableraster = Raster.createWritableRaster((directcolormodel.createCompatibleSampleModel((this.anInt6917), (this.anInt6920))), databufferint, null);
        anImage9066 = new BufferedImage(directcolormodel, writableraster, false, new Hashtable());
    }
}
