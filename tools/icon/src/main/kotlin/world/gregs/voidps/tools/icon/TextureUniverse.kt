package world.gregs.voidps.tools.icon

/* Class358 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class TextureUniverse(`is`: IntArray?, is_1_: IntArray?, is_2_: IntArray?, fs: Array<FloatArray?>?) {
    var matrices: Array<FloatArray?>?
    var originZ: IntArray?
    var originY: IntArray?
    var originX: IntArray?

    init {
        try {
            this.originZ = is_2_
            this.matrices = fs
            this.originY = is_1_
            this.originX = `is`
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("ew.<init>(" + (if (`is` != null) "{...}" else "null") + ',' + (if (is_1_ != null) "{...}" else "null") + ',' + (if (is_2_ != null) "{...}" else "null") + ',' + (if (fs != null) "{...}" else "null") + ')'))
        }
    }
}
