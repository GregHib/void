package world.gregs.voidps.tools.icon;/* Class348_Sub41 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub41 extends Class348 {
    int anInt7050;
    static int anInt7046;
    static Class114 aClass114_7052 = new Class114(86, 6);
    static int anInt7055;

    /* NOTE: method3156 is NOT in the genuine-methods list (0 JaCoCo hits) -
     * its only caller in method3158 is guarded by "if (i > -74)" which is
     * never true for the (i == -105 / i == -120) call sites reachable from
     * this renderer. Stubbed rather than pulling in Class239_Sub26 for a
     * call that never actually executes. */
    static final int method3156(boolean bool, String string) {
        anInt7055++;
        if (bool != true) aClass114_7052 = null;
        throw new IllegalStateException(); // unreachable per JaCoCo coverage
    }

    static final byte[] decodeContainer(byte[] is, int i) {
        anInt7046++;
        Packet packet = new Packet(is);
        int i_37_ = packet.readUnsignedByte(255);
        if (i > -74) method3156(true, null);
        int i_38_ = packet.readInt((byte) -126);
        if (i_38_ < 0 || (Class29.anInt401 != 0 && i_38_ > Class29.anInt401)) {
            throw new RuntimeException();
        }
        if (i_37_ != 0) {
            /* Corrected: this branch IS reachable in practice (the
             * materials/idx26 archive is compressed) - confirmed by an
             * actual run against the real cache, which hit this path and
             * threw the placeholder IllegalStateException a prior pass left
             * here on the (incorrect) assumption this was dead per JaCoCo.
             * Restored the real gzip/bzip2 dispatch verbatim. */
            int i_39_ = packet.readInt((byte) -126);
            if (i_39_ < 0 || (Class29.anInt401 != 0 && i_39_ > Class29.anInt401) || i_39_ > 10000000) {
                return new byte[4];
            }
            byte[] is_40_ = new byte[i_39_];
            if (i_37_ == 1) Class212.method1547(is_40_, i_39_, is, i_38_, 9);
            else {
                synchronized (Class348_Sub33.aClass152_6955) {
                    Class348_Sub33.aClass152_6955.method1218(is_40_, 29123, packet);
                }
            }
            return is_40_;
        }
        byte[] is_41_ = new byte[i_38_];
        packet.gdata(2147483647, 0, i_38_, is_41_);
        return is_41_;
    }

    public Class348_Sub41() {
        /* empty */
    }

}
