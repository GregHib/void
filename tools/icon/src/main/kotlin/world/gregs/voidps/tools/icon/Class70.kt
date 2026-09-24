package world.gregs.voidps.tools.icon

import kotlin.math.cos
import kotlin.math.sin

/* Class70 - minimal stub (missing from trimmed tree)
* See client/src/Class70.java for the full original.
*/

internal object Class70 {
    var anIntArray1204: IntArray
    var anIntArray1207: IntArray = IntArray(16384)

    init {
        anIntArray1204 = IntArray(16384)
        val d = 3.834951969714103E-4
        for (i in 0..16383) {
            anIntArray1207[i] = (16384.0 * sin(d * i.toDouble())).toInt()
            anIntArray1204[i] = (cos(d * i.toDouble()) * 16384.0).toInt()
        }
    }
}
