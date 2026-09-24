package world.gregs.voidps.tools.icon;/* Class167 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class JavaThreadResource {
    private final JavaToolkit aHa_Sub1_2191;
    int anInt2192;
    static int anInt2193;
    static int anInt2194;
    boolean aBoolean2195 = false;
    Runnable aRunnable2198;
    int anInt2197;
    int anInt2205;
    boolean aBoolean2201;
    boolean aBoolean2202;
    int anInt2210;
    int anInt2211;
    int[] anIntArray2212;
    int[] anIntArray2213;
    int[] anIntArray2214;
    int anInt2215;
    int[] anIntArray2216;
    int[] anIntArray2217;
    int[] anIntArray2218;
    Matrix_Sub1 aClass101_Sub1_2209;
    JavaModel aClass64_Sub1_2219;
    Rasterizer rasterizer;
    int anInt2221;
    int[] anIntArray2222;
    JavaModel aClass64_Sub1_2223;
    JavaModel aClass64_Sub1_2224;
    JavaModel aClass64_Sub1_2225;
    float[] aFloatArray2226;
    JavaModel aClass64_Sub1_2227;
    int[] anIntArray2228;
    int anInt2229;
    int[] anIntArray2230;
    JavaModel aClass64_Sub1_2231;
    int[] anIntArray2232;
    JavaModel aClass64_Sub1_2233;
    int[] anIntArray2234;
    int[] anIntArray2235;
    int[] anIntArray2236;
    int[] anIntArray2237;
    int[] anIntArray2238;
    JavaModel aClass64_Sub1_2239;
    int[] anIntArray2240;
    int[] anIntArray2241;
    int[] anIntArray2242;
    JavaModel aClass64_Sub1_2243;
    int[] anIntArray2244;
    int[] anIntArray2245;
    JavaModel aClass64_Sub1_2246;
    int[] anIntArray2247;

    final void method1291(int i, Runnable runnable) {
        if (i == 10000) {
            this.aRunnable2198 = runnable;
            anInt2193++;
        }
    }

    final void method1292(int i) {
        if (i == 64) {
            anInt2194++;
            this.rasterizer = new Rasterizer(aHa_Sub1_2191, this);
        }
    }

    JavaThreadResource(JavaToolkit var_ha_Sub1) {
        this.anInt2192 = 0;
        this.anInt2197 = 0;
        this.anInt2205 = 0;
        this.aBoolean2202 = true;
        this.anInt2211 = 0;
        this.aClass101_Sub1_2209 = new Matrix_Sub1();
        this.anIntArray2213 = new int[JavaModel.anInt5350];
        this.anIntArray2214 = new int[JavaModel.anInt5350];
        this.anIntArray2212 = new int[64];
        this.aFloatArray2226 = new float[2];
        this.anIntArray2216 = new int[10000];
        this.anIntArray2222 = new int[JavaModel.anInt5350];
        this.anIntArray2232 = new int[64];
        this.anIntArray2218 = new int[8];
        this.anIntArray2237 = new int[JavaModel.anInt5350];
        this.anIntArray2236 = new int[10000];
        this.anIntArray2230 = new int[JavaModel.anInt5350];
        this.anIntArray2240 = new int[10];
        this.anIntArray2228 = new int[64];
        this.anIntArray2238 = new int[10];
        this.anIntArray2241 = new int[8];
        this.anIntArray2235 = new int[10];
        this.anIntArray2245 = new int[8];
        this.anIntArray2217 = new int[64];
        this.anIntArray2244 = new int[JavaModel.anInt5350];
        this.anIntArray2247 = new int[10];
        this.anIntArray2234 = new int[JavaModel.anInt5350];
        aHa_Sub1_2191 = var_ha_Sub1;
        this.anInt2210 = aHa_Sub1_2191.anInt7494 + -255;
        this.rasterizer = new Rasterizer(var_ha_Sub1, this);
        this.aClass64_Sub1_2243 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2224 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2219 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2239 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2233 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2231 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2223 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2227 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2246 = new JavaModel(aHa_Sub1_2191);
        this.aClass64_Sub1_2225 = new JavaModel(aHa_Sub1_2191);
        this.anIntArray2242 = new int[JavaModel.anInt5346];
        for (int i = 0; JavaModel.anInt5346 > i; i++)
            this.anIntArray2242[i] = -1;
    }
}
