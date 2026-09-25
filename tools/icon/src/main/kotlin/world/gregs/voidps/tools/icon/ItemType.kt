package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.Cache

/* Class213 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class ItemType {
    var id: Int = 0
    var anInt2752: Int = -1
    private var anInt2753 = 0
    var grandExchange: Boolean = false
    private var mesh = 0
    var params: IterableHashTable? = null
    var anInt2758: Int
    var anInt2759: Int
    private var anInt2760: Int
    var list: ItemTypeList? = null
    var stackIds: IntArray? = null
    var iop: Array<String?>? = null
    var anInt2764: Int
    private var resizeZ = 128
    var anInt2766: Int
    private var anInt2767: Int
    private var anInt2770 = -1
    private var aShortArray2771: ShortArray? = null
    var quests: IntArray? = null
    var anInt2774: Int
    private var anInt2775: Int
    private var recol_s: ShortArray? = null
    var anInt2778: Int
    var anInt2779: Int = 0
    var anInt2781: Int = 0
    var members: Boolean = false
    var anInt2784: Int = 0
    private var retex_s: ShortArray? = null
    private var resizeX = 128
    var anInt2787: Int = 0
    var anInt2788: Int
    private var ambient = 0
    private var anInt2792: Int
    private var resizeY = 128
    var name: String? = "null"
    private var anInt2797 = 0
    private var anInt2799 = 0
    private var retex_d: ShortArray? = null
    var anInt2802: Int
    private var anInt2803 = 0
    private var anInt2804: Int
    private var anInt2805: Int
    private var anInt2807 = 0
    private var anInt2808 = 0
    var anInt2810: Int = 0
    var op: Array<String?>? = null
    var lendTemplateId: Int
    var anInt2815: Int
    var anInt2817: Int
    var anInt2818: Int
    var anInt2819: Int = 1
    var stackable: Int = 0
    private var recol_d_palette: ByteArray? = null
    private var anInt2822: Int
    private var anInt2823 = 0
    private var contrast = 0
    var zoom2d: Int = 2000
    var anInt2826: Int = 0
    var team: Int = 0
    var anInt2830: Int
    var stackAmounts: IntArray? = null
    var notedTemplateId: Int

    fun lendGen(itemType_9_: ItemType?, itemType_10_: ItemType?) {
        anInt2808 = itemType_9_!!.anInt2808
        this.anInt2779 = itemType_10_!!.anInt2779
        this.anInt2819 = 0
        retex_s = itemType_9_.retex_s
        this.anInt2788 = itemType_9_.anInt2788
        this.anInt2781 = itemType_10_.anInt2781
        this.op = itemType_9_.op
        anInt2797 = itemType_9_.anInt2797
        recol_d_palette = itemType_9_.recol_d_palette
        aShortArray2771 = itemType_9_.aShortArray2771
        this.name = itemType_9_.name
        mesh = itemType_10_.mesh
        anInt2770 = itemType_9_.anInt2770
        recol_s = itemType_9_.recol_s
        this.anInt2815 = itemType_9_.anInt2815
        anInt2822 = itemType_9_.anInt2822
        anInt2804 = itemType_9_.anInt2804
        this.zoom2d = itemType_10_.zoom2d
        anInt2767 = itemType_9_.anInt2767
        retex_d = itemType_9_.retex_d
        this.members = itemType_9_.members
        this.anInt2810 = itemType_10_.anInt2810
        this.iop = arrayOfNulls<String>(5)
        anInt2823 = itemType_9_.anInt2823
        anInt2775 = itemType_9_.anInt2775
        anInt2807 = itemType_9_.anInt2807
        anInt2792 = itemType_9_.anInt2792
        this.params = itemType_9_.params
        anInt2805 = itemType_9_.anInt2805
        this.team = itemType_9_.team
        this.anInt2787 = itemType_10_.anInt2787
        anInt2760 = itemType_9_.anInt2760
        anInt2753 = itemType_9_.anInt2753
        anInt2803 = itemType_9_.anInt2803
        this.anInt2826 = itemType_10_.anInt2826
        if (itemType_9_.iop != null) {
            for (i_11_ in 0..3) this.iop!![i_11_] = itemType_9_.iop!![i_11_]
        }
        this.iop!![4] = LocalisedText.Discard!!.method2063((this.list!!.languageId))
    }

    fun sprite(invCount: Int, bool: Boolean, graphicShadow: Int, toolkit: Toolkit?, scratchToolkit: Toolkit?, outline: Int): IntArray? {
        val mesh = load(this.list!!.cache!!, this.mesh)
        if (mesh == null) return null
        if (mesh.version < 13) mesh.upscale(2)
        if (recol_s != null) {
            var i_42_ = 0
            while ((recol_s!!.size > i_42_)) {
                if (recol_d_palette == null || i_42_ >= recol_d_palette!!.size) mesh.recolour(recol_s!![i_42_], aShortArray2771!![i_42_])
                else mesh.recolour(recol_s!![i_42_], (aShortArray4172[recol_d_palette!![i_42_].toInt() and 0xff]))
                i_42_++
            }
        }
        if (retex_s != null) {
            var i_43_ = 0
            while ((retex_s!!.size > i_43_)) {
                mesh.retexture(retex_s!![i_43_], retex_d!![i_43_])
                i_43_++
            }
        }
        var functionMask = 2048
        var scaled = false
        if (resizeX != 128 || resizeY != 128 || resizeZ != 128) {
            functionMask = functionMask or 0x7
            scaled = true
        }
        val model = scratchToolkit!!.createModel(mesh, functionMask, 64, ambient + 64, 768 + contrast)
        if (!model!!.loadedTextures()) return null
        if (scaled) model.O(resizeX, resizeY, resizeZ)
        var sprite: Sprite? = null
        if (this.notedTemplateId == -1) {
            if (this.lendTemplateId != -1) {
                sprite = (this.list!!.sprite(scratchToolkit, graphicShadow, invCount, toolkit, this.anInt2778, false, outline))
                if (sprite == null) return null
            }
        } else {
            sprite = (this.list!!.sprite(scratchToolkit, 0, 10, toolkit, this.anInt2758, true, 1))
            if (sprite == null) return null
        }
        val zoom: Int
        if (!bool) {
            if (outline == 2) zoom = ((1.04 * this.zoom2d.toDouble()).toInt() shl 2)
            else zoom = this.zoom2d shl 2
        } else zoom = ((1.5 * this.zoom2d.toDouble()).toInt() shl 2)
        scratchToolkit.DA(16, 16, 512, 512)
        val matrix = scratchToolkit.method3654()
        matrix!!.makeIdentity()
        scratchToolkit.setCamera(matrix)
        scratchToolkit.xa(1.0f)
        scratchToolkit.ZA(16777215, 1.0f, 1.0f, -50.0f, -10.0f, -50.0f)
        val scratch = scratchToolkit.method3705()
        scratch!!.makeRotationZ(-this.anInt2810 shl 3)
        scratch.makeAxisY(this.anInt2781 shl 3)
        scratch.translate(this.anInt2779 shl 2, ((zoom * (Mesh.anIntArray1207[this.anInt2787 shl 3]) shr 14) - model.fa() / 2 + (this.anInt2826 shl 2)), ((zoom * (Mesh.anIntArray1204[this.anInt2787 shl 3]) shr 14) - -(this.anInt2826 shl 2)))
        scratch.rotateAxisX(this.anInt2787 shl 3)
        val i_50_ = scratchToolkit.i()
        val i_51_ = scratchToolkit.XA()
        scratchToolkit.f(50, 2147483647)
        scratchToolkit.ya()
        scratchToolkit.la()
        scratchToolkit.aa(0, 0, 36, 32, 0, 0)
        model.render(scratch, 1)
        scratchToolkit.f(i_50_, i_51_)
        var image = scratchToolkit.na(0, 0, 36, 32)
        if (outline >= 1) {
            image = colourBorder(-16777214, image)
            if (outline >= 2) image = colourBorder(-1, image)
        }
        if (graphicShadow != 0) applyShadow(graphicShadow, image)
        scratchToolkit.createSprite(36, image, 36, 32)!!.render(0, 0)
        if (this.notedTemplateId == -1) {
            if (this.lendTemplateId != -1) sprite!!.render(0, 0)
        } else sprite!!.render(0, 0)
        image = scratchToolkit.na(0, 0, 36, 32)
        for (i_52_ in image!!.indices) {
            if ((0xffffff and image[i_52_]) != 0) image[i_52_] = Class348_Sub40_Sub12.or(image[i_52_], -16777216)
            else image[i_52_] = 0
        }
        return image
    }

    private fun decode(i_57_: Int, packet: Packet?) {
        if (i_57_ != 1) {
            if (i_57_ != 2) {
                if (i_57_ == 4) this.zoom2d = packet!!.readUnsignedShort()
                else if (i_57_ == 5) this.anInt2787 = packet!!.readUnsignedShort()
                else if (i_57_ == 6) this.anInt2781 = packet!!.readUnsignedShort()
                else if (i_57_ == 7) {
                    this.anInt2779 = packet!!.readUnsignedShort()
                    if (this.anInt2779 > 32767) this.anInt2779 -= 65536
                } else if (i_57_ == 8) {
                    this.anInt2826 = packet!!.readUnsignedShort()
                    if (this.anInt2826 > 32767) this.anInt2826 -= 65536
                } else if (i_57_ != 11) {
                    if (i_57_ != 12) {
                        if (i_57_ == 16) this.members = true
                        else if (i_57_ == 18) this.anInt2802 = packet!!.readUnsignedShort()
                        else if (i_57_ == 23) this.anInt2815 = packet!!.readUnsignedShort()
                        else if (i_57_ == 24) anInt2804 = packet!!.readUnsignedShort()
                        else if (i_57_ == 25) this.anInt2788 = packet!!.readUnsignedShort()
                        else if (i_57_ == 26) anInt2760 = packet!!.readUnsignedShort()
                        else if (i_57_ < 30 || i_57_ >= 35) {
                            if (i_57_ >= 35 && i_57_ < 40) this.iop!![-35 + i_57_] = packet!!.readString()
                            else if (i_57_ == 40) {
                                val i_58_ = packet!!.readUnsignedByte()
                                recol_s = ShortArray(i_58_)
                                aShortArray2771 = ShortArray(i_58_)
                                var i_59_ = 0
                                while (i_58_ > i_59_) {
                                    recol_s!![i_59_] = (packet.readUnsignedShort()).toShort()
                                    aShortArray2771!![i_59_] = (packet.readUnsignedShort()).toShort()
                                    i_59_++
                                }
                            } else if (i_57_ == 41) {
                                val i_68_ = packet!!.readUnsignedByte()
                                retex_d = ShortArray(i_68_)
                                retex_s = ShortArray(i_68_)
                                for (i_69_ in 0..<i_68_) {
                                    retex_s!![i_69_] = (packet.readUnsignedShort()).toShort()
                                    retex_d!![i_69_] = (packet.readUnsignedShort()).toShort()
                                }
                            } else if (i_57_ == 42) {
                                val i_60_ = packet!!.readUnsignedByte()
                                recol_d_palette = ByteArray(i_60_)
                                for (i_61_ in 0..<i_60_) recol_d_palette!![i_61_] = packet.readByte()
                            } else if (i_57_ == 65) this.grandExchange = true
                            else if (i_57_ == 78) anInt2805 = packet!!.readUnsignedShort()
                            else if (i_57_ == 79) anInt2770 = (packet!!.readUnsignedShort())
                            else if (i_57_ == 90) anInt2792 = packet!!.readUnsignedShort()
                            else if (i_57_ == 91) anInt2822 = packet!!.readUnsignedShort()
                            else if (i_57_ != 92) {
                                if (i_57_ != 93) {
                                    if (i_57_ != 95) {
                                        if (i_57_ != 96) {
                                            if (i_57_ == 97) this.anInt2758 = (packet!!.readUnsignedShort())
                                            else if (i_57_ == 98) this.notedTemplateId = (packet!!.readUnsignedShort())
                                            else if ((i_57_ >= 100) && (i_57_ < 110)) {
                                                if ((this.stackIds) == null) {
                                                    this.stackAmounts = (IntArray(10))
                                                    this.stackIds = (IntArray(10))
                                                }
                                                this.stackIds!![i_57_ - 100] = (packet!!.readUnsignedShort())
                                                this.stackAmounts!![i_57_ + -100] = (packet.readUnsignedShort())
                                            } else if (i_57_ == 110) resizeX = (packet!!.readUnsignedShort())
                                            else if (i_57_ != 111) {
                                                if (i_57_ == 112) resizeZ = (packet!!.readUnsignedShort())
                                                else if (i_57_ != 113) {
                                                    if (i_57_ == 114) contrast = ((packet!!.readByte()) * 5)
                                                    else if (i_57_ == 115) this.team = (packet!!.readUnsignedByte())
                                                    else if (i_57_ != 121) {
                                                        if (i_57_ != 122) {
                                                            if (i_57_ == 125) {
                                                                anInt2807 = packet!!.readByte().toInt() shl 2
                                                                anInt2797 = packet.readByte().toInt() shl 2
                                                                anInt2808 = packet.readByte().toInt() shl 2
                                                            } else if (i_57_ == 126) {
                                                                anInt2803 = packet!!.readByte().toInt() shl 2
                                                                anInt2753 = packet.readByte().toInt() shl 2
                                                                anInt2823 = packet.readByte().toInt() shl 2
                                                            } else if (i_57_ == 127) {
                                                                this.anInt2752 = packet!!.readUnsignedByte()
                                                                this.anInt2759 = packet.readUnsignedShort()
                                                            } else if (i_57_ == 128) {
                                                                this.anInt2764 = packet!!.readUnsignedByte()
                                                                this.anInt2830 = packet.readUnsignedShort()
                                                            } else if (i_57_ == 129) {
                                                                this.anInt2766 = packet!!.readUnsignedByte()
                                                                this.anInt2818 = packet.readUnsignedShort()
                                                            } else if (i_57_ == 130) {
                                                                this.anInt2774 = packet!!.readUnsignedByte()
                                                                this.anInt2817 = packet.readUnsignedShort()
                                                            } else if (i_57_ == 132) {
                                                                val i_62_ = packet!!.readUnsignedByte()
                                                                this.quests = IntArray(i_62_)
                                                                var i_63_ = 0
                                                                while (i_62_ > i_63_) {
                                                                    this.quests!![i_63_] = packet.readUnsignedShort()
                                                                    i_63_++
                                                                }
                                                            } else if (i_57_ == 134) this.anInt2784 = packet!!.readUnsignedByte()
                                                            else if (i_57_ == 249) {
                                                                val i_64_ = packet!!.readUnsignedByte()
                                                                if (this.params == null) {
                                                                    val i_65_ = method340(i_64_)
                                                                    this.params = IterableHashTable(i_65_)
                                                                }
                                                                for (i_66_ in 0..<i_64_) {
                                                                    val bool = packet.readUnsignedByte() == 1
                                                                    val i_67_ = packet.readMedium()
                                                                    val class348: Class348?
                                                                    if (bool) class348 = Class348_Sub50(packet.readString())
                                                                    else class348 = Class348_Sub35(packet.readInt())
                                                                    this.params!!.put(i_67_.toLong(), class348)
                                                                }
                                                            }
                                                        } else this.lendTemplateId = packet!!.readUnsignedShort()
                                                    } else this.anInt2778 = (packet!!.readUnsignedShort())
                                                } else ambient = (packet!!.readByte()).toInt()
                                            } else resizeY = (packet!!.readUnsignedShort())
                                        } else this.anInt2799 = (packet!!.readUnsignedByte())
                                    } else this.anInt2810 = (packet!!.readUnsignedShort())
                                } else anInt2775 = (packet!!.readUnsignedShort())
                            } else anInt2767 = packet!!.readUnsignedShort()
                        } else this.op!![-30 + i_57_] = packet!!.readString()
                    } else this.anInt2819 = packet!!.readInt()
                } else this.stackable = 1
            } else this.name = packet!!.readString()
        } else mesh = packet!!.readUnsignedShort()
    }

    fun decode(packet: Packet?) {
        while (true) {
            val code = packet!!.readUnsignedByte()
            if (code == 0) break
            decode(code, packet)
        }
    }

    fun noteGen(itemType_94_: ItemType?, itemType_95_: ItemType?) {
        aShortArray2771 = itemType_95_!!.aShortArray2771
        retex_s = itemType_95_.retex_s
        recol_d_palette = itemType_95_.recol_d_palette
        this.members = itemType_94_!!.members
        this.anInt2787 = itemType_95_.anInt2787
        this.anInt2810 = itemType_95_.anInt2810
        this.anInt2779 = itemType_95_.anInt2779
        retex_d = itemType_95_.retex_d
        this.anInt2781 = itemType_95_.anInt2781
        this.anInt2819 = itemType_94_.anInt2819
        this.anInt2826 = itemType_95_.anInt2826
        this.stackable = 1
        recol_s = itemType_95_.recol_s
        this.zoom2d = itemType_95_.zoom2d
        this.name = itemType_94_.name
        mesh = itemType_95_.mesh
    }

    private fun applyShadow(i: Int, `is`: IntArray?) {
        for (i_97_ in 31 downTo 1) {
            val i_98_ = 36 * i_97_
            for (i_99_ in 35 downTo 1) {
                if (`is`!![i_99_ - -i_98_] == 0 && `is`[i_99_ + i_98_ - 1 + -36] != 0) `is`[i_98_ + i_99_] = i
            }
        }
    }

    init {
        this.anInt2766 = -1
        anInt2775 = -1
        this.anInt2778 = -1
        this.anInt2758 = -1
        this.anInt2788 = -1
        anInt2804 = -1
        anInt2792 = -1
        this.lendTemplateId = -1
        anInt2767 = -1
        anInt2760 = -1
        this.anInt2817 = -1
        anInt2822 = -1
        this.anInt2815 = -1
        this.anInt2818 = -1
        this.anInt2774 = -1
        anInt2805 = -1
        this.anInt2764 = -1
        this.anInt2759 = -1
        this.anInt2802 = -1
        this.anInt2830 = -1
        this.notedTemplateId = -1
    }

    private fun colourBorder(i: Int, `is`: IntArray?): IntArray? {
        val is_101_ = IntArray(1152)
        var i_102_ = 0
        for (i_103_ in 0..31) {
            for (i_104_ in 0..35) {
                var i_105_ = `is`!![i_102_]
                if (i_105_ == 0) {
                    if (i_104_ <= 0 || `is`[i_102_ - 1] == 0) {
                        if (i_103_ > 0 && `is`[-36 + i_102_] != 0) i_105_ = i
                        else if (i_104_ < 35 && `is`[i_102_ + 1] != 0) i_105_ = i
                        else if (i_103_ < 31 && `is`[i_102_ + 36] != 0) i_105_ = i
                    } else i_105_ = i
                }
                is_101_[i_102_++] = i_105_
            }
        }
        return is_101_
    }

    companion object {

        fun method340(i: Int): Int {
            var i = i
            i = --i or (i ushr 1)
            i = i or (i ushr 2)
            i = i or (i ushr 4)
            i = i or (i ushr 8)
            i = i or (i ushr 16)
            return 1 + i
        }

        fun load(cache: Cache, i_5_: Int): Mesh? {
            val `is` = cache.data(Index.MODELS, i_5_, 0)
            if (`is` == null) return null
            return Mesh(`is`)
        }


        var aShortArray4172: ShortArray = ShortArray(256)
    }
}
