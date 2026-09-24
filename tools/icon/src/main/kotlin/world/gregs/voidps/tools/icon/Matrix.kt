package world.gregs.voidps.tools.icon

/* Class101 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal abstract class Matrix {
    abstract fun translate(i: Int, i_0_: Int, i_1_: Int)

    abstract fun makeAxisY(i: Int)

    abstract fun rotateAxisX(i: Int)

    abstract fun makeRotationZ(i: Int)

    abstract fun makeIdentity()
}
