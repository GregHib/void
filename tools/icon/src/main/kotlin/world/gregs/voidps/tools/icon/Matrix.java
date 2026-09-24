package world.gregs.voidps.tools.icon;/* Class101 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class Matrix {
    abstract void translate(int i, int i_0_, int i_1_);

    abstract void makeAxisY(int i);

    abstract void rotateAxisX(int i);

    abstract void makeRotationZ(int i);

    abstract void makeIdentity();

    public Matrix() {
        /* empty */
    }
}
