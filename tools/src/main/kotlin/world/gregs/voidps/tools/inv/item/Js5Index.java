package world.gregs.voidps.tools.inv.item;/* Class291 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Js5Index {
    int crc;
    int[][] fileIds;
    int[] groupVersions;
    NameHashTable groupNameTable;
    int[] fileLimits;
    int[] fileCounts;
    int groupCount;
    NameHashTable[] fileNameTables;
    int[] groupCrcs;
    byte[][] groupHashes;
    static int anInt3731;
    int version;
    int[] groupNames;
    int groupLimit;
    int[][] fileNames;
    int[] groupIds;
    private byte[] whirlpool;
    static int anInt3741;

    private final void loadIndex(byte i, byte[] is) {
        anInt3731++;
        Packet packet = new Packet(Class348_Sub41.decodeContainer(is, -105));
        int i_2_ = packet.readUnsignedByte(255);
        if (i_2_ < 5 || i_2_ > 6) throw new RuntimeException();
        if (i_2_ < 6) this.version = 0;
        else this.version = packet.readInt((byte) -126);
        int i_3_ = packet.readUnsignedByte(255);
        boolean bool = (i_3_ & 0x1) != 0;
        boolean bool_4_ = (i_3_ & 0x2) != 0;
        this.groupCount = packet.readUnsignedShort(842397944);
        int i_5_ = 0;
        this.groupIds = new int[this.groupCount];
        int i_6_ = -1;
        for (int i_7_ = 0; i_7_ < this.groupCount; i_7_++) {
            this.groupIds[i_7_] = i_5_ += packet.readUnsignedShort(842397944);
            if (i_6_ < this.groupIds[i_7_]) i_6_ = this.groupIds[i_7_];
        }
        this.groupLimit = i_6_ - -1;
        if (bool_4_) this.groupHashes = new byte[this.groupLimit][];
        this.groupCrcs = new int[this.groupLimit];
        this.fileLimits = new int[this.groupLimit];
        this.fileCounts = new int[this.groupLimit];
        this.groupVersions = new int[this.groupLimit];
        this.fileIds = new int[this.groupLimit][];
        if (bool) {
            this.groupNames = new int[this.groupLimit];
            for (int i_8_ = 0; i_8_ < this.groupLimit; i_8_++)
                this.groupNames[i_8_] = -1;
            for (int i_9_ = 0; (this.groupCount > i_9_); i_9_++)
                this.groupNames[(this.groupIds[i_9_])] = packet.readInt((byte) -126);
            this.groupNameTable = new NameHashTable(this.groupNames);
        }
        if (i >= -83) loadIndex((byte) 42, null);
        for (int i_10_ = 0; i_10_ < this.groupCount; i_10_++)
            this.groupCrcs[(this.groupIds[i_10_])] = packet.readInt((byte) -126);
        if (bool_4_) {
            for (int i_11_ = 0; i_11_ < this.groupCount; i_11_++) {
                byte[] is_12_ = new byte[64];
                packet.gdata(2147483647, 0, 64, is_12_);
                this.groupHashes[(this.groupIds[i_11_])] = is_12_;
            }
        }
        for (int i_13_ = 0; i_13_ < this.groupCount; i_13_++)
            this.groupVersions[(this.groupIds[i_13_])] = packet.readInt((byte) -126);
        for (int i_14_ = 0; this.groupCount > i_14_; i_14_++)
            this.fileCounts[(this.groupIds[i_14_])] = packet.readUnsignedShort(842397944);
        for (int i_15_ = 0; this.groupCount > i_15_; i_15_++) {
            int i_16_ = this.groupIds[i_15_];
            i_5_ = 0;
            int i_17_ = this.fileCounts[i_16_];
            int i_18_ = -1;
            this.fileIds[i_16_] = new int[i_17_];
            for (int i_19_ = 0; i_17_ > i_19_; i_19_++) {
                int i_20_ = (this.fileIds[i_16_][i_19_] = i_5_ += packet.readUnsignedShort(842397944));
                if (i_18_ < i_20_) i_18_ = i_20_;
            }
            this.fileLimits[i_16_] = i_18_ + 1;
            if (1 + i_18_ == i_17_) this.fileIds[i_16_] = null;
        }
        if (bool) {
            this.fileNames = new int[i_6_ + 1][];
            this.fileNameTables = new NameHashTable[1 + i_6_];
            for (int i_21_ = 0; i_21_ < this.groupCount; i_21_++) {
                int i_22_ = this.groupIds[i_21_];
                int i_23_ = this.fileCounts[i_22_];
                this.fileNames[i_22_] = new int[this.fileLimits[i_22_]];
                for (int i_24_ = 0; this.fileLimits[i_22_] > i_24_; i_24_++)
                    this.fileNames[i_22_][i_24_] = -1;
                for (int i_25_ = 0; i_23_ > i_25_; i_25_++) {
                    int i_26_;
                    if (this.fileIds[i_22_] != null) i_26_ = (this.fileIds[i_22_][i_25_]);
                    else i_26_ = i_25_;
                    this.fileNames[i_22_][i_26_] = packet.readInt((byte) -126);
                }
                this.fileNameTables[i_22_] = new NameHashTable(this.fileNames[i_22_]);
            }
        }
    }

    Js5Index(byte[] is, int i, byte[] is_27_) {
        try {
            this.crc = Class59_Sub1.method554(5126, is.length, is);
            if (i != this.crc) throw new RuntimeException();
            if (is_27_ != null) {
                if (is_27_.length != 64) throw new RuntimeException();
                whirlpool = Class348_Sub1_Sub2.method2730(4567, 0, is, is.length);
                for (int i_28_ = 0; i_28_ < 64; i_28_++) {
                    if (whirlpool[i_28_] != is_27_[i_28_]) throw new RuntimeException();
                }
            }
            loadIndex((byte) -120, is);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("vw.<init>(" + (is != null ? "{...}" : "null") + ',' + i + ',' + (is_27_ != null ? "{...}" : "null") + ')'));
        }
    }
}
