package world.gregs.voidps.tools.render

/** Class262 **/
internal class LinkedList {
    var aNode_3334: Node = Node()

    fun method1993(): Node? {
        val class348 = this.aNode_3334.aNode_4295
        if (this.aNode_3334 === class348) {
            return null
        }
        return class348
    }

    fun method1996() {
        while (true) {
            val class348 = (this.aNode_3334.aNode_4294)
            if (this.aNode_3334 === class348) break
            class348!!.unlink()
        }
    }

    fun method2001(node: Node) {
        if (node.aNode_4295 != null) node.unlink()
        node.aNode_4295 = this.aNode_3334
        node.aNode_4294 = this.aNode_3334.aNode_4294
        node.aNode_4295!!.aNode_4294 = node
        node.aNode_4294!!.aNode_4295 = node
    }

    init {
        this.aNode_3334.aNode_4295 = this.aNode_3334
        this.aNode_3334.aNode_4294 = this.aNode_3334
    }

    companion object {
        fun cubeMap(f: Float, f_3_: Float, fs: FloatArray?, i: Int, i_4_: Int, i_5_: Int, i_6_: Int, i_7_: Int, i_8_: Int, f_9_: Float, fs_10_: FloatArray?, i_11_: Int, i_12_: Int) {
            var i = i
            var i_7_ = i_7_
            var i_8_ = i_8_
            i_7_ -= i_6_
            i -= i_4_
            i_8_ -= i_11_
            val f_13_ = fs!![2] * i.toFloat() + (fs[1] * i_8_.toFloat() + fs[0] * i_7_.toFloat())
            val f_14_ = (i_7_.toFloat() * fs[3] + i_8_.toFloat() * fs[4] + i.toFloat() * fs[5])
            val f_15_ = fs[8] * i.toFloat() + (fs[6] * i_7_.toFloat() + i_8_.toFloat() * fs[7])
            var f_16_: Float
            var f_17_: Float
            if (i_12_ == 0) {
                f_16_ = 0.5f + (f_3_ + f_13_)
                f_17_ = -f_15_ + f + 0.5f
            } else if (i_12_ == 1) {
                f_17_ = 0.5f + (f_15_ + f)
                f_16_ = 0.5f + (f_3_ + f_13_)
            } else if (i_12_ == 2) {
                f_16_ = 0.5f + (-f_13_ + f_3_)
                f_17_ = -f_14_ + f_9_ + 0.5f
            } else if (i_12_ == 3) {
                f_17_ = -f_14_ + f_9_ + 0.5f
                f_16_ = f_13_ + f_3_ + 0.5f
            } else if (i_12_ == 4) {
                f_16_ = f_15_ + f + 0.5f
                f_17_ = -f_14_ + f_9_ + 0.5f
            } else {
                f_16_ = 0.5f + (f + -f_15_)
                f_17_ = -f_14_ + f_9_ + 0.5f
            }
            if (i_5_ == 1) {
                val f_18_ = f_16_
                f_16_ = -f_17_
                f_17_ = f_18_
            } else if (i_5_ == 2) {
                f_17_ = -f_17_
                f_16_ = -f_16_
            } else if (i_5_ == 3) {
                val f_19_ = f_16_
                f_16_ = f_17_
                f_17_ = -f_19_
            }
            fs_10_!![1] = f_17_
            fs_10_[0] = f_16_
        }
    }
}
