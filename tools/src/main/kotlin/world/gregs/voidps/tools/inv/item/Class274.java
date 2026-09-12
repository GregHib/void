package world.gregs.voidps.tools.inv.item;/* Class274 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class274 {
    static int anInt3477;
    static int anInt3479;
    private final String[] aStringArray3481;
    static int anInt3482;
    /* NOTE: these three static instances are not touched by Class274's own
     * genuine methods, but Class255 (same batch) unconditionally reads them
     * in its genuine constructor/method1940 (e.g. Class274.aClass274_3490.
     * method2063(...)), so they are kept here for that real cross-class need. */
    static Class274 aClass274_3488;
    static Class274 aClass274_3489;
    static Class274 aClass274_3490;
    static Class274 aClass274_3491;
    static Class274 aClass274_3517;
    static Class274 aClass274_3519;

    private Class274(String string, String string_7_, String string_8_, String string_9_) {
        try {
            aStringArray3481 = new String[]{string, string_7_, string_8_, string_9_};
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("va.<init>(" + (string != null ? "{...}" : "null") + ',' + (string_7_ != null ? "{...}" : "null") + ',' + (string_8_ != null ? "{...}" : "null") + ',' + (string_9_ != null ? "{...}" : "null") + ')'));
        }
    }

    public final String toString() {
        anInt3482++;
        throw new IllegalStateException();
    }

    /* NOTE: method2061 is not in the genuine-methods list (never observed
     * covered - all real call sites reachable from this renderer pass
     * i_10_==544, so "if (i_10_ != 544) method2061(126)" is unreachable
     * dead code). Kept here as a minimal stub (counter increment only,
     * original body nulled out static caches on ~10 unrelated classes
     * that are out of scope for icon rendering) purely so the genuine
     * method2063 call site resolves at compile time. */
    static final void method2061(int i) {
        anInt3477++;
    }

    final String method2063(int i, int i_10_) {
        if (i_10_ != 544) method2061(126);
        anInt3479++;
        return aStringArray3481[i];
    }

    static {
        aClass274_3488 = new Class274("Members object", "Gegenstand für Mitglieder", "Objet d'abonnés", "Objeto para membros");
        aClass274_3489 = new Class274("Discard", "Ablegen", "Jeter", "Descartar");
        aClass274_3490 = new Class274("Take", "Nehmen", "Prendre", "Pegar");
        aClass274_3491 = new Class274("Drop", "Fallen lassen", "Poser", "Largar");
        aClass274_3517 = new Class274("M", "M", "M", "M");
        aClass274_3519 = new Class274("K", "T", "K", "K");
    }
}
