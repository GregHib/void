package world.gregs.voidps.tools.inv.item;/* Class45 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Js5 {
    private Js5Index index = null;
    static int anInt628;
    static int anInt630;
    int discardUnpacked;
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
    private final boolean discardPacked;
    private Object[] packed;
    private ResourceProvider provider;
    private Object[][] unpacked;

    final int getCrc(int i) {
        int i_0_ = -117 / ((-60 - i) / 33);
        anInt657++;
        if (!indexReady(false)) throw new IllegalStateException("");
        return index.crc;
    }

    private final boolean isValidGroup(int i, byte i_4_) {
        anInt646++;
        if (!indexReady(false)) return false;
        if (i < 0 || index.fileLimits.length <= i || (index.fileLimits[i] == 0)) {
            if (Class285.aBoolean4741) throw new IllegalArgumentException(Integer.toString(i));
            return false;
        }
        return i_4_ == -40;
    }

    final byte[] getFile(int i, int i_5_, int i_6_, int[] is) {
        anInt639++;
        if (i_6_ != 2) anInt669 = 51;
        if (!isValidFile(i_5_, i_6_ + -2, i)) return null;
        if (unpacked[i] == null || unpacked[i][i_5_] == null) {
            boolean bool = unpackFile(i_5_, (byte) -78, is, i);
            if (!bool) {
                fetchGroup(i, -117);
                bool = unpackFile(i_5_, (byte) -103, is, i);
                if (!bool) return null;
            }
        }
        byte[] is_7_ = Class50_Sub1.unwrap(false, unpacked[i][i_5_], 53146732);
        if (this.discardUnpacked == 1) {
            unpacked[i][i_5_] = null;
            if (index.fileLimits[i] == 1) unpacked[i] = null;
        } else if (this.discardUnpacked == 2) unpacked[i] = null;
        return is_7_;
    }

    private final boolean unpackFile(int i, byte i_8_, int[] is, int groupId) {
        anInt628++;
        if (!isValidGroup(groupId, (byte) -40)) return false;
        if (packed[groupId] == null) return false;
        int i_10_ = index.fileCounts[groupId];
        int[] is_11_ = index.fileIds[groupId];
        if (unpacked[groupId] == null) unpacked[groupId] = new Object[index.fileLimits[groupId]];
        Object[] objects = unpacked[groupId];
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
        byte[] unpacked;
        if (is == null || (is[0] == 0 && is[1] == 0 && is[2] == 0 && is[3] == 0)) {
            unpacked = Class50_Sub1.unwrap(false, packed[groupId], 53146732);
        }
        else {
            unpacked = Class50_Sub1.unwrap(true, packed[groupId], 53146732);
            Packet packet = new Packet(unpacked);
            packet.method3367(607818341, is, 5, (packet.aByteArray7154).length);
        }
        byte[] is_15_;
        try {
            is_15_ = Class348_Sub41.decodeContainer(unpacked, -120);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("T3 - " + (is != null) + "," + groupId + "," + unpacked.length + "," + Class59_Sub1.method554(5126, unpacked.length, unpacked) + "," + Class59_Sub1.method554(5126, -2 + unpacked.length, unpacked) + "," + index.groupCrcs[groupId] + "," + index.crc));
        }
        if (discardPacked) packed[groupId] = null;
        if (i_8_ >= -17) getFile((byte) 70, -7);
        if (i_10_ > 1) {
            if (this.discardUnpacked == 2) {
                int i_30_ = is_15_.length;
                int i_31_ = 0xff & is_15_[--i_30_];
                i_30_ -= 4 * (i_31_ * i_10_);
                Packet packet = new Packet(is_15_);
                int i_32_ = 0;
                int i_33_ = 0;
                packet.pos = i_30_;
                for (int i_34_ = 0; i_34_ < i_31_; i_34_++) {
                    int i_35_ = 0;
                    for (int i_36_ = 0; i_36_ < i_10_; i_36_++) {
                        i_35_ += packet.readInt((byte) -126);
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
                packet.pos = i_30_;
                i_32_ = 0;
                int i_39_ = 0;
                for (int i_40_ = 0; i_40_ < i_31_; i_40_++) {
                    int i_41_ = 0;
                    for (int i_42_ = 0; i_42_ < i_10_; i_42_++) {
                        i_41_ += packet.readInt((byte) -126);
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
                Packet packet = new Packet(is_15_);
                int[] is_18_ = new int[i_10_];
                packet.pos = i_16_;
                for (int i_19_ = 0; i_19_ < i_17_; i_19_++) {
                    int i_20_ = 0;
                    for (int i_21_ = 0; i_21_ < i_10_; i_21_++) {
                        i_20_ += packet.readInt((byte) -126);
                        is_18_[i_21_] += i_20_;
                    }
                }
                byte[][] is_22_ = new byte[i_10_][];
                for (int i_23_ = 0; i_10_ > i_23_; i_23_++) {
                    is_22_[i_23_] = new byte[is_18_[i_23_]];
                    is_18_[i_23_] = 0;
                }
                packet.pos = i_16_;
                int i_24_ = 0;
                for (int i_25_ = 0; i_25_ < i_17_; i_25_++) {
                    int i_26_ = 0;
                    for (int i_27_ = 0; i_10_ > i_27_; i_27_++) {
                        i_26_ += packet.readInt((byte) -126);
                        Class214.method1577(is_15_, i_24_, is_22_[i_27_], is_18_[i_27_], i_26_);
                        i_24_ += i_26_;
                        is_18_[i_27_] += i_26_;
                    }
                }
                for (int i_28_ = 0; i_10_ > i_28_; i_28_++) {
                    int i_29_;
                    if (is_11_ == null) i_29_ = i_28_;
                    else i_29_ = is_11_[i_28_];
                    if (this.discardUnpacked != 0) objects[i_29_] = is_22_[i_28_];
                    else objects[i_29_] = Class179.wrap(is_22_[i_28_], false, (byte) 126);
                }
            }
        } else {
            int i_44_;
            if (is_11_ != null) i_44_ = is_11_[0];
            else i_44_ = 0;
            if (this.discardUnpacked != 0) objects[i_44_] = is_15_;
            else objects[i_44_] = Class179.wrap(is_15_, false, (byte) 104);
        }
        return true;
    }

    private final boolean indexReady(boolean bool) {
        anInt652++;
        if (index == null) {
            index = provider.index((byte) 56);
            if (index == null) return false;
            packed = new Object[index.groupLimit];
            unpacked = new Object[index.groupLimit][];
        }
        if (bool != false) provider = null;
        return true;
    }

    private final void fetchGroup(int i, int i_61_) {
        if (i_61_ > -105) unpacked = null;
        if (!discardPacked) packed[i] = Class179.wrap(provider.fetchGroup(i, (byte) 73), false, (byte) 123);
        else packed[i] = provider.fetchGroup(i, (byte) 12);
        anInt665++;
    }

    final int fileLimit(int i, int i_62_) {
        if (i != 0) getCrc(-61);
        anInt645++;
        if (!isValidGroup(i_62_, (byte) -40)) return 0;
        return index.fileLimits[i_62_];
    }

    final byte[] getFile(int i, int i_64_, int i_65_) {
        if (i != -1860) unpacked = null;
        anInt651++;
        return getFile(i_64_, i_65_, i ^ ~0x741, null);
    }

    final int groupSize(int i) {
        anInt637++;
        if (i != -1) return 49;
        if (!indexReady(false)) return -1;
        return index.fileLimits.length;
    }

    final byte[] getFile(byte i, int i_70_) {
        anInt630++;
        if (!indexReady(false)) return null;
        if (index.fileLimits.length == 1) return getFile(i ^ ~0x70a, 0, i_70_);
        if (!isValidGroup(i_70_, (byte) -40)) return null;
        if (i != 73) unpacked = null;
        if (index.fileLimits[i_70_] == 1) return getFile(i ^ ~0x70a, i_70_, 0);
        throw new RuntimeException();
    }

    private final boolean isValidFile(int i, int i_73_, int i_74_) {
        anInt662++;
        if (!indexReady(false)) return false;
        if (i_74_ < i_73_ || i < 0 || (index.fileLimits.length <= i_74_) || (index.fileLimits[i_74_] <= i)) {
            if (Class285.aBoolean4741) throw new IllegalArgumentException(i_74_ + "," + i);
            return false;
        }
        return true;
    }

    final boolean requestDownload(int i, int i_76_, int i_77_) {
        anInt638++;
        if (!isValidFile(i_77_, 0, i_76_)) return false;
        if (unpacked[i_76_] != null && unpacked[i_76_][i_77_] != null) return true;
        if (i != -10499) return true;
        if (packed[i_76_] != null) return true;
        fetchGroup(i_76_, -125);
        return packed[i_76_] != null;
    }

    final boolean isFileReady(boolean bool, int i) {
        anInt661++;
        if (!indexReady(bool)) return false;
        if (index.fileLimits.length == 1) return requestDownload(-10499, 0, i);
        if (!isValidGroup(i, (byte) -40)) return false;
        if (index.fileLimits[i] == 1) return requestDownload(-10499, i, 0);
        if (bool != false) return false;
        throw new RuntimeException();
    }

    Js5(ResourceProvider resourceProvider, boolean discardPacked, int discardUnpacked) {
        if (discardUnpacked < 0 || discardUnpacked > 2) throw new IllegalArgumentException("js5: Invalid value " + discardUnpacked + " supplied for discardunpacked");
        provider = resourceProvider;
        this.discardPacked = discardPacked;
        this.discardUnpacked = discardUnpacked;
    }
}
