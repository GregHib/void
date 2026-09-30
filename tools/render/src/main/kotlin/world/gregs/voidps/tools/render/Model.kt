package world.gregs.voidps.tools.render

/* Class64 */

abstract class Model {
    abstract fun render(matrix: Matrix?, i: Int)

    abstract fun fa(): Int

    abstract fun loadedTextures(): Boolean

    abstract fun O(i: Int, i_765_: Int, i_766_: Int)

    /**
     * Class64.method617/method602 without tweening: poses the model with a single [frame].
     * Lighting is calculated for the bind pose first, as the client does when copying a model to animate.
     */
    fun animate(frame: AnimationFrame) {
        if (!prepareAnimation()) return
        val base = frame.base
        for (i in 0 until frame.count) {
            val group = frame.groups[i].toInt()
            val origin = frame.origins[i].toInt()
            if (origin != -1) transform(0, base.labels[origin], 0, 0, 0)
            transform(base.types[group], base.labels[group], frame.x[i].toInt(), frame.y[i].toInt(), frame.z[i].toInt())
        }
        finishAnimation()
    }

    /** Class64_Sub1.NA - returns false if the model has no vertex labels to animate. */
    protected abstract fun prepareAnimation(): Boolean

    /** Class64_Sub1.method605 - applies one transform [type] to the vertex/face groups in [labels]. */
    protected abstract fun transform(type: Int, labels: IntArray, x: Int, y: Int, z: Int)

    /** Class64_Sub1.wa */
    protected abstract fun finishAnimation()
}
