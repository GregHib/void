package world.gregs.voidps.tools.icon

import java.io.File
import java.util.*

/* Class348_Sub24 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub24(var anInt6872: Int, var anInt6875: Int) : Class348() {
    companion object {
        var anInt6871: Int = 0
        var aString6876: String?
        var aString6877: String?
        var anIntArray6878: IntArray?

        fun method2993(i: Byte) {
            anIntArray6878 = null
            aString6876 = null
            aString6877 = null
            val i_37_ = 31 % ((3 - i) / 37)
        }

        fun method2994(i: Int) {
            anInt6871++
        }

        init {
            var string = "Unknown"
            try {
                string = System.getProperty("java.vendor").lowercase(Locale.getDefault())
            } catch (exception: Exception) {
                /* empty */
            }
            string.lowercase(Locale.getDefault())
            string = "Unknown"
            try {
                string = System.getProperty("java.version").lowercase(Locale.getDefault())
            } catch (exception: Exception) {
                /* empty */
            }
            string.lowercase(Locale.getDefault())
            string = "Unknown"
            try {
                string = System.getProperty("os.name").lowercase(Locale.getDefault())
            } catch (exception: Exception) {
                /* empty */
            }
            aString6877 = string.lowercase(Locale.getDefault())
            string = "Unknown"
            try {
                string = System.getProperty("os.arch").lowercase(Locale.getDefault())
            } catch (exception: Exception) {
                /* empty */
            }
            aString6876 = string.lowercase(Locale.getDefault())
            string = "Unknown"
            try {
                string = System.getProperty("os.version").lowercase(Locale.getDefault())
            } catch (exception: Exception) {
                /* empty */
            }
            string.lowercase(Locale.getDefault())
            string = "~/"
            try {
                string = System.getProperty("user.home").lowercase(Locale.getDefault())
            } catch (exception: Exception) {
                /* empty */
            }
            File(string)
        }
    }
}
