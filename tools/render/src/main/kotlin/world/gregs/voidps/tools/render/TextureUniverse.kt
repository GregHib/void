package world.gregs.voidps.tools.render

/* Class358 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class TextureUniverse(`is`: IntArray?, is_1_: IntArray?, is_2_: IntArray?, fs: Array<FloatArray?>?) {
    var matrices: Array<FloatArray?>?
    var originZ: IntArray?
    var originY: IntArray?
    var originX: IntArray?

    init {
        this.originZ = is_2_
        this.matrices = fs
        this.originY = is_1_
        this.originX = `is`
    }
}
