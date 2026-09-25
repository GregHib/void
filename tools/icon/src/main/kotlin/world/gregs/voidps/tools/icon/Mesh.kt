package world.gregs.voidps.tools.icon

import java.util.*
import kotlin.math.cos
import kotlin.math.sin

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
    var shadingType: ByteArray? = null
    var texSpaceScaleZ: IntArray? = null
    var emitters: Array<ModelParticleEmitter?>? = null
    var vertexY: IntArray? = null
    var texSpaceDefB: ShortArray? = null
    var globalPriority: Byte = 0
    var vertexZ: IntArray? = null
    var texDirection: ByteArray? = null
    var faceC: ShortArray? = null
    var texOffsetY: IntArray? = null
    var texSpaceScaleX: IntArray? = null
    var faceColour: ShortArray? = null
    var faceA: ShortArray? = null
    var texOffsetZ: IntArray? = null
    var effectors: Array<ModelParticleEffector?>? = null
    var texOffsetX: IntArray? = null
    var vertexLabel: IntArray? = null
    fun upscale(i: Int) {
        var i_1_ = 0
        while (this.vertexCount > i_1_) {
            this.vertexX!![i_1_] = this.vertexX!![i_1_] shl i
            this.vertexY!![i_1_] = this.vertexY!![i_1_] shl i
            this.vertexZ!![i_1_] = this.vertexZ!![i_1_] shl i
            i_1_++
        }
        if (this.texSpaceCount > 0 && this.texSpaceScaleX != null) {
            for (i_2_ in this.texSpaceScaleX!!.indices) {
                this.texSpaceScaleX!![i_2_] = this.texSpaceScaleX!![i_2_] shl i
                this.texSpaceScaleY!![i_2_] = this.texSpaceScaleY!![i_2_] shl i
                if (this.texMappingType!![i_2_].toInt() != 1) this.texSpaceScaleZ!![i_2_] = this.texSpaceScaleZ!![i_2_] shl i
            }
        }
    }

    fun getBillboardGroups(): Array<IntArray?>? {
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
        for (i_8_ in this.billboards!!.indices) {
            val i_9_ = (this.billboards!![i_8_]!!.anInt2156)
            if (i_9_ >= 0) is_6_[i_9_]!![`is`[i_9_]++] = i_8_
        }
        return is_6_
    }

    fun getFaceLabels(): Array<IntArray?> {
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

    fun retexture(i: Short, i_18_: Short) {
        if (this.faceTexture != null) {
            for (i_19_ in 0..<this.faceCount) {
                if (i == this.faceTexture!![i_19_]) this.faceTexture!![i_19_] = i_18_
            }
        }
    }

    fun recolour(i: Short, i_24_: Short) {
        for (i_25_ in 0..<this.faceCount) {
            if (i == this.faceColour!![i_25_]) this.faceColour!![i_25_] = i_24_
        }
    }

    private fun method1106(`is`: ByteArray) {
        val packet1 = Packet(`is`)
        val packet2 = Packet(`is`)
        val packet3 = Packet(`is`)
        val packet4 = Packet(`is`)
        val packet5 = Packet(`is`)
        val packet6 = Packet(`is`)
        val packet7 = Packet(`is`)
        packet1.pos = -23 + `is`.size
        this.vertexCount = packet1.readUnsignedShort()
        this.faceCount = packet1.readUnsignedShort()
        this.texSpaceCount = packet1.readUnsignedByte()
        val globalFlags = packet1.readUnsignedByte()
        val hasFlatShading = (0x1 and globalFlags) == 1
        val hasParticleEffects = (globalFlags and 0x2) == 2
        val hasBillboards = (globalFlags and 0x4) == 4
        val hasVersion = (globalFlags and 0x8) == 8
        if (hasVersion) {
            packet1.pos -= 7
            this.version = packet1.readUnsignedByte()
            packet1.pos += 6
        }
        val priorityFlag = packet1.readUnsignedByte()
        val faceAlphaFlag = packet1.readUnsignedByte()
        val faceGroupFlag = packet1.readUnsignedByte()
        val faceTextureFlag = packet1.readUnsignedByte()
        val vertexLabelFlag = packet1.readUnsignedByte()
        val vertexLengthX = packet1.readUnsignedShort()
        val vertexLengthY = packet1.readUnsignedShort()
        val vertexLengthZ = packet1.readUnsignedShort()
        val faceDataSize = packet1.readUnsignedShort()
        val texSpaceSize = packet1.readUnsignedShort()
        var planarMappingCount = 0
        var complexMappingCount = 0
        var cubeMappingCount = 0
        if (this.texSpaceCount > 0) {
            packet1.pos = 0
            this.texMappingType = ByteArray(this.texSpaceCount)
            var i_141_ = 0
            while ((i_141_ < this.texSpaceCount)) {
                val type = (packet1.readByte().also { this.texMappingType!![i_141_] = it })
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
        for (i in 0..<this.vertexCount) {
            val vertexData = packet1.readUnsignedByte()
            var x = 0
            if ((vertexData and 0x1) != 0) x = packet2.method3362()
            var y = 0
            if ((vertexData and 0x2) != 0) y = packet3.method3362()
            var z = 0
            if ((0x4 and vertexData) != 0) z = packet4.method3362()
            this.vertexX!![i] = x + pvX
            this.vertexY!![i] = y + pvY
            this.vertexZ!![i] = pvZ + z
            pvY = this.vertexY!![i]
            pvX = this.vertexX!![i]
            pvZ = this.vertexZ!![i]
            if (vertexLabelFlag == 1) this.vertexLabel!![i] = packet5.readUnsignedByte()
        }
        packet1.pos = i_154_
        packet2.pos = smoothingPtr
        packet3.pos = facePriPtr
        packet4.pos = i_faceAlphaPtr50_
        packet5.pos = faceGroupPtr
        packet6.pos = faceTextureFlagPtr
        packet7.pos = texSpaceSizePtr
        for (i in 0..<this.faceCount) {
            this.faceColour!![i] = packet1.readUnsignedShort().toShort()
            if (hasFlatShading) this.shadingType!![i] = packet2.readByte()
            if (priorityFlag == 255) this.facePriority!![i] = packet3.readByte()
            if (faceAlphaFlag == 1) this.faceAlpha!![i] = packet4.readByte()
            if (faceGroupFlag == 1) this.faceLabel!![i] = packet5.readUnsignedByte()
            if (faceTextureFlag == 1) this.faceTexture!![i] = (packet6.readUnsignedShort() + -1).toShort()
            if (this.faceTexSpace != null) {
                if (this.faceTexture!![i].toInt() == -1) this.faceTexSpace!![i] = (-1).toByte()
                else this.faceTexSpace!![i] = (-1 + packet7.readUnsignedByte()).toByte()
            }
        }
        packet1.pos = faceDataSizePtr
        this.maxVertex = -1
        packet2.pos = faceTypePtr
        var faceA: Short = 0
        var faceB: Short = 0
        var faceC: Short = 0
        var facePriority = 0
        for (i in 0..<this.faceCount) {
            val type = packet2.readUnsignedByte()
            if (type == 1) {
                faceA = (packet1.method3362() + facePriority).toShort()
                facePriority = faceA.toInt()
                faceB = (packet1.method3362() + facePriority).toShort()
                facePriority = faceB.toInt()
                faceC = (facePriority + packet1.method3362()).toShort()
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
                faceC = (packet1.method3362() + facePriority).toShort()
                this.faceA!![i] = faceA
                facePriority = faceC.toInt()
                this.faceB!![i] = faceB
                this.faceC!![i] = faceC
                if (this.maxVertex < faceC) this.maxVertex = faceC.toInt()
            }
            if (type == 3) {
                faceA = faceC
                faceC = (packet1.method3362() + facePriority).toShort()
                facePriority = faceC.toInt()
                this.faceA!![i] = faceA
                this.faceB!![i] = faceB
                this.faceC!![i] = faceC
                if (this.maxVertex < faceC) this.maxVertex = faceC.toInt()
            }
            if (type == 4) {
                val i_181_ = faceA
                faceA = faceB
                faceC = (facePriority + packet1.method3362()).toShort()
                faceB = i_181_
                this.faceA!![i] = faceA
                facePriority = faceC.toInt()
                this.faceB!![i] = faceB
                this.faceC!![i] = faceC
                if (this.maxVertex < faceC) this.maxVertex = faceC.toInt()
            }
        }
        packet1.pos = planarMappingCountPtr
        this.maxVertex++
        packet2.pos = complexMappingCountPtr
        packet3.pos = texSpaceScalePtr
        packet4.pos = texSpaceRotationPtr
        packet5.pos = texSpaceOrientationPtr
        packet6.pos = texSpaceOffsetPtr
        for (i in 0..<this.texSpaceCount) {
            val type = this.texMappingType!![i].toInt() and 0xff
            if (type == 0) {
                this.texSpaceDefA!![i] = packet1.readUnsignedShort().toShort()
                this.texSpaceDefB!![i] = packet1.readUnsignedShort().toShort()
                this.texSpaceDefC!![i] = packet1.readUnsignedShort().toShort()
            }
            if (type == 1) {
                this.texSpaceDefA!![i] = packet2.readUnsignedShort().toShort()
                this.texSpaceDefB!![i] = packet2.readUnsignedShort().toShort()
                this.texSpaceDefC!![i] = packet2.readUnsignedShort().toShort()
                if (this.version >= 15) {
                    this.texSpaceScaleX!![i] = packet3.readMedium()
                    this.texSpaceScaleY!![i] = packet3.readMedium()
                    this.texSpaceScaleZ!![i] = packet3.readMedium()
                } else {
                    this.texSpaceScaleX!![i] = packet3.readUnsignedShort()
                    if (this.version >= 14) this.texSpaceScaleY!![i] = packet3.readMedium()
                    else this.texSpaceScaleY!![i] = packet3.readUnsignedShort()
                    this.texSpaceScaleZ!![i] = packet3.readUnsignedShort()
                }
                this.texRotation!![i] = packet4.readByte()
                this.texDirection!![i] = packet5.readByte()
                this.texOffsetX!![i] = packet6.readByte().toInt()
            }
            if (type == 2) {
                this.texSpaceDefA!![i] = packet2.readUnsignedShort().toShort()
                this.texSpaceDefB!![i] = packet2.readUnsignedShort().toShort()
                this.texSpaceDefC!![i] = packet2.readUnsignedShort().toShort()
                if (this.version >= 15) {
                    this.texSpaceScaleX!![i] = packet3.readMedium()
                    this.texSpaceScaleY!![i] = packet3.readMedium()
                    this.texSpaceScaleZ!![i] = packet3.readMedium()
                } else {
                    this.texSpaceScaleX!![i] = packet3.readUnsignedShort()
                    if (this.version < 14) this.texSpaceScaleY!![i] = packet3.readUnsignedShort()
                    else this.texSpaceScaleY!![i] = packet3.readMedium()
                    this.texSpaceScaleZ!![i] = packet3.readUnsignedShort()
                }
                this.texRotation!![i] = packet4.readByte()
                this.texDirection!![i] = packet5.readByte()
                this.texOffsetX!![i] = packet6.readByte().toInt()
                this.texOffsetY!![i] = packet6.readByte().toInt()
                this.texOffsetZ!![i] = packet6.readByte().toInt()
            }
            if (type == 3) {
                this.texSpaceDefA!![i] = packet2.readUnsignedShort().toShort()
                this.texSpaceDefB!![i] = packet2.readUnsignedShort().toShort()
                this.texSpaceDefC!![i] = packet2.readUnsignedShort().toShort()
                if (this.version < 15) {
                    this.texSpaceScaleX!![i] = packet3.readUnsignedShort()
                    if (this.version < 14) this.texSpaceScaleY!![i] = packet3.readUnsignedShort()
                    else this.texSpaceScaleY!![i] = packet3.readMedium()
                    this.texSpaceScaleZ!![i] = packet3.readUnsignedShort()
                } else {
                    this.texSpaceScaleX!![i] = packet3.readMedium()
                    this.texSpaceScaleY!![i] = packet3.readMedium()
                    this.texSpaceScaleZ!![i] = packet3.readMedium()
                }
                this.texRotation!![i] = packet4.readByte()
                this.texDirection!![i] = packet5.readByte()
                this.texOffsetX!![i] = packet6.readByte().toInt()
            }
        }
        packet1.pos = i_165_
        if (hasParticleEffects) {
            val emitterCount = packet1.readUnsignedByte()
            if (emitterCount > 0) {
                this.emitters = arrayOfNulls<ModelParticleEmitter>(emitterCount)
                var i = 0
                while (emitterCount > i) {
                    val type = packet1.readUnsignedShort()
                    val face = packet1.readUnsignedShort()
                    val priority: Byte
                    if (priorityFlag != 255) priority = priorityFlag.toByte()
                    else priority = this.facePriority!![face]
                    this.emitters!![i] = (ModelParticleEmitter(type, this.faceA!![face].toInt(), this.faceB!![face].toInt(), this.faceC!![face].toInt(), priority))
                    i++
                }
            }
            val effectorCount = packet1.readUnsignedByte()
            if (effectorCount > 0) {
                this.effectors = arrayOfNulls<ModelParticleEffector>(effectorCount)
                var i = 0
                while (effectorCount > i) {
                    val type = packet1.readUnsignedShort()
                    val vertex = packet1.readUnsignedShort()
                    this.effectors!![i] = ModelParticleEffector(type, vertex)
                    i++
                }
            }
        }
        if (hasBillboards) {
            val billboardCount = packet1.readUnsignedByte()
            if (billboardCount > 0) {
                this.billboards = arrayOfNulls<MeshBillboard>(billboardCount)
                var i = 0
                while (billboardCount > i) {
                    val type = packet1.readUnsignedShort()
                    val face = packet1.readUnsignedShort()
                    val group = packet1.readUnsignedByte()
                    val priority = packet1.readByte()
                    this.billboards!![i] = MeshBillboard(type, face, group, priority.toInt())
                    i++
                }
            }
        }
    }

    fun getVertexLabels(): Array<IntArray?> {
        val `is` = IntArray(256)
        var i_31_ = 0
        val i_32_ = this.vertexCount
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
        return is_35_
    }

    private fun method1103(`is`: ByteArray) {
        var bool = false
        var bool_52_ = false
        val packet = Packet(`is`)
        val packet_53_ = Packet(`is`)
        val packet_54_ = Packet(`is`)
        val packet_55_ = Packet(`is`)
        val packet_56_ = Packet(`is`)
        packet.pos = -18 + `is`.size
        this.vertexCount = packet.readUnsignedShort()
        this.faceCount = packet.readUnsignedShort()
        this.texSpaceCount = packet.readUnsignedByte()
        val i_57_ = packet.readUnsignedByte()
        val i_58_ = packet.readUnsignedByte()
        val i_59_ = packet.readUnsignedByte()
        val i_60_ = packet.readUnsignedByte()
        val i_61_ = packet.readUnsignedByte()
        val i_62_ = packet.readUnsignedShort()
        val i_63_ = packet.readUnsignedShort()
        val i_64_ = packet.readUnsignedShort()
        val i_65_ = packet.readUnsignedShort()
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
        if (i_59_ == 1) i_66_ += this.faceCount
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
            val i_84_ = packet.readUnsignedByte()
            var i_85_ = 0
            if ((i_84_ and 0x1) != 0) i_85_ = packet_53_.method3362()
            var i_86_ = 0
            if ((i_84_ and 0x2) != 0) i_86_ = packet_54_.method3362()
            var i_87_ = 0
            if ((0x4 and i_84_) != 0) i_87_ = packet_55_.method3362()
            this.vertexX!![i_83_] = i_85_ + i_80_
            this.vertexY!![i_83_] = i_81_ - -i_86_
            this.vertexZ!![i_83_] = i_82_ + i_87_
            i_82_ = this.vertexZ!![i_83_]
            i_81_ = this.vertexY!![i_83_]
            i_80_ = this.vertexX!![i_83_]
            if (i_61_ == 1) this.vertexLabel!![i_83_] = packet_56_.readUnsignedByte()
        }
        packet.pos = i_75_
        packet_53_.pos = i_71_
        packet_54_.pos = i_69_
        packet_55_.pos = i_73_
        packet_56_.pos = i_70_
        for (i_88_ in 0..<this.faceCount) {
            this.faceColour!![i_88_] = packet.readUnsignedShort().toShort()
            if (i_57_ == 1) {
                val i_89_ = packet_53_.readUnsignedByte()
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
            if (i_58_ == 255) this.facePriority!![i_88_] = packet_54_.readByte()
            if (i_59_ == 1) this.faceAlpha!![i_88_] = packet_55_.readByte()
            if (i_60_ == 1) this.faceLabel!![i_88_] = packet_56_.readUnsignedByte()
        }
        packet.pos = i_74_
        this.maxVertex = -1
        packet_53_.pos = i_68_
        var i_90_: Short = 0
        var i_91_: Short = 0
        var i_92_: Short = 0
        var i_93_ = 0
        for (i_94_ in 0..<this.faceCount) {
            val i_95_ = packet_53_.readUnsignedByte()
            if (i_95_ == 1) {
                i_90_ = (i_93_ + packet.method3362()).toShort()
                i_93_ = i_90_.toInt()
                i_91_ = (packet.method3362() + i_93_).toShort()
                i_93_ = i_91_.toInt()
                i_92_ = (i_93_ + packet.method3362()).toShort()
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
                i_92_ = (packet.method3362() + i_93_).toShort()
                this.faceA!![i_94_] = i_90_
                i_93_ = i_92_.toInt()
                this.faceB!![i_94_] = i_91_
                this.faceC!![i_94_] = i_92_
                if (i_92_ > this.maxVertex) this.maxVertex = i_92_.toInt()
            }
            if (i_95_ == 3) {
                i_90_ = i_92_
                i_92_ = (packet.method3362() + i_93_).toShort()
                i_93_ = i_92_.toInt()
                this.faceA!![i_94_] = i_90_
                this.faceB!![i_94_] = i_91_
                this.faceC!![i_94_] = i_92_
                if (this.maxVertex < i_92_) this.maxVertex = i_92_.toInt()
            }
            if (i_95_ == 4) {
                val i_96_ = i_90_
                i_90_ = i_91_
                i_92_ = (packet.method3362() + i_93_).toShort()
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
            this.texSpaceDefA!![i_97_] = packet.readUnsignedShort().toShort()
            this.texSpaceDefB!![i_97_] = packet.readUnsignedShort().toShort()
            this.texSpaceDefC!![i_97_] = packet.readUnsignedShort().toShort()
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

    constructor(`is`: ByteArray) {
        this.faceCount = 0
        this.globalPriority = 0.toByte()
        this.maxVertex = 0
        this.texSpaceCount = 0
        if (`is`[`is`.size + -1].toInt() == -1 && `is`[-2 + `is`.size].toInt() == -1) method1106(`is`)
        else method1103(`is`)
    }

    companion object {

        fun method1436(i_3_: Int): Boolean {
            return i_3_ == (i_3_ and -i_3_)
        }

        var anIntArray1204: IntArray
        var anIntArray1207: IntArray = IntArray(16384)

        init {
            anIntArray1204 = IntArray(16384)
            val d = 3.834951969714103E-4
            for (i in 0..16383) {
                anIntArray1207[i] = (16384.0 * sin(d * i.toDouble())).toInt()
                anIntArray1204[i] = (cos(d * i.toDouble()) * 16384.0).toInt()
            }
        }

        fun method1097(i_20_: Int, random: Random): Int {
            require(i_20_ > 0)
            if (method1436(i_20_)) return (i_20_.toLong() * (0xffffffffL and random.nextInt().toLong()) shr 32).toInt()
            val i_21_ = -2147483648 + -(4294967296L % i_20_.toLong()).toInt()
            var i_22_: Int
            do i_22_ = random.nextInt() while (i_22_ >= i_21_)
            return JavaBillboardAttributes.method3452(i_22_, i_20_)
        }
    }
}
