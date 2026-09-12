package world.gregs.voidps.tools.inv.item;/* Class274 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class LocalisedText {
    static int anInt3477;
    static int anInt3479;
    private final String[] aStringArray3481;
    static int anInt3482;
    /* NOTE: these three static instances are not touched by Class274's own
     * genuine methods, but Class255 (same batch) unconditionally reads them
     * in its genuine constructor/method1940 (e.g. Class274.aClass274_3490.
     * method2063(...)), so they are kept here for that real cross-class need. */
    static LocalisedText MEMBERS_OBJECT;
    static LocalisedText Discard;
    static LocalisedText TAKE;
    static LocalisedText DROP;
    static LocalisedText M;
    static LocalisedText K;

    private LocalisedText(String string, String string_7_, String string_8_, String string_9_) {
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
        MEMBERS_OBJECT = new LocalisedText("Members object", "Gegenstand für Mitglieder", "Objet d'abonnés", "Objeto para membros");
        Discard = new LocalisedText("Discard", "Ablegen", "Jeter", "Descartar");
        TAKE = new LocalisedText("Take", "Nehmen", "Prendre", "Pegar");
        DROP = new LocalisedText("Drop", "Fallen lassen", "Poser", "Largar");
        M = new LocalisedText("M", "M", "M", "M");
        K = new LocalisedText("K", "T", "K", "K");
    }
}
