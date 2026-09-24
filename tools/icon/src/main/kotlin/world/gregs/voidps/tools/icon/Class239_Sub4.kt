package world.gregs.voidps.tools.icon

import kotlin.math.cos
import kotlin.math.sin

/* Class239_Sub4 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class239_Sub4 {
    var aFloatArray5874: FloatArray = FloatArray(16384)
    var aFloatArray5876: FloatArray = FloatArray(16384)

    init {
        val d = 3.834951969714103E-4
        for (i in 0..16383) {
            aFloatArray5874[i] = sin(d * i.toDouble()).toFloat()
            aFloatArray5876[i] = cos(i.toDouble() * d).toFloat()
        }
    }
}
