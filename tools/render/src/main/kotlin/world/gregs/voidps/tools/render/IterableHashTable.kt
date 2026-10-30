package world.gregs.voidps.tools.render

/* Class356 */

class IterableHashTable(var anInt4377: Int) {
    var aNodeArray4374: Array<Node?>
    private var aNode_4389: Node? = null

    fun method3480(l: Long): Node? {
        val class348 = (this.aNodeArray4374[(l and (this.anInt4377 + -1).toLong()).toInt()])
        aNode_4389 = class348!!.aNode_4294
        while (aNode_4389 !== class348) {
            if (l == aNode_4389!!.aLong4291) {
                val class348_7_ = aNode_4389
                aNode_4389 = aNode_4389!!.aNode_4294
                return class348_7_
            }
            aNode_4389 = aNode_4389!!.aNode_4294
        }
        aNode_4389 = null
        return null
    }

    fun put(l: Long, node: Node?) {
        if (node!!.aNode_4295 != null) node.unlink()
        val class348_10_ = (this.aNodeArray4374[(l and (-1 + this.anInt4377).toLong()).toInt()])
        node.aNode_4294 = class348_10_
        node.aNode_4295 = class348_10_!!.aNode_4295
        node.aNode_4295!!.aNode_4294 = node
        node.aNode_4294!!.aNode_4295 = node
        node.aLong4291 = l
    }

    init {
        this.aNodeArray4374 = arrayOfNulls<Node>(anInt4377)
        var i_11_ = 0
        while (anInt4377 > i_11_) {
            this.aNodeArray4374[i_11_] = Node()
            val class348 = this.aNodeArray4374[i_11_]
            class348!!.aNode_4294 = class348
            class348.aNode_4295 = class348
            i_11_++
        }
    }
}
