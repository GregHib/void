package world.gregs.voidps.tools.icon;/* Class358 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class TextureUniverse {
    float[][] matrices;
    int[] originZ;
    int[] originY;
    int[] originX;

    TextureUniverse(int[] is, int[] is_1_, int[] is_2_, float[][] fs) {
        try {
            this.originZ = is_2_;
            this.matrices = fs;
            this.originY = is_1_;
            this.originX = is;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ew.<init>(" + (is != null ? "{...}" : "null") + ',' + (is_1_ != null ? "{...}" : "null") + ',' + (is_2_ != null ? "{...}" : "null") + ',' + (fs != null ? "{...}" : "null") + ')'));
        }
    }
}
