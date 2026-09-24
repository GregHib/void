package world.gregs.voidps.tools.icon;/* Class107 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Queue {
    static int anInt1653;
    static int anInt1654;
    Class348_Sub42 aClass348_Sub42_1647 = new Class348_Sub42();

    final Class348_Sub42 method1008(int i) {
        if (i != 20) aClass348_Sub42_1652 = null;
        anInt1653++;
        Class348_Sub42 class348_sub42 = (this.aClass348_Sub42_1647.aClass348_Sub42_7063);
        if (class348_sub42 == this.aClass348_Sub42_1647) return null;
        class348_sub42.unlink2(true);
        return class348_sub42;
    }

    private Class348_Sub42 aClass348_Sub42_1652;

    final void add(boolean bool, Class348_Sub42 class348_sub42) {
        if (class348_sub42.aClass348_Sub42_7060 != null) class348_sub42.unlink2(bool);
        anInt1654++;
        class348_sub42.aClass348_Sub42_7063 = this.aClass348_Sub42_1647;
        class348_sub42.aClass348_Sub42_7060 = (this.aClass348_Sub42_1647.aClass348_Sub42_7060);
        if (bool == true) {
            class348_sub42.aClass348_Sub42_7060.aClass348_Sub42_7063 = class348_sub42;
            class348_sub42.aClass348_Sub42_7063.aClass348_Sub42_7060 = class348_sub42;
        }
    }

    public Queue() {
        this.aClass348_Sub42_1647.aClass348_Sub42_7060 = this.aClass348_Sub42_1647;
        this.aClass348_Sub42_1647.aClass348_Sub42_7063 = this.aClass348_Sub42_1647;
    }
}
