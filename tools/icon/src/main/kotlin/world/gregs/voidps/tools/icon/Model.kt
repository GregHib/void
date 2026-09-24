package world.gregs.voidps.tools.icon

/* Class64 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal abstract class Model {
    var aBoolean1124: Boolean = false

    // abstract dependencies of Class213.method1562 (genuine), which calls
    // these on a Class64-typed reference - all implemented concretely in
    // Class64_Sub1.
    abstract fun render(matrix: Matrix?, class318_sub3: Class318_Sub3?, i: Int)

    abstract fun fa(): Int

    abstract fun loadedTextures(): Boolean

    abstract fun O(i: Int, i_765_: Int, i_766_: Int)
}
