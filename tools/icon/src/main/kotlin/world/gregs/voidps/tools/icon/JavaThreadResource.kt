package world.gregs.voidps.tools.icon

/* Class167 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class JavaThreadResource(var_ha_Sub1: JavaToolkit) {
    private val aHa_Sub1_2191: JavaToolkit
    var anInt2192: Int = 0
    var aBoolean2195: Boolean = false
    var aRunnable2198: Runnable? = null
    var anInt2197: Int = 0
    var aBoolean2201: Boolean = false
    var aBoolean2202: Boolean = true
    var anInt2210: Int
    var anInt2211: Int = 0
    var anIntArray2212: IntArray
    var anIntArray2213: IntArray
    var anIntArray2214: IntArray
    var anInt2215: Int = 0
    var anIntArray2216: IntArray
    var anIntArray2217: IntArray
    var anIntArray2218: IntArray
    var aClass101_Sub1_2209: Matrix_Sub1?
    var aClass64_Sub1_2219: JavaModel?
    var rasterizer: Rasterizer?
    var anInt2221: Int = 0
    var anIntArray2222: IntArray
    var aClass64_Sub1_2223: JavaModel?
    var aClass64_Sub1_2224: JavaModel?
    var aClass64_Sub1_2225: JavaModel?
    var aFloatArray2226: FloatArray
    var aClass64_Sub1_2227: JavaModel?
    var anIntArray2228: IntArray
    var anInt2229: Int = 0
    var anIntArray2230: IntArray
    var aClass64_Sub1_2231: JavaModel?
    var anIntArray2232: IntArray
    var aClass64_Sub1_2233: JavaModel?
    var anIntArray2234: IntArray
    var anIntArray2235: IntArray
    var anIntArray2236: IntArray
    var anIntArray2237: IntArray
    var anIntArray2238: IntArray
    var aClass64_Sub1_2239: JavaModel?
    var anIntArray2240: IntArray
    var anIntArray2241: IntArray
    var anIntArray2242: IntArray
    var aClass64_Sub1_2243: JavaModel?
    var anIntArray2244: IntArray
    var anIntArray2245: IntArray
    var aClass64_Sub1_2246: JavaModel?
    var anIntArray2247: IntArray

    fun method1291(runnable: Runnable?) {
        this.aRunnable2198 = runnable
    }

    fun method1292() {
        this.rasterizer = Rasterizer(aHa_Sub1_2191, this)
    }

    init {
        this.aClass101_Sub1_2209 = Matrix_Sub1()
        this.anIntArray2213 = IntArray(JavaModel.Companion.anInt5350)
        this.anIntArray2214 = IntArray(JavaModel.Companion.anInt5350)
        this.anIntArray2212 = IntArray(64)
        this.aFloatArray2226 = FloatArray(2)
        this.anIntArray2216 = IntArray(10000)
        this.anIntArray2222 = IntArray(JavaModel.Companion.anInt5350)
        this.anIntArray2232 = IntArray(64)
        this.anIntArray2218 = IntArray(8)
        this.anIntArray2237 = IntArray(JavaModel.Companion.anInt5350)
        this.anIntArray2236 = IntArray(10000)
        this.anIntArray2230 = IntArray(JavaModel.Companion.anInt5350)
        this.anIntArray2240 = IntArray(10)
        this.anIntArray2228 = IntArray(64)
        this.anIntArray2238 = IntArray(10)
        this.anIntArray2241 = IntArray(8)
        this.anIntArray2235 = IntArray(10)
        this.anIntArray2245 = IntArray(8)
        this.anIntArray2217 = IntArray(64)
        this.anIntArray2244 = IntArray(JavaModel.Companion.anInt5350)
        this.anIntArray2247 = IntArray(10)
        this.anIntArray2234 = IntArray(JavaModel.Companion.anInt5350)
        aHa_Sub1_2191 = var_ha_Sub1
        this.anInt2210 = aHa_Sub1_2191.anInt7494 + -255
        this.rasterizer = Rasterizer(var_ha_Sub1, this)
        this.aClass64_Sub1_2243 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2224 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2219 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2239 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2233 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2231 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2223 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2227 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2246 = JavaModel(aHa_Sub1_2191)
        this.aClass64_Sub1_2225 = JavaModel(aHa_Sub1_2191)
        this.anIntArray2242 = IntArray(JavaModel.Companion.anInt5346)
        var i = 0
        while (JavaModel.Companion.anInt5346 > i) {
            this.anIntArray2242[i] = -1
            i++
        }
    }
}
