package world.gregs.voidps.tools.icon;/* Class239_Sub4 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class239_Sub4 {
    static float[] aFloatArray5874 = new float[16384];
    static float[] aFloatArray5876 = new float[16384];

    static {
        double d = 3.834951969714103E-4;
        for (int i = 0; i < 16384; i++) {
            aFloatArray5874[i] = (float) Math.sin(d * (double) i);
            aFloatArray5876[i] = (float) Math.cos((double) i * d);
        }
    }
}
