package world.gregs.voidps.tools.icon;/* Class348_Sub17 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub17 {
    static int anInt6789;

    static final RuntimeException_Sub1 method2929(Throwable throwable, String string) {
        anInt6789++;
        throwable.printStackTrace();
        RuntimeException_Sub1 runtimeexception_sub1;
        if (throwable instanceof RuntimeException_Sub1) {
            runtimeexception_sub1 = (RuntimeException_Sub1) throwable;
            runtimeexception_sub1.aString4594 += ' ' + string;
        } else runtimeexception_sub1 = new RuntimeException_Sub1(throwable, string);
        return runtimeexception_sub1;
    }

    public Class348_Sub17() {
        /* empty */
    }
}
