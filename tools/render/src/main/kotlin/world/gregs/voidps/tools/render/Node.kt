package world.gregs.voidps.tools.render

import world.gregs.voidps.cache.Cache

/** Class348 **/
open class Node {
    var aNode_4294: Node? = null
    var aNode_4295: Node? = null
    var aLong4291: Long = 0

    fun unlink() {
        if (this.aNode_4295 != null) {
            this.aNode_4295!!.aNode_4294 = this.aNode_4294
            this.aNode_4294!!.aNode_4295 = this.aNode_4295
            this.aNode_4294 = null
            this.aNode_4295 = null
        }
    }

    companion object {
        var aCache_4286: Cache? = null
    }
}
