package world.gregs.voidps.tools.icon

import java.util.*

/* Class124 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Mesh {
    var texSpaceScaleY: IntArray? = null
    var faceCount: Int = 0
    var texSpaceCount: Int = 0
    var faceTexSpace: ByteArray? = null
    var maxVertex: Int = 0
    var faceTexture: ShortArray? = null
    var texMappingType: ByteArray? = null
    var faceLabel: IntArray? = null
    var texSpaceDefC: ShortArray? = null
    var texSpaceDefA: ShortArray? = null
    var version: Int = 12
    var billboards: Array<MeshBillboard?>? = null
    var texRotation: ByteArray? = null
    var faceAlpha: ByteArray? = null
    var faceB: ShortArray? = null
    var vertexCount: Int = 0
    var facePriority: ByteArray? = null
    var vertexX: IntArray? = null
    var aShortArray1842: ShortArray? = null
    var shadingType: ByteArray? = null
    var texSpaceScaleZ: IntArray? = null
    var emitters: Array<ModelParticleEmitter?>? = null
    var vertexY: IntArray? = null
    var texSpaceDefB: ShortArray? = null
    var globalPriority: Byte = 0
    var vertexZ: IntArray? = null
    var texDirection: ByteArray? = null
    var faceC: ShortArray? = null
    var aShortArray1856: ShortArray? = null
    var texOffsetY: IntArray? = null
    var texSpaceScaleX: IntArray? = null
    var faceColour: ShortArray? = null
    var faceA: ShortArray? = null
    var texOffsetZ: IntArray? = null
    var effectors: Array<ModelParticleEffector?>? = null
    var texOffsetX: IntArray? = null
    var vertexLabel: IntArray? = null
    fun upscale(i: Int, i_0_: Int) {
        anInt1819++
        var i_1_ = 0
        while (this.vertexCount > i_1_) {
            this.vertexX!![i_1_] = this.vertexX!![i_1_] shl i
            this.vertexY!![i_1_] = this.vertexY!![i_1_] shl i
            this.vertexZ!![i_1_] = this.vertexZ!![i_1_] shl i
            i_1_++
        }
        if (i_0_ <= 39) method1107(40, -7, -80, -24)
        if (this.texSpaceCount > 0 && this.texSpaceScaleX != null) {
            for (i_2_ in this.texSpaceScaleX!!.indices) {
                this.texSpaceScaleX!![i_2_] = this.texSpaceScaleX!![i_2_] shl i
                this.texSpaceScaleY!![i_2_] = this.texSpaceScaleY!![i_2_] shl i
                if (this.texMappingType!![i_2_].toInt() != 1) this.texSpaceScaleZ!![i_2_] = this.texSpaceScaleZ!![i_2_] shl i
            }
        }
    }

    fun getBillboardGroups(i: Byte): Array<IntArray?>? {
        anInt1854++
        val `is` = IntArray(256)
        var i_3_ = 0
        var i_4_ = 0
        while (this.billboards!!.size > i_4_) {
            val i_5_ = (this.billboards!![i_4_]!!.anInt2156)
            if (i_5_ >= 0) {
                `is`[i_5_]++
                if (i_3_ < i_5_) i_3_ = i_5_
            }
            i_4_++
        }
        val is_6_ = arrayOfNulls<IntArray>(1 + i_3_)
        for (i_7_ in 0..i_3_) {
            is_6_[i_7_] = IntArray(`is`[i_7_])
            `is`[i_7_] = 0
        }
        if (i > -68) return null
        for (i_8_ in this.billboards!!.indices) {
            val i_9_ = (this.billboards!![i_8_]!!.anInt2156)
            if (i_9_ >= 0) is_6_[i_9_]!![`is`[i_9_]++] = i_8_
        }
        return is_6_
    }

    fun getFaceLabels(i: Byte): Array<IntArray?> {
        anInt1864++
        val `is` = IntArray(256)
        var i_10_ = 0
        for (i_11_ in 0..<this.faceCount) {
            val i_12_ = this.faceLabel!![i_11_]
            if (i_12_ >= 0) {
                if (i_10_ < i_12_) i_10_ = i_12_
                `is`[i_12_]++
            }
        }
        val is_13_ = arrayOfNulls<IntArray>(i_10_ - -1)
        if (i <= 13) method1102(-65, (-8).toByte(), 94.toByte(), (-89).toShort(), 6.toByte(), (-117).toShort(), 21, 21.toByte(), -31)
        var i_14_ = 0
        while (i_10_ >= i_14_) {
            is_13_[i_14_] = IntArray(`is`[i_14_])
            `is`[i_14_] = 0
            i_14_++
        }
        for (i_15_ in 0..<this.faceCount) {
            val i_16_ = this.faceLabel!![i_15_]
            if (i_16_ >= 0) is_13_[i_16_]!![`is`[i_16_]++] = i_15_
        }
        return is_13_
    }

    fun retexture(i: Short, i_17_: Int, i_18_: Short) {
        anInt1869++
        if (this.faceTexture != null) {
            var i_19_ = i_17_
            while (this.faceCount > i_19_) {
                if (i == this.faceTexture!![i_19_]) this.faceTexture!![i_19_] = i_18_
                i_19_++
            }
        }
    }

    fun recolour(i: Short, i_23_: Byte, i_24_: Short) {
        if (i_23_.toInt() == 126) {
            for (i_25_ in 0..<this.faceCount) {
                if (i == this.faceColour!![i_25_]) this.faceColour!![i_25_] = i_24_
            }
            anInt1826++
        }
    }

    private fun method1106(`is`: ByteArray, i_1234: Byte) {
        anInt1828++
        val packet1 = Packet(`is`)
        val packet2 = Packet(`is`)
        val packet3 = Packet(`is`)
        val packet4 = Packet(`is`)
        val packet5 = Packet(`is`)
        val packet6 = Packet(`is`)
        val packet7 = Packet(`is`)
        packet1.pos = -23 + `is`.size
        this.vertexCount = packet1.readUnsignedShort(842397944)
        this.faceCount = packet1.readUnsignedShort(842397944)
        this.texSpaceCount = packet1.readUnsignedByte(255)
        val globalFlags = packet1.readUnsignedByte(255)
        val hasFlatShading = (0x1 and globalFlags) == 1
        val hasParticleEffects = (globalFlags and 0x2) == 2
        val hasBillboards = (globalFlags and 0x4) == 4
        val hasVersion = (globalFlags and 0x8) == 8
        if (hasVersion) {
            packet1.pos -= 7
            this.version = packet1.readUnsignedByte(255)
            packet1.pos += 6
        }
        val priorityFlag = packet1.readUnsignedByte(255)
        val faceAlphaFlag = packet1.readUnsignedByte(255)
        val faceGroupFlag = packet1.readUnsignedByte(255)
        val faceTextureFlag = packet1.readUnsignedByte(255)
        val vertexLabelFlag = packet1.readUnsignedByte(255)
        val vertexLengthX = packet1.readUnsignedShort(842397944)
        val vertexLengthY = packet1.readUnsignedShort(842397944)
        val vertexLengthZ = packet1.readUnsignedShort(842397944)
        val faceDataSize = packet1.readUnsignedShort(842397944)
        val texSpaceSize = packet1.readUnsignedShort(842397944)
        var planarMappingCount = 0
        var complexMappingCount = 0
        var cubeMappingCount = 0
        if (this.texSpaceCount > 0) {
            packet1.pos = 0
            this.texMappingType = ByteArray(this.texSpaceCount)
            var i_141_ = 0
            while ((i_141_ < this.texSpaceCount)) {
                val type = (packet1.readByte(-124).also { this.texMappingType!![i_141_] = it })
                if (type >= 1 && type <= 3) complexMappingCount++
                if (type.toInt() == 2) cubeMappingCount++
                if (type.toInt() == 0) planarMappingCount++
                i_141_++
            }
        }
        var ptr = this.texSpaceCount
        val vertexFlagsPtr = ptr
        ptr += this.vertexCount
        val smoothingPtr = ptr
        if (hasFlatShading) ptr += this.faceCount
        val faceTypePtr = ptr
        ptr += this.faceCount
        val facePriPtr = ptr
        if (priorityFlag == 255) ptr += this.faceCount
        val faceGroupPtr = ptr
        if (faceGroupFlag == 1) ptr += this.faceCount
        val vertexLabelPtr = ptr
        if (vertexLabelFlag == 1) ptr += this.vertexCount
        val i_faceAlphaPtr50_ = ptr
        if (faceAlphaFlag == 1) ptr += this.faceCount
        if (i_1234 <= 68) this.maxVertex = 85
        val faceDataSizePtr = ptr
        ptr += faceDataSize
        val faceTextureFlagPtr = ptr
        if (faceTextureFlag == 1) ptr += 2 * this.faceCount
        val texSpaceSizePtr = ptr
        ptr += texSpaceSize
        val i_154_ = ptr
        ptr += 2 * this.faceCount
        val vertexLengthXPtr = ptr
        ptr += vertexLengthX
        val vertexLengthYPtr = ptr
        ptr += vertexLengthY
        val vertexLengthZPtr = ptr
        ptr += vertexLengthZ
        val planarMappingCountPtr = ptr
        ptr += planarMappingCount * 6
        val complexMappingCountPtr = ptr
        ptr += 6 * complexMappingCount
        var texSpaceScaleSize = 6
        if (this.version == 14) texSpaceScaleSize = 7
        else if (this.version >= 15) texSpaceScaleSize = 9
        val texSpaceScalePtr = ptr
        ptr += texSpaceScaleSize * complexMappingCount
        val texSpaceRotationPtr = ptr
        ptr += complexMappingCount
        val texSpaceOrientationPtr = ptr
        ptr += complexMappingCount
        val texSpaceOffsetPtr = ptr
        ptr += 2 * cubeMappingCount + complexMappingCount
        this.faceB = ShortArray(this.faceCount)
        this.faceColour = ShortArray(this.faceCount)
        if (faceGroupFlag == 1) this.faceLabel = IntArray(this.faceCount)
        if (this.texSpaceCount > 0) {
            if (cubeMappingCount > 0) {
                this.texOffsetY = IntArray(cubeMappingCount)
                this.texOffsetZ = IntArray(cubeMappingCount)
            }
            this.texSpaceDefB = ShortArray(this.texSpaceCount)
            this.texSpaceDefA = ShortArray(this.texSpaceCount)
            this.texSpaceDefC = ShortArray(this.texSpaceCount)
            if (complexMappingCount > 0) {
                this.texOffsetX = IntArray(complexMappingCount)
                this.texDirection = ByteArray(complexMappingCount)
                this.texSpaceScaleY = IntArray(complexMappingCount)
                this.texRotation = ByteArray(complexMappingCount)
                this.texSpaceScaleZ = IntArray(complexMappingCount)
                this.texSpaceScaleX = IntArray(complexMappingCount)
            }
        }
        if (hasFlatShading) this.shadingType = ByteArray(this.faceCount)
        if (faceAlphaFlag == 1) this.faceAlpha = ByteArray(this.faceCount)
        if (priorityFlag == 255) this.facePriority = ByteArray(this.faceCount)
        else this.globalPriority = priorityFlag.toByte()
        this.faceC = ShortArray(this.faceCount)
        this.vertexY = IntArray(this.vertexCount)
        this.faceA = ShortArray(this.faceCount)
        if (faceTextureFlag == 1 && this.texSpaceCount > 0) this.faceTexSpace = ByteArray(this.faceCount)
        val i_165_ = ptr
        this.vertexZ = IntArray(this.vertexCount)
        this.vertexX = IntArray(this.vertexCount)
        if (vertexLabelFlag == 1) this.vertexLabel = IntArray(this.vertexCount)
        packet1.pos = vertexFlagsPtr
        if (faceTextureFlag == 1) this.faceTexture = ShortArray(this.faceCount)
        packet2.pos = vertexLengthXPtr
        packet3.pos = vertexLengthYPtr
        packet4.pos = vertexLengthZPtr
        packet5.pos = vertexLabelPtr
        var pvX = 0
        var pvY = 0
        var pvZ = 0
        run {
            var i = 0
            while (this.vertexCount > i) {
                val vertexData = packet1.readUnsignedByte(255)
                var x = 0
                if ((vertexData and 0x1) != 0) x = packet2.method3362(77.toByte())
                var y = 0
                if ((vertexData and 0x2) != 0) y = packet3.method3362(77.toByte())
                var z = 0
                if ((0x4 and vertexData) != 0) z = packet4.method3362(77.toByte())
                this.vertexX!![i] = x + pvX
                this.vertexY!![i] = y + pvY
                this.vertexZ!![i] = pvZ + z
                pvY = this.vertexY!![i]
                pvX = this.vertexX!![i]
                pvZ = this.vertexZ!![i]
                if (vertexLabelFlag == 1) this.vertexLabel!![i] = packet5.readUnsignedByte(255)
                i++
            }
        }
        packet1.pos = i_154_
        packet2.pos = smoothingPtr
        packet3.pos = facePriPtr
        packet4.pos = i_faceAlphaPtr50_
        packet5.pos = faceGroupPtr
        packet6.pos = faceTextureFlagPtr
        packet7.pos = texSpaceSizePtr
        run {
            var i = 0
            while ((this.faceCount > i)) {
                this.faceColour!![i] = packet1.readUnsignedShort(842397944).toShort()
                if (hasFlatShading) this.shadingType!![i] = packet2.readByte(-98)
                if (priorityFlag == 255) this.facePriority!![i] = packet3.readByte(-78)
                if (faceAlphaFlag == 1) this.faceAlpha!![i] = packet4.readByte(-99)
                if (faceGroupFlag == 1) this.faceLabel!![i] = packet5.readUnsignedByte(255)
                if (faceTextureFlag == 1) this.faceTexture!![i] = (packet6.readUnsignedShort(842397944) + -1).toShort()
                if (this.faceTexSpace != null) {
                    if (this.faceTexture!![i].toInt() == -1) this.faceTexSpace!![i] = (-1).toByte()
                    else this.faceTexSpace!![i] = (-1 + packet7.readUnsignedByte(255)).toByte()
                }
                i++
            }
        }
        packet1.pos = faceDataSizePtr
        this.maxVertex = -1
        packet2.pos = faceTypePtr
        var faceA: Short = 0
        var faceB: Short = 0
        var faceC: Short = 0
        var facePriority = 0
        run {
            var i = 0
            while ((i < this.faceCount)) {
                val type = packet2.readUnsignedByte(255)
                if (type == 1) {
                    faceA = (packet1.method3362(77.toByte()) + facePriority).toShort()
                    facePriority = faceA.toInt()
                    faceB = (packet1.method3362(77.toByte()) + facePriority).toShort()
                    facePriority = faceB.toInt()
                    faceC = (facePriority + packet1.method3362(77.toByte())).toShort()
                    facePriority = faceC.toInt()
                    this.faceA!![i] = faceA
                    this.faceB!![i] = faceB
                    this.faceC!![i] = faceC
                    if (faceA > this.maxVertex) this.maxVertex = faceA.toInt()
                    if (this.maxVertex < faceB) this.maxVertex = faceB.toInt()
                    if (faceC > this.maxVertex) this.maxVertex = faceC.toInt()
                }
                if (type == 2) {
                    faceB = faceC
                    faceC = (packet1.method3362(77.toByte()) + facePriority).toShort()
                    this.faceA!![i] = faceA
                    facePriority = faceC.toInt()
                    this.faceB!![i] = faceB
                    this.faceC!![i] = faceC
                    if (this.maxVertex < faceC) this.maxVertex = faceC.toInt()
                }
                if (type == 3) {
                    faceA = faceC
                    faceC = (packet1.method3362(77.toByte()) + facePriority).toShort()
                    facePriority = faceC.toInt()
                    this.faceA!![i] = faceA
                    this.faceB!![i] = faceB
                    this.faceC!![i] = faceC
                    if (this.maxVertex < faceC) this.maxVertex = faceC.toInt()
                }
                if (type == 4) {
                    val i_181_ = faceA
                    faceA = faceB
                    faceC = (facePriority + packet1.method3362(77.toByte())).toShort()
                    faceB = i_181_
                    this.faceA!![i] = faceA
                    facePriority = faceC.toInt()
                    this.faceB!![i] = faceB
                    this.faceC!![i] = faceC
                    if (this.maxVertex < faceC) this.maxVertex = faceC.toInt()
                }
                i++
            }
        }
        packet1.pos = planarMappingCountPtr
        this.maxVertex++
        packet2.pos = complexMappingCountPtr
        packet3.pos = texSpaceScalePtr
        packet4.pos = texSpaceRotationPtr
        packet5.pos = texSpaceOrientationPtr
        packet6.pos = texSpaceOffsetPtr
        var i = 0
        while (this.texSpaceCount > i) {
            val type = this.texMappingType!![i].toInt() and 0xff
            if (type == 0) {
                this.texSpaceDefA!![i] = packet1.readUnsignedShort(842397944).toShort()
                this.texSpaceDefB!![i] = packet1.readUnsignedShort(842397944).toShort()
                this.texSpaceDefC!![i] = packet1.readUnsignedShort(842397944).toShort()
            }
            if (type == 1) {
                this.texSpaceDefA!![i] = packet2.readUnsignedShort(842397944).toShort()
                this.texSpaceDefB!![i] = packet2.readUnsignedShort(842397944).toShort()
                this.texSpaceDefC!![i] = packet2.readUnsignedShort(842397944).toShort()
                if (this.version >= 15) {
                    this.texSpaceScaleX!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleY!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleZ!![i] = packet3.readMedium(-1)
                } else {
                    this.texSpaceScaleX!![i] = packet3.readUnsignedShort(842397944)
                    if (this.version >= 14) this.texSpaceScaleY!![i] = packet3.readMedium(-1)
                    else this.texSpaceScaleY!![i] = packet3.readUnsignedShort(842397944)
                    this.texSpaceScaleZ!![i] = packet3.readUnsignedShort(842397944)
                }
                this.texRotation!![i] = packet4.readByte(-86)
                this.texDirection!![i] = packet5.readByte(-116)
                this.texOffsetX!![i] = packet6.readByte(-79).toInt()
            }
            if (type == 2) {
                this.texSpaceDefA!![i] = packet2.readUnsignedShort(842397944).toShort()
                this.texSpaceDefB!![i] = packet2.readUnsignedShort(842397944).toShort()
                this.texSpaceDefC!![i] = packet2.readUnsignedShort(842397944).toShort()
                if (this.version >= 15) {
                    this.texSpaceScaleX!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleY!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleZ!![i] = packet3.readMedium(-1)
                } else {
                    this.texSpaceScaleX!![i] = packet3.readUnsignedShort(842397944)
                    if (this.version < 14) this.texSpaceScaleY!![i] = packet3.readUnsignedShort(842397944)
                    else this.texSpaceScaleY!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleZ!![i] = packet3.readUnsignedShort(842397944)
                }
                this.texRotation!![i] = packet4.readByte(-97)
                this.texDirection!![i] = packet5.readByte(-100)
                this.texOffsetX!![i] = packet6.readByte(-124).toInt()
                this.texOffsetY!![i] = packet6.readByte(-112).toInt()
                this.texOffsetZ!![i] = packet6.readByte(-114).toInt()
            }
            if (type == 3) {
                this.texSpaceDefA!![i] = packet2.readUnsignedShort(842397944).toShort()
                this.texSpaceDefB!![i] = packet2.readUnsignedShort(842397944).toShort()
                this.texSpaceDefC!![i] = packet2.readUnsignedShort(842397944).toShort()
                if (this.version < 15) {
                    this.texSpaceScaleX!![i] = packet3.readUnsignedShort(842397944)
                    if (this.version < 14) this.texSpaceScaleY!![i] = packet3.readUnsignedShort(842397944)
                    else this.texSpaceScaleY!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleZ!![i] = packet3.readUnsignedShort(842397944)
                } else {
                    this.texSpaceScaleX!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleY!![i] = packet3.readMedium(-1)
                    this.texSpaceScaleZ!![i] = packet3.readMedium(-1)
                }
                this.texRotation!![i] = packet4.readByte(-104)
                this.texDirection!![i] = packet5.readByte(-127)
                this.texOffsetX!![i] = packet6.readByte(-109).toInt()
            }
            i++
        }
        packet1.pos = i_165_
        if (hasParticleEffects) {
            val emitterCount = packet1.readUnsignedByte(255)
            if (emitterCount > 0) {
                this.emitters = arrayOfNulls<ModelParticleEmitter>(emitterCount)
                var i = 0
                while (emitterCount > i) {
                    val type = packet1.readUnsignedShort(842397944)
                    val face = packet1.readUnsignedShort(842397944)
                    val priority: Byte
                    if (priorityFlag != 255) priority = priorityFlag.toByte()
                    else priority = this.facePriority!![face]
                    this.emitters!![i] = (ModelParticleEmitter(type, this.faceA!![face].toInt(), this.faceB!![face].toInt(), this.faceC!![face].toInt(), priority))
                    i++
                }
            }
            val effectorCount = packet1.readUnsignedByte(255)
            if (effectorCount > 0) {
                this.effectors = arrayOfNulls<ModelParticleEffector>(effectorCount)
                var i = 0
                while (effectorCount > i) {
                    val type = packet1.readUnsignedShort(842397944)
                    val vertex = packet1.readUnsignedShort(842397944)
                    this.effectors!![i] = ModelParticleEffector(type, vertex)
                    i++
                }
            }
        }
        if (hasBillboards) {
            val billboardCount = packet1.readUnsignedByte(255)
            if (billboardCount > 0) {
                this.billboards = arrayOfNulls<MeshBillboard>(billboardCount)
                var i = 0
                while (billboardCount > i) {
                    val type = packet1.readUnsignedShort(842397944)
                    val face = packet1.readUnsignedShort(842397944)
                    val group = packet1.readUnsignedByte(255)
                    val priority = packet1.readByte(-127)
                    this.billboards!![i] = MeshBillboard(type, face, group, priority.toInt())
                    i++
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
    fun getVertexLabels(bool: Boolean, i: Int): Array<IntArray?> {
        anInt1845++
        val `is` = IntArray(256)
        var i_31_ = 0
        val i_32_ = (if (!bool) this.maxVertex else this.vertexCount)
        for (i_33_ in 0..<i_32_) {
            val i_34_ = this.vertexLabel!![i_33_]
            if (i_34_ >= 0) {
                `is`[i_34_]++
                if (i_31_ < i_34_) i_31_ = i_34_
            }
        }
        val is_35_ = arrayOfNulls<IntArray>(1 + i_31_)
        for (i_36_ in 0..i_31_) {
            is_35_[i_36_] = IntArray(`is`[i_36_])
            `is`[i_36_] = 0
        }
        var i_37_ = 0
        while (i_32_ > i_37_) {
            val i_38_ = this.vertexLabel!![i_37_]
            if (i_38_ >= 0) is_35_[i_38_]!![`is`[i_38_]++] = i_37_
            i_37_++
        }
        if (i > -14) recolour(9.toShort(), 80.toByte(), (-118).toShort())
        return is_35_
    }

    private fun method1102(i: Int, i_44_: Byte, i_45_: Byte, i_46_: Short, i_47_: Byte, i_48_: Short, i_49_: Int, i_50_: Byte, i_51_: Int): Int {
        this.faceA!![this.faceCount] = i.toShort()
        anInt1860++
        this.faceB!![this.faceCount] = i_49_.toShort()
        this.faceC!![this.faceCount] = i_51_.toShort()
        if (i_47_ < 30) return -92
        this.shadingType!![this.faceCount] = i_44_
        this.faceTexSpace!![this.faceCount] = i_45_
        this.faceColour!![this.faceCount] = i_48_
        this.faceAlpha!![this.faceCount] = i_50_
        this.faceTexture!![this.faceCount] = i_46_
        return this.faceCount++
    }

    private fun method1103(i: Int, `is`: ByteArray) {
        anInt1831++
        var bool = false
        var bool_52_ = false
        val packet = Packet(`is`)
        val packet_53_ = Packet(`is`)
        val packet_54_ = Packet(`is`)
        val packet_55_ = Packet(`is`)
        val packet_56_ = Packet(`is`)
        packet.pos = -18 + `is`.size
        this.vertexCount = packet.readUnsignedShort(i xor 0x3235f8f9)
        this.faceCount = packet.readUnsignedShort(842397944)
        this.texSpaceCount = packet.readUnsignedByte(255)
        val i_57_ = packet.readUnsignedByte(255)
        val i_58_ = packet.readUnsignedByte(255)
        val i_59_ = packet.readUnsignedByte(255)
        val i_60_ = packet.readUnsignedByte(255)
        val i_61_ = packet.readUnsignedByte(255)
        val i_62_ = packet.readUnsignedShort(842397944)
        val i_63_ = packet.readUnsignedShort(842397944)
        val i_64_ = packet.readUnsignedShort(842397944)
        val i_65_ = packet.readUnsignedShort(842397944)
        var i_66_ = 0
        val i_67_ = i_66_
        i_66_ += this.vertexCount
        val i_68_ = i_66_
        i_66_ += this.faceCount
        val i_69_ = i_66_
        if (i_58_ == 255) i_66_ += this.faceCount
        val i_70_ = i_66_
        if (i_60_ == 1) i_66_ += this.faceCount
        val i_71_ = i_66_
        if (i_57_ == 1) i_66_ += this.faceCount
        val i_72_ = i_66_
        if (i_61_ == 1) i_66_ += this.vertexCount
        val i_73_ = i_66_
        if (i == i_59_) i_66_ += this.faceCount
        val i_74_ = i_66_
        i_66_ += i_65_
        val i_75_ = i_66_
        i_66_ += this.faceCount * 2
        val i_76_ = i_66_
        i_66_ += this.texSpaceCount * 6
        val i_77_ = i_66_
        i_66_ += i_62_
        val i_78_ = i_66_
        i_66_ += i_63_
        val i_79_ = i_66_
        this.faceColour = ShortArray(this.faceCount)
        if (i_58_ == 255) this.facePriority = ByteArray(this.faceCount)
        else this.globalPriority = i_58_.toByte()
        if (i_61_ == 1) this.vertexLabel = IntArray(this.vertexCount)
        if (i_59_ == 1) this.faceAlpha = ByteArray(this.faceCount)
        this.faceA = ShortArray(this.faceCount)
        if (i_57_ == 1) {
            this.faceTexture = ShortArray(this.faceCount)
            this.faceTexSpace = ByteArray(this.faceCount)
            this.shadingType = ByteArray(this.faceCount)
        }
        this.vertexY = IntArray(this.vertexCount)
        if (i_60_ == 1) this.faceLabel = IntArray(this.faceCount)
        this.faceC = ShortArray(this.faceCount)
        if (this.texSpaceCount > 0) {
            this.texMappingType = ByteArray(this.texSpaceCount)
            this.texSpaceDefA = ShortArray(this.texSpaceCount)
            this.texSpaceDefB = ShortArray(this.texSpaceCount)
            this.texSpaceDefC = ShortArray(this.texSpaceCount)
        }
        this.faceB = ShortArray(this.faceCount)
        this.vertexZ = IntArray(this.vertexCount)
        this.vertexX = IntArray(this.vertexCount)
        packet.pos = i_67_
        i_66_ += i_64_
        packet_53_.pos = i_77_
        packet_54_.pos = i_78_
        packet_55_.pos = i_79_
        packet_56_.pos = i_72_
        var i_80_ = 0
        var i_81_ = 0
        var i_82_ = 0
        for (i_83_ in 0..<this.vertexCount) {
            val i_84_ = packet.readUnsignedByte(255)
            var i_85_ = 0
            if ((i_84_ and 0x1) != 0) i_85_ = packet_53_.method3362(77.toByte())
            var i_86_ = 0
            if ((i_84_ and 0x2) != 0) i_86_ = packet_54_.method3362(77.toByte())
            var i_87_ = 0
            if ((0x4 and i_84_) != 0) i_87_ = packet_55_.method3362(77.toByte())
            this.vertexX!![i_83_] = i_85_ + i_80_
            this.vertexY!![i_83_] = i_81_ - -i_86_
            this.vertexZ!![i_83_] = i_82_ + i_87_
            i_82_ = this.vertexZ!![i_83_]
            i_81_ = this.vertexY!![i_83_]
            i_80_ = this.vertexX!![i_83_]
            if (i_61_ == 1) this.vertexLabel!![i_83_] = packet_56_.readUnsignedByte(Class348_Sub21.method2955(i, 254))
        }
        packet.pos = i_75_
        packet_53_.pos = i_71_
        packet_54_.pos = i_69_
        packet_55_.pos = i_73_
        packet_56_.pos = i_70_
        for (i_88_ in 0..<this.faceCount) {
            this.faceColour!![i_88_] = packet.readUnsignedShort(842397944).toShort()
            if (i_57_ == 1) {
                val i_89_ = packet_53_.readUnsignedByte(255)
                if ((0x1 and i_89_) == 1) {
                    this.shadingType!![i_88_] = 1.toByte()
                    bool = true
                } else this.shadingType!![i_88_] = 0.toByte()
                if ((i_89_ and 0x2) == 2) {
                    this.faceTexSpace!![i_88_] = (i_89_ shr 2).toByte()
                    this.faceTexture!![i_88_] = this.faceColour!![i_88_]
                    this.faceColour!![i_88_] = 127.toShort()
                    if (this.faceTexture!![i_88_].toInt() != -1) bool_52_ = true
                } else {
                    this.faceTexSpace!![i_88_] = (-1).toByte()
                    this.faceTexture!![i_88_] = (-1).toShort()
                }
            }
            if (i_58_ == 255) this.facePriority!![i_88_] = packet_54_.readByte(-108)
            if (i_59_ == 1) this.faceAlpha!![i_88_] = packet_55_.readByte(Class348_Sub21.method2955(i, -120))
            if (i_60_ == 1) this.faceLabel!![i_88_] = packet_56_.readUnsignedByte(255)
        }
        packet.pos = i_74_
        this.maxVertex = -1
        packet_53_.pos = i_68_
        var i_90_: Short = 0
        var i_91_: Short = 0
        var i_92_: Short = 0
        var i_93_ = 0
        for (i_94_ in 0..<this.faceCount) {
            val i_95_ = packet_53_.readUnsignedByte(255)
            if (i_95_ == 1) {
                i_90_ = (i_93_ + packet.method3362(77.toByte())).toShort()
                i_93_ = i_90_.toInt()
                i_91_ = (packet.method3362(77.toByte()) + i_93_).toShort()
                i_93_ = i_91_.toInt()
                i_92_ = (i_93_ + packet.method3362(77.toByte())).toShort()
                this.faceA!![i_94_] = i_90_
                i_93_ = i_92_.toInt()
                this.faceB!![i_94_] = i_91_
                this.faceC!![i_94_] = i_92_
                if (i_90_ > this.maxVertex) this.maxVertex = i_90_.toInt()
                if (this.maxVertex < i_91_) this.maxVertex = i_91_.toInt()
                if (this.maxVertex < i_92_) this.maxVertex = i_92_.toInt()
            }
            if (i_95_ == 2) {
                i_91_ = i_92_
                i_92_ = (packet.method3362(77.toByte()) + i_93_).toShort()
                this.faceA!![i_94_] = i_90_
                i_93_ = i_92_.toInt()
                this.faceB!![i_94_] = i_91_
                this.faceC!![i_94_] = i_92_
                if (i_92_ > this.maxVertex) this.maxVertex = i_92_.toInt()
            }
            if (i_95_ == 3) {
                i_90_ = i_92_
                i_92_ = (packet.method3362(77.toByte()) + i_93_).toShort()
                i_93_ = i_92_.toInt()
                this.faceA!![i_94_] = i_90_
                this.faceB!![i_94_] = i_91_
                this.faceC!![i_94_] = i_92_
                if (this.maxVertex < i_92_) this.maxVertex = i_92_.toInt()
            }
            if (i_95_ == 4) {
                val i_96_ = i_90_
                i_90_ = i_91_
                i_92_ = (packet.method3362(77.toByte()) + i_93_).toShort()
                i_91_ = i_96_
                i_93_ = i_92_.toInt()
                this.faceA!![i_94_] = i_90_
                this.faceB!![i_94_] = i_91_
                this.faceC!![i_94_] = i_92_
                if (this.maxVertex < i_92_) this.maxVertex = i_92_.toInt()
            }
        }
        this.maxVertex++
        packet.pos = i_76_
        for (i_97_ in 0..<this.texSpaceCount) {
            this.texMappingType!![i_97_] = 0.toByte()
            this.texSpaceDefA!![i_97_] = packet.readUnsignedShort(842397944).toShort()
            this.texSpaceDefB!![i_97_] = packet.readUnsignedShort(842397944).toShort()
            this.texSpaceDefC!![i_97_] = packet.readUnsignedShort(842397944).toShort()
        }
        if (this.faceTexSpace != null) {
            var bool_98_ = false
            for (i_99_ in 0..<this.faceCount) {
                val i_100_ = this.faceTexSpace!![i_99_].toInt() and 0xff
                if (i_100_ != 255) {
                    if (((0xffff and this.texSpaceDefA!![i_100_].toInt()) != this.faceA!![i_99_].toInt()) || (this.faceB!![i_99_].toInt() != (0xffff and this.texSpaceDefB!![i_100_].toInt())) || ((0xffff and this.texSpaceDefC!![i_100_].toInt()) != this.faceC!![i_99_].toInt())) bool_98_ = true
                    else this.faceTexSpace!![i_99_] = (-1).toByte()
                }
            }
            if (!bool_98_) this.faceTexSpace = null
        }
        if (!bool) this.shadingType = null
        if (!bool_52_) this.faceTexture = null
    }

    fun method1107(i: Int, i_199_: Int, i_200_: Int, i_201_: Int) {
        if (i_200_ != 0) {
            val i_202_ = Class70.anIntArray1207[i_200_]
            val i_203_ = Class70.anIntArray1204[i_200_]
            for (i_204_ in 0..<this.vertexCount) {
                val i_205_ = ((i_203_ * this.vertexX!![i_204_] + this.vertexY!![i_204_] * i_202_) shr 14)
                this.vertexY!![i_204_] = ((-(this.vertexX!![i_204_] * i_202_) + this.vertexY!![i_204_] * i_203_) shr 14)
                this.vertexX!![i_204_] = i_205_
            }
        }
        if (i != 6875) this.faceB = null
        anInt1837++
        if (i_201_ != 0) {
            val i_206_ = Class70.anIntArray1207[i_201_]
            val i_207_ = Class70.anIntArray1204[i_201_]
            for (i_208_ in 0..<this.vertexCount) {
                val i_209_ = ((this.vertexY!![i_208_] * i_207_ + -(i_206_ * this.vertexZ!![i_208_])) shr 14)
                this.vertexZ!![i_208_] = ((this.vertexY!![i_208_] * i_206_ - -(i_207_ * this.vertexZ!![i_208_])) shr 14)
                this.vertexY!![i_208_] = i_209_
            }
        }
        if (i_199_ != 0) {
            val i_210_ = Class70.anIntArray1207[i_199_]
            val i_211_ = Class70.anIntArray1204[i_199_]
            var i_212_ = 0
            while (this.vertexCount > i_212_) {
                val i_213_ = ((i_211_ * this.vertexX!![i_212_] + this.vertexZ!![i_212_] * i_210_) shr 14)
                this.vertexZ!![i_212_] = ((i_211_ * this.vertexZ!![i_212_] + -(this.vertexX!![i_212_] * i_210_)) shr 14)
                this.vertexX!![i_212_] = i_213_
                i_212_++
            }
        }
    }

    constructor(`is`: ByteArray) {
        this.faceCount = 0
        this.globalPriority = 0.toByte()
        this.maxVertex = 0
        this.texSpaceCount = 0
        if (`is`[`is`.size + -1].toInt() == -1 && `is`[-2 + `is`.size].toInt() == -1) method1106(`is`, 93.toByte())
        else method1103(1, `is`)
    }

    // Dual-model merge constructor: unreachable per JaCoCo coverage (0 hits)
    // for every item dumped by the real renderer, so trimmed to a stub that
    // only satisfies the type contract (Class213.method1562's i_1_ != -1
    // branch is never taken for the items in this cache).
    constructor(meshes: Array<Mesh?>?, i: Int)

    companion object {
        var anInt1819: Int = 0
        var anInt1826: Int = 0
        var anInt1828: Int = 0
        var aJs5_1848: Js5? = null
        var anInt1860: Int = 0
        var anInt1861: Int = 0
        var anInt1869: Int = 0
        var anInt1831: Int = 0
        var anInt1837: Int = 0
        var anInt1854: Int = 0
        var anInt1864: Int = 0
        var anInt1845: Int = 0

        fun method1096(i: Int) {
            if (i >= 88) aJs5_1848 = null
        }

        fun method1097(i: Byte, i_20_: Int, random: Random): Int {
            anInt1861++
            require(i_20_ > 0)
            if (Class192.method1436(-19, i_20_)) return (i_20_.toLong() * (0xffffffffL and random.nextInt().toLong()) shr 32).toInt()
            val i_21_ = -2147483648 + -(4294967296L % i_20_.toLong()).toInt()
            if (i < 78) aJs5_1848 = null
            var i_22_: Int
            do i_22_ = random.nextInt() while (i_22_ >= i_21_)
            return JavaBillboardAttributes.Companion.method3452(i_22_, (-15).toByte(), i_20_)
        }
    }
}
