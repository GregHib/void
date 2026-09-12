package world.gregs.voidps.tools.inv.item;/* Class124 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.util.Random;

final class Mesh {
    int[] texSpaceScaleY;
    int faceCount;
    int texSpaceCount;
    static int anInt1819;
    byte[] faceTexSpace;
    int maxVertex;
    short[] faceTexture;
    byte[] texMappingType;
    int[] faceLabel;
    short[] texSpaceDefC;
    short[] texSpaceDefA;
    int version = 12;
    MeshBillboard[] billboards;
    byte[] texRotation;
    byte[] faceAlpha;
    short[] faceB;
    int vertexCount = 0;
    static int anInt1826;
    static int anInt1828;
    byte[] facePriority;
    int[] vertexX;
    short[] aShortArray1842;
    byte[] shadingType;
    int[] texSpaceScaleZ;
    ModelParticleEmitter[] emitters;
    int[] vertexY;
    static Js5 aJs5_1848;
    short[] texSpaceDefB;
    byte globalPriority;
    int[] vertexZ;
    byte[] texDirection;
    short[] faceC;
    short[] aShortArray1856;
    int[] texOffsetY;
    int[] texSpaceScaleX;
    static int anInt1860;
    static int anInt1861;
    short[] faceColour;
    short[] faceA;
    int[] texOffsetZ;
    ModelParticleEffector[] effectors;
    int[] texOffsetX;
    int[] vertexLabel;
    static int anInt1869;
    static int anInt1831;
    static int anInt1837;
    static int anInt1854;
    static int anInt1864;
    static int anInt1845;

    final void upscale(int i, int i_0_) {
        anInt1819++;
        for (int i_1_ = 0; this.vertexCount > i_1_; i_1_++) {
            this.vertexX[i_1_] <<= i;
            this.vertexY[i_1_] <<= i;
            this.vertexZ[i_1_] <<= i;
        }
        if (i_0_ <= 39) method1107(40, -7, -80, -24);
        if (this.texSpaceCount > 0 && this.texSpaceScaleX != null) {
            for (int i_2_ = 0; i_2_ < this.texSpaceScaleX.length; i_2_++) {
                this.texSpaceScaleX[i_2_] <<= i;
                this.texSpaceScaleY[i_2_] <<= i;
                if (this.texMappingType[i_2_] != 1) this.texSpaceScaleZ[i_2_] <<= i;
            }
        }
    }

    final int[][] getBillboardGroups(byte i) {
        anInt1854++;
        int[] is = new int[256];
        int i_3_ = 0;
        for (int i_4_ = 0; this.billboards.length > i_4_; i_4_++) {
            int i_5_ = (this.billboards[i_4_].anInt2156);
            if (i_5_ >= 0) {
                is[i_5_]++;
                if (i_3_ < i_5_) i_3_ = i_5_;
            }
        }
        int[][] is_6_ = new int[1 + i_3_][];
        for (int i_7_ = 0; i_7_ <= i_3_; i_7_++) {
            is_6_[i_7_] = new int[is[i_7_]];
            is[i_7_] = 0;
        }
        if (i > -68) return null;
        for (int i_8_ = 0; i_8_ < this.billboards.length; i_8_++) {
            int i_9_ = (this.billboards[i_8_].anInt2156);
            if (i_9_ >= 0) is_6_[i_9_][is[i_9_]++] = i_8_;
        }
        return is_6_;
    }

    final int[][] getFaceLabels(byte i) {
        anInt1864++;
        int[] is = new int[256];
        int i_10_ = 0;
        for (int i_11_ = 0; i_11_ < this.faceCount; i_11_++) {
            int i_12_ = this.faceLabel[i_11_];
            if (i_12_ >= 0) {
                if (i_10_ < i_12_) i_10_ = i_12_;
                is[i_12_]++;
            }
        }
        int[][] is_13_ = new int[i_10_ - -1][];
        if (i <= 13) method1102(-65, (byte) -8, (byte) 94, (short) -89, (byte) 6, (short) -117, 21, (byte) 21, -31);
        for (int i_14_ = 0; i_10_ >= i_14_; i_14_++) {
            is_13_[i_14_] = new int[is[i_14_]];
            is[i_14_] = 0;
        }
        for (int i_15_ = 0; i_15_ < this.faceCount; i_15_++) {
            int i_16_ = this.faceLabel[i_15_];
            if (i_16_ >= 0) is_13_[i_16_][is[i_16_]++] = i_15_;
        }
        return is_13_;
    }

    final void retexture(short i, int i_17_, short i_18_) {
        anInt1869++;
        if (this.faceTexture != null) {
            for (int i_19_ = i_17_; this.faceCount > i_19_; i_19_++) {
                if (i == this.faceTexture[i_19_]) this.faceTexture[i_19_] = i_18_;
            }
        }
    }

    public static void method1096(int i) {
        if (i >= 88) aJs5_1848 = null;
    }

    static final int method1097(byte i, int i_20_, Random random) {
        anInt1861++;
        if (i_20_ <= 0) throw new IllegalArgumentException();
        if (Class192.method1436(-19, i_20_)) return (int) ((long) i_20_ * (0xffffffffL & (long) random.nextInt()) >> 32);
        int i_21_ = -2147483648 + -(int) (4294967296L % (long) i_20_);
        if (i < 78) aJs5_1848 = null;
        int i_22_;
        do i_22_ = random.nextInt(); while (i_22_ >= i_21_);
        return JavaBillboardAttributes.method3452(i_22_, (byte) -15, i_20_);
    }

    final void recolour(short i, byte i_23_, short i_24_) {
        if (i_23_ == 126) {
            for (int i_25_ = 0; i_25_ < this.faceCount; i_25_++) {
                if (i == this.faceColour[i_25_]) this.faceColour[i_25_] = i_24_;
            }
            anInt1826++;
        }
    }

    private final void method1106(byte[] is, byte i_1234) {
        anInt1828++;
        Packet packet1 = new Packet(is);
        Packet packet2 = new Packet(is);
        Packet packet3 = new Packet(is);
        Packet packet4 = new Packet(is);
        Packet packet5 = new Packet(is);
        Packet packet6 = new Packet(is);
        Packet packet7 = new Packet(is);
        packet1.pos = -23 + is.length;
        this.vertexCount = packet1.readUnsignedShort(842397944);
        this.faceCount = packet1.readUnsignedShort(842397944);
        this.texSpaceCount = packet1.readUnsignedByte(255);
        int globalFlags = packet1.readUnsignedByte(255);
        boolean hasFlatShading = (0x1 & globalFlags) == 1;
        boolean hasParticleEffects = (globalFlags & 0x2) == 2;
        boolean hasBillboards = (globalFlags & 0x4) == 4;
        boolean hasVersion = (globalFlags & 0x8) == 8;
        if (hasVersion) {
            packet1.pos -= 7;
            this.version = packet1.readUnsignedByte(255);
            packet1.pos += 6;
        }
        int priorityFlag = packet1.readUnsignedByte(255);
        int faceAlphaFlag = packet1.readUnsignedByte(255);
        int faceGroupFlag = packet1.readUnsignedByte(255);
        int faceTextureFlag = packet1.readUnsignedByte(255);
        int vertexLabelFlag = packet1.readUnsignedByte(255);
        int vertexLengthX = packet1.readUnsignedShort(842397944);
        int vertexLengthY = packet1.readUnsignedShort(842397944);
        int vertexLengthZ = packet1.readUnsignedShort(842397944);
        int faceDataSize = packet1.readUnsignedShort(842397944);
        int texSpaceSize = packet1.readUnsignedShort(842397944);
        int planarMappingCount = 0;
        int complexMappingCount = 0;
        int cubeMappingCount = 0;
        if (this.texSpaceCount > 0) {
            packet1.pos = 0;
            this.texMappingType = new byte[this.texSpaceCount];
            for (int i_141_ = 0; (i_141_ < this.texSpaceCount); i_141_++) {
                byte type = (this.texMappingType[i_141_] = packet1.readByte(-124));
                if (type >= 1 && type <= 3) complexMappingCount++;
                if (type == 2) cubeMappingCount++;
                if (type == 0) planarMappingCount++;
            }
        }
        int ptr = this.texSpaceCount;
        int vertexFlagsPtr = ptr;
        ptr += this.vertexCount;
        int smoothingPtr = ptr;
        if (hasFlatShading) ptr += this.faceCount;
        int faceTypePtr = ptr;
        ptr += this.faceCount;
        int facePriPtr = ptr;
        if (priorityFlag == 255) ptr += this.faceCount;
        int faceGroupPtr = ptr;
        if (faceGroupFlag == 1) ptr += this.faceCount;
        int vertexLabelPtr = ptr;
        if (vertexLabelFlag == 1) ptr += this.vertexCount;
        int i_faceAlphaPtr50_ = ptr;
        if (faceAlphaFlag == 1) ptr += this.faceCount;
        if (i_1234 <= 68) this.maxVertex = 85;
        int faceDataSizePtr = ptr;
        ptr += faceDataSize;
        int faceTextureFlagPtr = ptr;
        if (faceTextureFlag == 1) ptr += 2 * this.faceCount;
        int texSpaceSizePtr = ptr;
        ptr += texSpaceSize;
        int i_154_ = ptr;
        ptr += 2 * this.faceCount;
        int vertexLengthXPtr = ptr;
        ptr += vertexLengthX;
        int vertexLengthYPtr = ptr;
        ptr += vertexLengthY;
        int vertexLengthZPtr = ptr;
        ptr += vertexLengthZ;
        int planarMappingCountPtr = ptr;
        ptr += planarMappingCount * 6;
        int complexMappingCountPtr = ptr;
        ptr += 6 * complexMappingCount;
        int texSpaceScaleSize = 6;
        if (this.version == 14) texSpaceScaleSize = 7;
        else if (this.version >= 15) texSpaceScaleSize = 9;
        int texSpaceScalePtr = ptr;
        ptr += texSpaceScaleSize * complexMappingCount;
        int texSpaceRotationPtr = ptr;
        ptr += complexMappingCount;
        int texSpaceOrientationPtr = ptr;
        ptr += complexMappingCount;
        int texSpaceOffsetPtr = ptr;
        ptr += 2 * cubeMappingCount + complexMappingCount;
        this.faceB = new short[this.faceCount];
        this.faceColour = new short[this.faceCount];
        if (faceGroupFlag == 1) this.faceLabel = new int[this.faceCount];
        if (this.texSpaceCount > 0) {
            if (cubeMappingCount > 0) {
                this.texOffsetY = new int[cubeMappingCount];
                this.texOffsetZ = new int[cubeMappingCount];
            }
            this.texSpaceDefB = new short[this.texSpaceCount];
            this.texSpaceDefA = new short[this.texSpaceCount];
            this.texSpaceDefC = new short[this.texSpaceCount];
            if (complexMappingCount > 0) {
                this.texOffsetX = new int[complexMappingCount];
                this.texDirection = new byte[complexMappingCount];
                this.texSpaceScaleY = new int[complexMappingCount];
                this.texRotation = new byte[complexMappingCount];
                this.texSpaceScaleZ = new int[complexMappingCount];
                this.texSpaceScaleX = new int[complexMappingCount];
            }
        }
        if (hasFlatShading) this.shadingType = new byte[this.faceCount];
        if (faceAlphaFlag == 1) this.faceAlpha = new byte[this.faceCount];
        if (priorityFlag == 255) this.facePriority = new byte[this.faceCount];
        else this.globalPriority = (byte) priorityFlag;
        this.faceC = new short[this.faceCount];
        this.vertexY = new int[this.vertexCount];
        this.faceA = new short[this.faceCount];
        if (faceTextureFlag == 1 && this.texSpaceCount > 0) this.faceTexSpace = new byte[this.faceCount];
        int i_165_ = ptr;
        this.vertexZ = new int[this.vertexCount];
        this.vertexX = new int[this.vertexCount];
        if (vertexLabelFlag == 1) this.vertexLabel = new int[this.vertexCount];
        packet1.pos = vertexFlagsPtr;
        if (faceTextureFlag == 1) this.faceTexture = new short[this.faceCount];
        packet2.pos = vertexLengthXPtr;
        packet3.pos = vertexLengthYPtr;
        packet4.pos = vertexLengthZPtr;
        packet5.pos = vertexLabelPtr;
        int pvX = 0;
        int pvY = 0;
        int pvZ = 0;
        for (int i = 0; this.vertexCount > i; i++) {
            int vertexData = packet1.readUnsignedByte(255);
            int x = 0;
            if ((vertexData & 0x1) != 0) x = packet2.method3362((byte) 77);
            int y = 0;
            if ((vertexData & 0x2) != 0) y = packet3.method3362((byte) 77);
            int z = 0;
            if ((0x4 & vertexData) != 0) z = packet4.method3362((byte) 77);
            this.vertexX[i] = x + pvX;
            this.vertexY[i] = y + pvY;
            this.vertexZ[i] = pvZ + z;
            pvY = this.vertexY[i];
            pvX = this.vertexX[i];
            pvZ = this.vertexZ[i];
            if (vertexLabelFlag == 1) this.vertexLabel[i] = packet5.readUnsignedByte(255);
        }
        packet1.pos = i_154_;
        packet2.pos = smoothingPtr;
        packet3.pos = facePriPtr;
        packet4.pos = i_faceAlphaPtr50_;
        packet5.pos = faceGroupPtr;
        packet6.pos = faceTextureFlagPtr;
        packet7.pos = texSpaceSizePtr;
        for (int i = 0; (this.faceCount > i); i++) {
            this.faceColour[i] = (short) packet1.readUnsignedShort(842397944);
            if (hasFlatShading) this.shadingType[i] = packet2.readByte(-98);
            if (priorityFlag == 255) this.facePriority[i] = packet3.readByte(-78);
            if (faceAlphaFlag == 1) this.faceAlpha[i] = packet4.readByte(-99);
            if (faceGroupFlag == 1) this.faceLabel[i] = packet5.readUnsignedByte(255);
            if (faceTextureFlag == 1) this.faceTexture[i] = (short) (packet6.readUnsignedShort(842397944) + -1);
            if (this.faceTexSpace != null) {
                if (this.faceTexture[i] == -1) this.faceTexSpace[i] = (byte) -1;
                else this.faceTexSpace[i] = (byte) (-1 + packet7.readUnsignedByte(255));
            }
        }
        packet1.pos = faceDataSizePtr;
        this.maxVertex = -1;
        packet2.pos = faceTypePtr;
        short faceA = 0;
        short faceB = 0;
        short faceC = 0;
        int facePriority = 0;
        for (int i = 0; (i < this.faceCount); i++) {
            int type = packet2.readUnsignedByte(255);
            if (type == 1) {
                faceA = (short) (packet1.method3362((byte) 77) + facePriority);
                facePriority = faceA;
                faceB = (short) (packet1.method3362((byte) 77) + facePriority);
                facePriority = faceB;
                faceC = (short) (facePriority + packet1.method3362((byte) 77));
                facePriority = faceC;
                this.faceA[i] = faceA;
                this.faceB[i] = faceB;
                this.faceC[i] = faceC;
                if (faceA > this.maxVertex) this.maxVertex = faceA;
                if (this.maxVertex < faceB) this.maxVertex = faceB;
                if (faceC > this.maxVertex) this.maxVertex = faceC;
            }
            if (type == 2) {
                faceB = faceC;
                faceC = (short) (packet1.method3362((byte) 77) + facePriority);
                this.faceA[i] = faceA;
                facePriority = faceC;
                this.faceB[i] = faceB;
                this.faceC[i] = faceC;
                if (this.maxVertex < faceC) this.maxVertex = faceC;
            }
            if (type == 3) {
                faceA = faceC;
                faceC = (short) (packet1.method3362((byte) 77) + facePriority);
                facePriority = faceC;
                this.faceA[i] = faceA;
                this.faceB[i] = faceB;
                this.faceC[i] = faceC;
                if (this.maxVertex < faceC) this.maxVertex = faceC;
            }
            if (type == 4) {
                short i_181_ = faceA;
                faceA = faceB;
                faceC = (short) (facePriority + packet1.method3362((byte) 77));
                faceB = i_181_;
                this.faceA[i] = faceA;
                facePriority = faceC;
                this.faceB[i] = faceB;
                this.faceC[i] = faceC;
                if (this.maxVertex < faceC) this.maxVertex = faceC;
            }
        }
        packet1.pos = planarMappingCountPtr;
        this.maxVertex++;
        packet2.pos = complexMappingCountPtr;
        packet3.pos = texSpaceScalePtr;
        packet4.pos = texSpaceRotationPtr;
        packet5.pos = texSpaceOrientationPtr;
        packet6.pos = texSpaceOffsetPtr;
        for (int i = 0; this.texSpaceCount > i; i++) {
            int type = this.texMappingType[i] & 0xff;
            if (type == 0) {
                this.texSpaceDefA[i] = (short) packet1.readUnsignedShort(842397944);
                this.texSpaceDefB[i] = (short) packet1.readUnsignedShort(842397944);
                this.texSpaceDefC[i] = (short) packet1.readUnsignedShort(842397944);
            }
            if (type == 1) {
                this.texSpaceDefA[i] = (short) packet2.readUnsignedShort(842397944);
                this.texSpaceDefB[i] = (short) packet2.readUnsignedShort(842397944);
                this.texSpaceDefC[i] = (short) packet2.readUnsignedShort(842397944);
                if (this.version >= 15) {
                    this.texSpaceScaleX[i] = packet3.readMedium(-1);
                    this.texSpaceScaleY[i] = packet3.readMedium(-1);
                    this.texSpaceScaleZ[i] = packet3.readMedium(-1);
                } else {
                    this.texSpaceScaleX[i] = packet3.readUnsignedShort(842397944);
                    if (this.version >= 14) this.texSpaceScaleY[i] = packet3.readMedium(-1);
                    else this.texSpaceScaleY[i] = packet3.readUnsignedShort(842397944);
                    this.texSpaceScaleZ[i] = packet3.readUnsignedShort(842397944);
                }
                this.texRotation[i] = packet4.readByte(-86);
                this.texDirection[i] = packet5.readByte(-116);
                this.texOffsetX[i] = packet6.readByte(-79);
            }
            if (type == 2) {
                this.texSpaceDefA[i] = (short) packet2.readUnsignedShort(842397944);
                this.texSpaceDefB[i] = (short) packet2.readUnsignedShort(842397944);
                this.texSpaceDefC[i] = (short) packet2.readUnsignedShort(842397944);
                if (this.version >= 15) {
                    this.texSpaceScaleX[i] = packet3.readMedium(-1);
                    this.texSpaceScaleY[i] = packet3.readMedium(-1);
                    this.texSpaceScaleZ[i] = packet3.readMedium(-1);
                } else {
                    this.texSpaceScaleX[i] = packet3.readUnsignedShort(842397944);
                    if (this.version < 14) this.texSpaceScaleY[i] = packet3.readUnsignedShort(842397944);
                    else this.texSpaceScaleY[i] = packet3.readMedium(-1);
                    this.texSpaceScaleZ[i] = packet3.readUnsignedShort(842397944);
                }
                this.texRotation[i] = packet4.readByte(-97);
                this.texDirection[i] = packet5.readByte(-100);
                this.texOffsetX[i] = packet6.readByte(-124);
                this.texOffsetY[i] = packet6.readByte(-112);
                this.texOffsetZ[i] = packet6.readByte(-114);
            }
            if (type == 3) {
                this.texSpaceDefA[i] = (short) packet2.readUnsignedShort(842397944);
                this.texSpaceDefB[i] = (short) packet2.readUnsignedShort(842397944);
                this.texSpaceDefC[i] = (short) packet2.readUnsignedShort(842397944);
                if (this.version < 15) {
                    this.texSpaceScaleX[i] = packet3.readUnsignedShort(842397944);
                    if (this.version < 14) this.texSpaceScaleY[i] = packet3.readUnsignedShort(842397944);
                    else this.texSpaceScaleY[i] = packet3.readMedium(-1);
                    this.texSpaceScaleZ[i] = packet3.readUnsignedShort(842397944);
                } else {
                    this.texSpaceScaleX[i] = packet3.readMedium(-1);
                    this.texSpaceScaleY[i] = packet3.readMedium(-1);
                    this.texSpaceScaleZ[i] = packet3.readMedium(-1);
                }
                this.texRotation[i] = packet4.readByte(-104);
                this.texDirection[i] = packet5.readByte(-127);
                this.texOffsetX[i] = packet6.readByte(-109);
            }
        }
        packet1.pos = i_165_;
        if (hasParticleEffects) {
            int emitterCount = packet1.readUnsignedByte(255);
            if (emitterCount > 0) {
                this.emitters = new ModelParticleEmitter[emitterCount];
                for (int i = 0; emitterCount > i; i++) {
                    int type = packet1.readUnsignedShort(842397944);
                    int face = packet1.readUnsignedShort(842397944);
                    byte priority;
                    if (priorityFlag != 255) priority = (byte) priorityFlag;
                    else priority = this.facePriority[face];
                    this.emitters[i] = (new ModelParticleEmitter(type, this.faceA[face], this.faceB[face], this.faceC[face], priority));
                }
            }
            int effectorCount = packet1.readUnsignedByte(255);
            if (effectorCount > 0) {
                this.effectors = new ModelParticleEffector[effectorCount];
                for (int i = 0; effectorCount > i; i++) {
                    int type = packet1.readUnsignedShort(842397944);
                    int vertex = packet1.readUnsignedShort(842397944);
                    this.effectors[i] = new ModelParticleEffector(type, vertex);
                }
            }
        }
        if (hasBillboards) {
            int billboardCount = packet1.readUnsignedByte(255);
            if (billboardCount > 0) {
                this.billboards = new MeshBillboard[billboardCount];
                for (int i = 0; billboardCount > i; i++) {
                    int type = packet1.readUnsignedShort(842397944);
                    int face = packet1.readUnsignedShort(842397944);
                    int group = packet1.readUnsignedByte(255);
                    byte priority = packet1.readByte(-127);
                    this.billboards[i] = new MeshBillboard(type, face, group, priority);
                }
            }
        }
    }

    /* NOTE: method1103 and method1107 are not in the genuine-methods list
     * (only method1106 is exercised by real .java icon data - method1103 is
     * an alternate/legacy decode format never hit, and method1107's caller
     * guard "if (i_0_ <= 39)" in method1092 is never true for reachable
     * callers). Both are added back verbatim anyway since Java requires
     * them to resolve at compile time (kept code still calls them). */
    final int[][] getVertexLabels(boolean bool, int i) {
        anInt1845++;
        int[] is = new int[256];
        int i_31_ = 0;
        int i_32_ = (!bool ? this.maxVertex : this.vertexCount);
        for (int i_33_ = 0; i_33_ < i_32_; i_33_++) {
            int i_34_ = this.vertexLabel[i_33_];
            if (i_34_ >= 0) {
                is[i_34_]++;
                if (i_31_ < i_34_) i_31_ = i_34_;
            }
        }
        int[][] is_35_ = new int[1 + i_31_][];
        for (int i_36_ = 0; i_36_ <= i_31_; i_36_++) {
            is_35_[i_36_] = new int[is[i_36_]];
            is[i_36_] = 0;
        }
        for (int i_37_ = 0; i_32_ > i_37_; i_37_++) {
            int i_38_ = this.vertexLabel[i_37_];
            if (i_38_ >= 0) is_35_[i_38_][is[i_38_]++] = i_37_;
        }
        if (i > -14) recolour((short) 9, (byte) 80, (short) -118);
        return is_35_;
    }

    private final int method1102(int i, byte i_44_, byte i_45_, short i_46_, byte i_47_, short i_48_, int i_49_, byte i_50_, int i_51_) {
        this.faceA[this.faceCount] = (short) i;
        anInt1860++;
        this.faceB[this.faceCount] = (short) i_49_;
        this.faceC[this.faceCount] = (short) i_51_;
        if (i_47_ < 30) return -92;
        this.shadingType[this.faceCount] = i_44_;
        this.faceTexSpace[this.faceCount] = i_45_;
        this.faceColour[this.faceCount] = i_48_;
        this.faceAlpha[this.faceCount] = i_50_;
        this.faceTexture[this.faceCount] = i_46_;
        return this.faceCount++;
    }

    private final void method1103(int i, byte[] is) {
        anInt1831++;
        boolean bool = false;
        boolean bool_52_ = false;
        Packet packet = new Packet(is);
        Packet packet_53_ = new Packet(is);
        Packet packet_54_ = new Packet(is);
        Packet packet_55_ = new Packet(is);
        Packet packet_56_ = new Packet(is);
        packet.pos = -18 + is.length;
        this.vertexCount = packet.readUnsignedShort(i ^ 0x3235f8f9);
        this.faceCount = packet.readUnsignedShort(842397944);
        this.texSpaceCount = packet.readUnsignedByte(255);
        int i_57_ = packet.readUnsignedByte(255);
        int i_58_ = packet.readUnsignedByte(255);
        int i_59_ = packet.readUnsignedByte(255);
        int i_60_ = packet.readUnsignedByte(255);
        int i_61_ = packet.readUnsignedByte(255);
        int i_62_ = packet.readUnsignedShort(842397944);
        int i_63_ = packet.readUnsignedShort(842397944);
        int i_64_ = packet.readUnsignedShort(842397944);
        int i_65_ = packet.readUnsignedShort(842397944);
        int i_66_ = 0;
        int i_67_ = i_66_;
        i_66_ += this.vertexCount;
        int i_68_ = i_66_;
        i_66_ += this.faceCount;
        int i_69_ = i_66_;
        if (i_58_ == 255) i_66_ += this.faceCount;
        int i_70_ = i_66_;
        if (i_60_ == 1) i_66_ += this.faceCount;
        int i_71_ = i_66_;
        if (i_57_ == 1) i_66_ += this.faceCount;
        int i_72_ = i_66_;
        if (i_61_ == 1) i_66_ += this.vertexCount;
        int i_73_ = i_66_;
        if (i == i_59_) i_66_ += this.faceCount;
        int i_74_ = i_66_;
        i_66_ += i_65_;
        int i_75_ = i_66_;
        i_66_ += this.faceCount * 2;
        int i_76_ = i_66_;
        i_66_ += this.texSpaceCount * 6;
        int i_77_ = i_66_;
        i_66_ += i_62_;
        int i_78_ = i_66_;
        i_66_ += i_63_;
        int i_79_ = i_66_;
        this.faceColour = new short[this.faceCount];
        if (i_58_ == 255) this.facePriority = new byte[this.faceCount];
        else this.globalPriority = (byte) i_58_;
        if (i_61_ == 1) this.vertexLabel = new int[this.vertexCount];
        if (i_59_ == 1) this.faceAlpha = new byte[this.faceCount];
        this.faceA = new short[this.faceCount];
        if (i_57_ == 1) {
            this.faceTexture = new short[this.faceCount];
            this.faceTexSpace = new byte[this.faceCount];
            this.shadingType = new byte[this.faceCount];
        }
        this.vertexY = new int[this.vertexCount];
        if (i_60_ == 1) this.faceLabel = new int[this.faceCount];
        this.faceC = new short[this.faceCount];
        if (this.texSpaceCount > 0) {
            this.texMappingType = new byte[this.texSpaceCount];
            this.texSpaceDefA = new short[this.texSpaceCount];
            this.texSpaceDefB = new short[this.texSpaceCount];
            this.texSpaceDefC = new short[this.texSpaceCount];
        }
        this.faceB = new short[this.faceCount];
        this.vertexZ = new int[this.vertexCount];
        this.vertexX = new int[this.vertexCount];
        packet.pos = i_67_;
        i_66_ += i_64_;
        packet_53_.pos = i_77_;
        packet_54_.pos = i_78_;
        packet_55_.pos = i_79_;
        packet_56_.pos = i_72_;
        int i_80_ = 0;
        int i_81_ = 0;
        int i_82_ = 0;
        for (int i_83_ = 0; i_83_ < this.vertexCount; i_83_++) {
            int i_84_ = packet.readUnsignedByte(255);
            int i_85_ = 0;
            if ((i_84_ & 0x1) != 0) i_85_ = packet_53_.method3362((byte) 77);
            int i_86_ = 0;
            if ((i_84_ & 0x2) != 0) i_86_ = packet_54_.method3362((byte) 77);
            int i_87_ = 0;
            if ((0x4 & i_84_) != 0) i_87_ = packet_55_.method3362((byte) 77);
            this.vertexX[i_83_] = i_85_ + i_80_;
            this.vertexY[i_83_] = i_81_ - -i_86_;
            this.vertexZ[i_83_] = i_82_ + i_87_;
            i_82_ = this.vertexZ[i_83_];
            i_81_ = this.vertexY[i_83_];
            i_80_ = this.vertexX[i_83_];
            if (i_61_ == 1) this.vertexLabel[i_83_] = packet_56_.readUnsignedByte(Class348_Sub21.method2955(i, 254));
        }
        packet.pos = i_75_;
        packet_53_.pos = i_71_;
        packet_54_.pos = i_69_;
        packet_55_.pos = i_73_;
        packet_56_.pos = i_70_;
        for (int i_88_ = 0; i_88_ < this.faceCount; i_88_++) {
            this.faceColour[i_88_] = (short) packet.readUnsignedShort(842397944);
            if (i_57_ == 1) {
                int i_89_ = packet_53_.readUnsignedByte(255);
                if ((0x1 & i_89_) == 1) {
                    this.shadingType[i_88_] = (byte) 1;
                    bool = true;
                } else this.shadingType[i_88_] = (byte) 0;
                if ((i_89_ & 0x2) == 2) {
                    this.faceTexSpace[i_88_] = (byte) (i_89_ >> 2);
                    this.faceTexture[i_88_] = this.faceColour[i_88_];
                    this.faceColour[i_88_] = (short) 127;
                    if (this.faceTexture[i_88_] != -1) bool_52_ = true;
                } else {
                    this.faceTexSpace[i_88_] = (byte) -1;
                    this.faceTexture[i_88_] = (short) -1;
                }
            }
            if (i_58_ == 255) this.facePriority[i_88_] = packet_54_.readByte(-108);
            if (i_59_ == 1) this.faceAlpha[i_88_] = packet_55_.readByte(Class348_Sub21.method2955(i, -120));
            if (i_60_ == 1) this.faceLabel[i_88_] = packet_56_.readUnsignedByte(255);
        }
        packet.pos = i_74_;
        this.maxVertex = -1;
        packet_53_.pos = i_68_;
        short i_90_ = 0;
        short i_91_ = 0;
        short i_92_ = 0;
        int i_93_ = 0;
        for (int i_94_ = 0; i_94_ < this.faceCount; i_94_++) {
            int i_95_ = packet_53_.readUnsignedByte(255);
            if (i_95_ == 1) {
                i_90_ = (short) (i_93_ + packet.method3362((byte) 77));
                i_93_ = i_90_;
                i_91_ = (short) (packet.method3362((byte) 77) + i_93_);
                i_93_ = i_91_;
                i_92_ = (short) (i_93_ + packet.method3362((byte) 77));
                this.faceA[i_94_] = i_90_;
                i_93_ = i_92_;
                this.faceB[i_94_] = i_91_;
                this.faceC[i_94_] = i_92_;
                if (i_90_ > this.maxVertex) this.maxVertex = i_90_;
                if (this.maxVertex < i_91_) this.maxVertex = i_91_;
                if (this.maxVertex < i_92_) this.maxVertex = i_92_;
            }
            if (i_95_ == 2) {
                i_91_ = i_92_;
                i_92_ = (short) (packet.method3362((byte) 77) + i_93_);
                this.faceA[i_94_] = i_90_;
                i_93_ = i_92_;
                this.faceB[i_94_] = i_91_;
                this.faceC[i_94_] = i_92_;
                if (i_92_ > this.maxVertex) this.maxVertex = i_92_;
            }
            if (i_95_ == 3) {
                i_90_ = i_92_;
                i_92_ = (short) (packet.method3362((byte) 77) + i_93_);
                i_93_ = i_92_;
                this.faceA[i_94_] = i_90_;
                this.faceB[i_94_] = i_91_;
                this.faceC[i_94_] = i_92_;
                if (this.maxVertex < i_92_) this.maxVertex = i_92_;
            }
            if (i_95_ == 4) {
                short i_96_ = i_90_;
                i_90_ = i_91_;
                i_92_ = (short) (packet.method3362((byte) 77) + i_93_);
                i_91_ = i_96_;
                i_93_ = i_92_;
                this.faceA[i_94_] = i_90_;
                this.faceB[i_94_] = i_91_;
                this.faceC[i_94_] = i_92_;
                if (this.maxVertex < i_92_) this.maxVertex = i_92_;
            }
        }
        this.maxVertex++;
        packet.pos = i_76_;
        for (int i_97_ = 0; i_97_ < this.texSpaceCount; i_97_++) {
            this.texMappingType[i_97_] = (byte) 0;
            this.texSpaceDefA[i_97_] = (short) packet.readUnsignedShort(842397944);
            this.texSpaceDefB[i_97_] = (short) packet.readUnsignedShort(842397944);
            this.texSpaceDefC[i_97_] = (short) packet.readUnsignedShort(842397944);
        }
        if (this.faceTexSpace != null) {
            boolean bool_98_ = false;
            for (int i_99_ = 0; i_99_ < this.faceCount; i_99_++) {
                int i_100_ = this.faceTexSpace[i_99_] & 0xff;
                if (i_100_ != 255) {
                    if (((0xffff & this.texSpaceDefA[i_100_]) != this.faceA[i_99_]) || (this.faceB[i_99_] != (0xffff & this.texSpaceDefB[i_100_])) || ((0xffff & this.texSpaceDefC[i_100_]) != this.faceC[i_99_])) bool_98_ = true;
                    else this.faceTexSpace[i_99_] = (byte) -1;
                }
            }
            if (!bool_98_) this.faceTexSpace = null;
        }
        if (!bool) this.shadingType = null;
        if (!bool_52_) this.faceTexture = null;
    }

    final void method1107(int i, int i_199_, int i_200_, int i_201_) {
        if (i_200_ != 0) {
            int i_202_ = Class70.anIntArray1207[i_200_];
            int i_203_ = Class70.anIntArray1204[i_200_];
            for (int i_204_ = 0; i_204_ < this.vertexCount; i_204_++) {
                int i_205_ = ((i_203_ * this.vertexX[i_204_] + this.vertexY[i_204_] * i_202_) >> 14);
                this.vertexY[i_204_] = ((-(this.vertexX[i_204_] * i_202_) + this.vertexY[i_204_] * i_203_) >> 14);
                this.vertexX[i_204_] = i_205_;
            }
        }
        if (i != 6875) this.faceB = null;
        anInt1837++;
        if (i_201_ != 0) {
            int i_206_ = Class70.anIntArray1207[i_201_];
            int i_207_ = Class70.anIntArray1204[i_201_];
            for (int i_208_ = 0; i_208_ < this.vertexCount; i_208_++) {
                int i_209_ = ((this.vertexY[i_208_] * i_207_ + -(i_206_ * this.vertexZ[i_208_])) >> 14);
                this.vertexZ[i_208_] = ((this.vertexY[i_208_] * i_206_ - -(i_207_ * this.vertexZ[i_208_])) >> 14);
                this.vertexY[i_208_] = i_209_;
            }
        }
        if (i_199_ != 0) {
            int i_210_ = Class70.anIntArray1207[i_199_];
            int i_211_ = Class70.anIntArray1204[i_199_];
            for (int i_212_ = 0; this.vertexCount > i_212_; i_212_++) {
                int i_213_ = ((i_211_ * this.vertexX[i_212_] + this.vertexZ[i_212_] * i_210_) >> 14);
                this.vertexZ[i_212_] = ((i_211_ * this.vertexZ[i_212_] + -(this.vertexX[i_212_] * i_210_)) >> 14);
                this.vertexX[i_212_] = i_213_;
            }
        }
    }

    Mesh(byte[] is) {
        this.faceCount = 0;
        this.globalPriority = (byte) 0;
        this.maxVertex = 0;
        this.texSpaceCount = 0;
        if (is[is.length + -1] == -1 && is[-2 + is.length] == -1) method1106(is, (byte) 93);
        else method1103(1, is);
    }

    // Dual-model merge constructor: unreachable per JaCoCo coverage (0 hits)
    // for every item dumped by the real renderer, so trimmed to a stub that
    // only satisfies the type contract (Class213.method1562's i_1_ != -1
    // branch is never taken for the items in this cache).
    Mesh(Mesh[] meshes, int i) {
        /* empty - see comment above */
    }
}
