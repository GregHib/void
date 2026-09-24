package world.gregs.voidps.tools.icon;/* Class348_Sub24 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.io.File;

final class Class348_Sub24 extends Class348 {
    static int anInt6871;
    int anInt6872;
    int anInt6875;
    static String aString6876;
    static String aString6877;
    static int[] anIntArray6878;

    public static void method2993(byte i) {
        anIntArray6878 = null;
        aString6876 = null;
        aString6877 = null;
        int i_37_ = 31 % ((3 - i) / 37);
    }

    static final void method2994(int i) {
        anInt6871++;
    }

    Class348_Sub24(int i, int i_38_) {
        this.anInt6872 = i;
        this.anInt6875 = i_38_;
    }

    static {
        String string = "Unknown";
        try {
            string = System.getProperty("java.vendor").toLowerCase();
        } catch (Exception exception) {
            /* empty */
        }
        string.toLowerCase();
        string = "Unknown";
        try {
            string = System.getProperty("java.version").toLowerCase();
        } catch (Exception exception) {
            /* empty */
        }
        string.toLowerCase();
        string = "Unknown";
        try {
            string = System.getProperty("os.name").toLowerCase();
        } catch (Exception exception) {
            /* empty */
        }
        aString6877 = string.toLowerCase();
        string = "Unknown";
        try {
            string = System.getProperty("os.arch").toLowerCase();
        } catch (Exception exception) {
            /* empty */
        }
        aString6876 = string.toLowerCase();
        string = "Unknown";
        try {
            string = System.getProperty("os.version").toLowerCase();
        } catch (Exception exception) {
            /* empty */
        }
        string.toLowerCase();
        string = "~/";
        try {
            string = System.getProperty("user.home").toLowerCase();
        } catch (Exception exception) {
            /* empty */
        }
        new File(string);
    }
}
