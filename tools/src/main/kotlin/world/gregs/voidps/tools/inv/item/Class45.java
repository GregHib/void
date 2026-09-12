package world.gregs.voidps.tools.inv.item;/* Class45 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class45 {
    private Class291 aClass291_629 = null;
    static int anInt628;
    static int anInt630;
    int anInt634;
    static int anInt637;
    static int anInt638;
    static int anInt639;
    static int anInt645;
    static int anInt646;
    static int anInt647;
    static int anInt651;
    static int anInt652;
    static int anInt657;
    static int anInt661;
    static int anInt662;
    static int anInt665;
    static int anInt669 = 0;
    private final boolean aBoolean655;
    private Object[] anObjectArray656;
    private Class314 aClass314_659;
    private Object[][] anObjectArrayArray664;

    final int method389(int i) {
        int i_0_ = -117 / ((-60 - i) / 33);
        anInt657++;
        if (!method399(false)) throw new IllegalStateException("");
        return aClass291_629.anInt3719;
    }

    private final boolean method392(int i, byte i_4_) {
        anInt646++;
        if (!method399(false)) return false;
        if (i < 0 || aClass291_629.anIntArray3724.length <= i || (aClass291_629.anIntArray3724[i] == 0)) {
            if (Class285.aBoolean4741) throw new IllegalArgumentException(Integer.toString(i));
            return false;
        }
        return i_4_ == -40;
    }

    final byte[] method393(int i, int i_5_, int i_6_, int[] is) {
        anInt639++;
        if (i_6_ != 2) anInt669 = 51;
        if (!method418(i_5_, i_6_ + -2, i)) return null;
        if (anObjectArrayArray664[i] == null || anObjectArrayArray664[i][i_5_] == null) {
            boolean bool = method394(i_5_, (byte) -78, is, i);
            if (!bool) {
                method406(i, -117);
                bool = method394(i_5_, (byte) -103, is, i);
                if (!bool) return null;
            }
        }
        byte[] is_7_ = Class50_Sub1.method461(false, anObjectArrayArray664[i][i_5_], 53146732);
        if (this.anInt634 == 1) {
            anObjectArrayArray664[i][i_5_] = null;
            if (aClass291_629.anIntArray3724[i] == 1) anObjectArrayArray664[i] = null;
        } else if (this.anInt634 == 2) anObjectArrayArray664[i] = null;
        return is_7_;
    }

    private final boolean method394(int i, byte i_8_, int[] is, int i_9_) {
        anInt628++;
        if (!method392(i_9_, (byte) -40)) return false;
        if (anObjectArray656[i_9_] == null) return false;
        int i_10_ = aClass291_629.anIntArray3725[i_9_];
        int[] is_11_ = aClass291_629.anIntArrayArray3721[i_9_];
        if (anObjectArrayArray664[i_9_] == null) anObjectArrayArray664[i_9_] = new Object[aClass291_629.anIntArray3724[i_9_]];
        Object[] objects = anObjectArrayArray664[i_9_];
        boolean bool = true;
        for (int i_12_ = 0; i_10_ > i_12_; i_12_++) {
            int i_13_;
            if (is_11_ == null) i_13_ = i_12_;
            else i_13_ = is_11_[i_12_];
            if (objects[i_13_] == null) {
                bool = false;
                break;
            }
        }
        if (bool) return true;
        byte[] is_14_;
        if (is == null || (is[0] == 0 && is[1] == 0 && is[2] == 0 && is[3] == 0)) is_14_ = Class50_Sub1.method461(false, anObjectArray656[i_9_], 53146732);
        else {
            is_14_ = Class50_Sub1.method461(true, anObjectArray656[i_9_], 53146732);
            Class348_Sub49 class348_sub49 = new Class348_Sub49(is_14_);
            class348_sub49.method3367(607818341, is, 5, (class348_sub49.aByteArray7154).length);
        }
        byte[] is_15_;
        try {
            is_15_ = Class348_Sub41.method3158(is_14_, -120);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("T3 - " + (is != null) + "," + i_9_ + "," + is_14_.length + "," + Class59_Sub1.method554(5126, is_14_.length, is_14_) + "," + Class59_Sub1.method554(5126, -2 + is_14_.length, is_14_) + "," + aClass291_629.anIntArray3729[i_9_] + "," + aClass291_629.anInt3719));
        }
        if (aBoolean655) anObjectArray656[i_9_] = null;
        if (i_8_ >= -17) method415((byte) 70, -7);
        if (i_10_ > 1) {
            if (this.anInt634 == 2) {
                int i_30_ = is_15_.length;
                int i_31_ = 0xff & is_15_[--i_30_];
                i_30_ -= 4 * (i_31_ * i_10_);
                Class348_Sub49 class348_sub49 = new Class348_Sub49(is_15_);
                int i_32_ = 0;
                int i_33_ = 0;
                class348_sub49.anInt7197 = i_30_;
                for (int i_34_ = 0; i_34_ < i_31_; i_34_++) {
                    int i_35_ = 0;
                    for (int i_36_ = 0; i_36_ < i_10_; i_36_++) {
                        i_35_ += class348_sub49.readInt((byte) -126);
                        int i_37_;
                        if (is_11_ == null) i_37_ = i_36_;
                        else i_37_ = is_11_[i_36_];
                        if (i == i_37_) {
                            i_33_ = i_37_;
                            i_32_ += i_35_;
                        }
                    }
                }
                if (i_32_ == 0) return true;
                byte[] is_38_ = new byte[i_32_];
                class348_sub49.anInt7197 = i_30_;
                i_32_ = 0;
                int i_39_ = 0;
                for (int i_40_ = 0; i_40_ < i_31_; i_40_++) {
                    int i_41_ = 0;
                    for (int i_42_ = 0; i_42_ < i_10_; i_42_++) {
                        i_41_ += class348_sub49.readInt((byte) -126);
                        int i_43_;
                        if (is_11_ != null) i_43_ = is_11_[i_42_];
                        else i_43_ = i_42_;
                        if (i_43_ == i) {
                            Class214.method1577(is_15_, i_39_, is_38_, i_32_, i_41_);
                            i_32_ += i_41_;
                        }
                        i_39_ += i_41_;
                    }
                }
                objects[i_33_] = is_38_;
            } else {
                int i_16_ = is_15_.length;
                int i_17_ = 0xff & is_15_[--i_16_];
                i_16_ -= 4 * (i_10_ * i_17_);
                Class348_Sub49 class348_sub49 = new Class348_Sub49(is_15_);
                int[] is_18_ = new int[i_10_];
                class348_sub49.anInt7197 = i_16_;
                for (int i_19_ = 0; i_19_ < i_17_; i_19_++) {
                    int i_20_ = 0;
                    for (int i_21_ = 0; i_21_ < i_10_; i_21_++) {
                        i_20_ += class348_sub49.readInt((byte) -126);
                        is_18_[i_21_] += i_20_;
                    }
                }
                byte[][] is_22_ = new byte[i_10_][];
                for (int i_23_ = 0; i_10_ > i_23_; i_23_++) {
                    is_22_[i_23_] = new byte[is_18_[i_23_]];
                    is_18_[i_23_] = 0;
                }
                class348_sub49.anInt7197 = i_16_;
                int i_24_ = 0;
                for (int i_25_ = 0; i_25_ < i_17_; i_25_++) {
                    int i_26_ = 0;
                    for (int i_27_ = 0; i_10_ > i_27_; i_27_++) {
                        i_26_ += class348_sub49.readInt((byte) -126);
                        Class214.method1577(is_15_, i_24_, is_22_[i_27_], is_18_[i_27_], i_26_);
                        i_24_ += i_26_;
                        is_18_[i_27_] += i_26_;
                    }
                }
                for (int i_28_ = 0; i_10_ > i_28_; i_28_++) {
                    int i_29_;
                    if (is_11_ == null) i_29_ = i_28_;
                    else i_29_ = is_11_[i_28_];
                    if (this.anInt634 != 0) objects[i_29_] = is_22_[i_28_];
                    else objects[i_29_] = Class179.method1357(is_22_[i_28_], false, (byte) 126);
                }
            }
        } else {
            int i_44_;
            if (is_11_ != null) i_44_ = is_11_[0];
            else i_44_ = 0;
            if (this.anInt634 != 0) objects[i_44_] = is_15_;
            else objects[i_44_] = Class179.method1357(is_15_, false, (byte) 104);
        }
        return true;
    }

    private final boolean method399(boolean bool) {
        anInt652++;
        if (aClass291_629 == null) {
            aClass291_629 = aClass314_659.method2340((byte) 56);
            if (aClass291_629 == null) return false;
            anObjectArray656 = new Object[aClass291_629.anInt3734];
            anObjectArrayArray664 = new Object[aClass291_629.anInt3734][];
        }
        if (bool != false) aClass314_659 = null;
        return true;
    }

    private final void method406(int i, int i_61_) {
        if (i_61_ > -105) anObjectArrayArray664 = null;
        if (!aBoolean655) anObjectArray656[i] = Class179.method1357(aClass314_659.method2339(i, (byte) 73), false, (byte) 123);
        else anObjectArray656[i] = aClass314_659.method2339(i, (byte) 12);
        anInt665++;
    }

    final int method407(int i, int i_62_) {
        if (i != 0) method389(-61);
        anInt645++;
        if (!method392(i_62_, (byte) -40)) return 0;
        return aClass291_629.anIntArray3724[i_62_];
    }

    final byte[] method410(int i, int i_64_, int i_65_) {
        if (i != -1860) anObjectArrayArray664 = null;
        anInt651++;
        return method393(i_64_, i_65_, i ^ ~0x741, null);
    }

    final int method414(int i) {
        anInt637++;
        if (i != -1) return 49;
        if (!method399(false)) return -1;
        return aClass291_629.anIntArray3724.length;
    }

    final byte[] method415(byte i, int i_70_) {
        anInt630++;
        if (!method399(false)) return null;
        if (aClass291_629.anIntArray3724.length == 1) return method410(i ^ ~0x70a, 0, i_70_);
        if (!method392(i_70_, (byte) -40)) return null;
        if (i != 73) anObjectArrayArray664 = null;
        if (aClass291_629.anIntArray3724[i_70_] == 1) return method410(i ^ ~0x70a, i_70_, 0);
        throw new RuntimeException();
    }

    private final boolean method418(int i, int i_73_, int i_74_) {
        anInt662++;
        if (!method399(false)) return false;
        if (i_74_ < i_73_ || i < 0 || (aClass291_629.anIntArray3724.length <= i_74_) || (aClass291_629.anIntArray3724[i_74_] <= i)) {
            if (Class285.aBoolean4741) throw new IllegalArgumentException(i_74_ + "," + i);
            return false;
        }
        return true;
    }

    final boolean method420(int i, int i_76_, int i_77_) {
        anInt638++;
        if (!method418(i_77_, 0, i_76_)) return false;
        if (anObjectArrayArray664[i_76_] != null && anObjectArrayArray664[i_76_][i_77_] != null) return true;
        if (i != -10499) return true;
        if (anObjectArray656[i_76_] != null) return true;
        method406(i_76_, -125);
        return anObjectArray656[i_76_] != null;
    }

    final boolean method421(boolean bool, int i) {
        anInt661++;
        if (!method399(bool)) return false;
        if (aClass291_629.anIntArray3724.length == 1) return method420(-10499, 0, i);
        if (!method392(i, (byte) -40)) return false;
        if (aClass291_629.anIntArray3724[i] == 1) return method420(-10499, i, 0);
        if (bool != false) return false;
        throw new RuntimeException();
    }

    Class45(Class314 class314, boolean bool, int i) {
        if (i < 0 || i > 2) throw new IllegalArgumentException("js5: Invalid value " + i + " supplied for discardunpacked");
        aClass314_659 = class314;
        aBoolean655 = bool;
        this.anInt634 = i;
    }
}
