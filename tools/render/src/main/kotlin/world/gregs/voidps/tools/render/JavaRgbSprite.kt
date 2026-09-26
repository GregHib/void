package world.gregs.voidps.tools.render

/* Class105_Sub3_Sub1 */

internal class JavaRgbSprite : Sprite_Sub3 {
    var anIntArray9933: IntArray

    override fun method964(i: Int, i_883_: Int, i_884_: Int, i_885_: Int, i_886_: Int) {
        var i = i
        var i_883_ = i_883_
        var i_885_ = i_885_
        check(!this.aHa_Sub1_8460!!.method3716())
        val i_887_ = this.aHa_Sub1_8460!!.anInt7477
        i += this.anInt8461
        i_883_ += this.anInt8464
        var i_888_ = i_883_ * i_887_ + i
        var i_889_ = 0
        var i_890_ = this.anInt8470
        var i_891_ = this.anInt8471
        var i_892_ = i_887_ - i_891_
        var i_893_ = 0
        if (i_883_ < this.aHa_Sub1_8460!!.anInt7476) {
            val i_894_ = (this.aHa_Sub1_8460!!.anInt7476 - i_883_)
            i_890_ -= i_894_
            i_883_ = this.aHa_Sub1_8460!!.anInt7476
            i_889_ += i_894_ * i_891_
            i_888_ += i_894_ * i_887_
        }
        if (i_883_ + i_890_ > this.aHa_Sub1_8460!!.anInt7503) i_890_ -= i_883_ + i_890_ - this.aHa_Sub1_8460!!.anInt7503
        if (i < this.aHa_Sub1_8460!!.anInt7496) {
            val i_895_ = (this.aHa_Sub1_8460!!.anInt7496 - i)
            i_891_ -= i_895_
            i = this.aHa_Sub1_8460!!.anInt7496
            i_889_ += i_895_
            i_888_ += i_895_
            i_893_ += i_895_
            i_892_ += i_895_
        }
        if (i + i_891_ > this.aHa_Sub1_8460!!.anInt7507) {
            val i_896_ = (i + i_891_ - (this.aHa_Sub1_8460!!.anInt7507))
            i_891_ -= i_896_
            i_893_ += i_896_
            i_892_ += i_896_
        }
        if (i_891_ > 0 && i_890_ > 0) {
            val `is` = (this.aHa_Sub1_8460!!.anIntArray7483)
            if (i_886_ == 0) {
                if (i_884_ == 1) {
                    for (i_897_ in -i_890_..-1) {
                        var i_898_ = i_888_ + i_891_ - 3
                        while (i_888_ < i_898_) {
                            `is`!![i_888_++] = (this.anIntArray9933[i_889_++])
                            `is`[i_888_++] = (this.anIntArray9933[i_889_++])
                            `is`[i_888_++] = (this.anIntArray9933[i_889_++])
                            `is`[i_888_++] = (this.anIntArray9933[i_889_++])
                        }
                        i_898_ += 3
                        while (i_888_ < i_898_) `is`!![i_888_++] = (this.anIntArray9933[i_889_++])
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 0) {
                    val i_899_ = (i_885_ and 0xff0000) shr 16
                    val i_900_ = (i_885_ and 0xff00) shr 8
                    val i_901_ = i_885_ and 0xff
                    for (i_902_ in -i_890_..-1) {
                        for (i_903_ in -i_891_..-1) {
                            val i_904_ = (this.anIntArray9933[i_889_++])
                            val i_905_ = (i_904_ and 0xff0000) * i_899_ and 0xffffff.inv()
                            val i_906_ = (i_904_ and 0xff00) * i_900_ and 0xff0000
                            val i_907_ = (i_904_ and 0xff) * i_901_ and 0xff00
                            `is`!![i_888_++] = (i_905_ or i_906_ or i_907_) ushr 8
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 3) {
                    for (i_908_ in -i_890_..-1) {
                        for (i_909_ in -i_891_..-1) {
                            val i_910_ = (this.anIntArray9933[i_889_++])
                            val i_911_ = i_910_ + i_885_
                            val i_912_ = (i_910_ and 0xff00ff) + (i_885_ and 0xff00ff)
                            val i_913_ = ((i_912_ and 0x1000100) + (i_911_ - i_912_ and 0x10000))
                            `is`!![i_888_++] = i_911_ - i_913_ or i_913_ - (i_913_ ushr 8)
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 2) {
                    val i_914_ = i_885_ ushr 24
                    val i_915_ = 256 - i_914_
                    var i_916_ = (i_885_ and 0xff00ff) * i_915_ and 0xff00ff.inv()
                    var i_917_ = (i_885_ and 0xff00) * i_915_ and 0xff0000
                    i_885_ = (i_916_ or i_917_) ushr 8
                    for (i_918_ in -i_890_..-1) {
                        for (i_919_ in -i_891_..-1) {
                            val i_920_ = (this.anIntArray9933[i_889_++])
                            i_916_ = (i_920_ and 0xff00ff) * i_914_ and 0xff00ff.inv()
                            i_917_ = (i_920_ and 0xff00) * i_914_ and 0xff0000
                            `is`!![i_888_++] = ((i_916_ or i_917_) ushr 8) + i_885_
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                throw IllegalArgumentException()
            }
            if (i_886_ == 1) {
                if (i_884_ == 1) {
                    for (i_921_ in -i_890_..-1) {
                        var i_922_ = i_888_ + i_891_ - 3
                        while (i_888_ < i_922_) {
                            var i_923_ = (this.anIntArray9933[i_889_++])
                            if (i_923_ != 0) `is`!![i_888_++] = i_923_
                            else i_888_++
                            i_923_ = (this.anIntArray9933[i_889_++])
                            if (i_923_ != 0) `is`!![i_888_++] = i_923_
                            else i_888_++
                            i_923_ = (this.anIntArray9933[i_889_++])
                            if (i_923_ != 0) `is`!![i_888_++] = i_923_
                            else i_888_++
                            i_923_ = (this.anIntArray9933[i_889_++])
                            if (i_923_ != 0) `is`!![i_888_++] = i_923_
                            else i_888_++
                        }
                        i_922_ += 3
                        while (i_888_ < i_922_) {
                            val i_924_ = (this.anIntArray9933[i_889_++])
                            if (i_924_ != 0) `is`!![i_888_++] = i_924_
                            else i_888_++
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 0) {
                    if ((i_885_ and 0xffffff) == 16777215) {
                        val i_925_ = i_885_ ushr 24
                        val i_926_ = 256 - i_925_
                        for (i_927_ in -i_890_..-1) {
                            for (i_928_ in -i_891_..-1) {
                                val i_929_ = (this.anIntArray9933[i_889_++])
                                if (i_929_ != 0) {
                                    val i_930_ = `is`!![i_888_]
                                    `is`[i_888_++] = ((((i_929_ and 0xff00ff) * i_925_ + (i_930_ and 0xff00ff) * i_926_) and 0xff00ff.inv()) + (((i_929_ and 0xff00) * i_925_ + (i_930_ and 0xff00) * i_926_) and 0xff0000)) shr 8
                                } else i_888_++
                            }
                            i_888_ += i_892_
                            i_889_ += i_893_
                        }
                    } else {
                        val i_931_ = (i_885_ and 0xff0000) shr 16
                        val i_932_ = (i_885_ and 0xff00) shr 8
                        val i_933_ = i_885_ and 0xff
                        val i_934_ = i_885_ ushr 24
                        val i_935_ = 256 - i_934_
                        for (i_936_ in -i_890_..-1) {
                            for (i_937_ in -i_891_..-1) {
                                var i_938_ = (this.anIntArray9933[i_889_++])
                                if (i_938_ != 0) {
                                    if (i_934_ == 255) {
                                        val i_943_ = ((i_938_ and 0xff0000) * i_931_ and 0xffffff.inv())
                                        val i_944_ = ((i_938_ and 0xff00) * i_932_ and 0xff0000)
                                        val i_945_ = ((i_938_ and 0xff) * i_933_ and 0xff00)
                                        `is`!![i_888_++] = (i_943_ or i_944_ or i_945_) ushr 8
                                    } else {
                                        val i_939_ = ((i_938_ and 0xff0000) * i_931_ and 0xffffff.inv())
                                        val i_940_ = ((i_938_ and 0xff00) * i_932_ and 0xff0000)
                                        val i_941_ = ((i_938_ and 0xff) * i_933_ and 0xff00)
                                        i_938_ = (i_939_ or i_940_ or i_941_) ushr 8
                                        val i_942_ = `is`!![i_888_]
                                        `is`[i_888_++] = ((((i_938_ and 0xff00ff) * i_934_ + ((i_942_ and 0xff00ff) * i_935_)) and 0xff00ff.inv()) + (((i_938_ and 0xff00) * i_934_ + ((i_942_ and 0xff00) * i_935_)) and 0xff0000)) shr 8
                                    }
                                } else i_888_++
                            }
                            i_888_ += i_892_
                            i_889_ += i_893_
                        }
                        return
                    }
                    return
                }
                if (i_884_ == 3) {
                    val i_946_ = i_885_ ushr 24
                    val i_947_ = 256 - i_946_
                    for (i_948_ in -i_890_..-1) {
                        for (i_949_ in -i_891_..-1) {
                            var i_950_ = (this.anIntArray9933[i_889_++])
                            val i_951_ = i_950_ + i_885_
                            val i_952_ = (i_950_ and 0xff00ff) + (i_885_ and 0xff00ff)
                            var i_953_ = ((i_952_ and 0x1000100) + (i_951_ - i_952_ and 0x10000))
                            i_953_ = i_951_ - i_953_ or i_953_ - (i_953_ ushr 8)
                            if (i_950_ == 0 && i_946_ != 255) {
                                i_950_ = i_953_
                                i_953_ = `is`!![i_888_]
                                i_953_ = ((((i_950_ and 0xff00ff) * i_946_ + (i_953_ and 0xff00ff) * i_947_) and 0xff00ff.inv()) + (((i_950_ and 0xff00) * i_946_ + (i_953_ and 0xff00) * i_947_) and 0xff0000)) shr 8
                            }
                            `is`!![i_888_++] = i_953_
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 2) {
                    val i_954_ = i_885_ ushr 24
                    val i_955_ = 256 - i_954_
                    var i_956_ = (i_885_ and 0xff00ff) * i_955_ and 0xff00ff.inv()
                    var i_957_ = (i_885_ and 0xff00) * i_955_ and 0xff0000
                    i_885_ = (i_956_ or i_957_) ushr 8
                    for (i_958_ in -i_890_..-1) {
                        for (i_959_ in -i_891_..-1) {
                            val i_960_ = (this.anIntArray9933[i_889_++])
                            if (i_960_ != 0) {
                                i_956_ = (i_960_ and 0xff00ff) * i_954_ and 0xff00ff.inv()
                                i_957_ = (i_960_ and 0xff00) * i_954_ and 0xff0000
                                `is`!![i_888_++] = ((i_956_ or i_957_) ushr 8) + i_885_
                            } else i_888_++
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                throw IllegalArgumentException()
            }
            if (i_886_ == 2) {
                if (i_884_ == 1) {
                    for (i_961_ in -i_890_..-1) {
                        for (i_962_ in -i_891_..-1) {
                            val i_963_ = (this.anIntArray9933[i_889_++])
                            if (i_963_ != 0) {
                                var i_964_ = `is`!![i_888_]
                                val i_965_ = i_963_ + i_964_
                                val i_966_ = ((i_963_ and 0xff00ff) + (i_964_ and 0xff00ff))
                                i_964_ = (i_966_ and 0x1000100) + (i_965_ - i_966_ and 0x10000)
                                `is`[i_888_++] = i_965_ - i_964_ or i_964_ - (i_964_ ushr 8)
                            } else i_888_++
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 0) {
                    val i_967_ = (i_885_ and 0xff0000) shr 16
                    val i_968_ = (i_885_ and 0xff00) shr 8
                    val i_969_ = i_885_ and 0xff
                    for (i_970_ in -i_890_..-1) {
                        for (i_971_ in -i_891_..-1) {
                            var i_972_ = (this.anIntArray9933[i_889_++])
                            if (i_972_ != 0) {
                                val i_973_ = (i_972_ and 0xff0000) * i_967_ and 0xffffff.inv()
                                val i_974_ = (i_972_ and 0xff00) * i_968_ and 0xff0000
                                val i_975_ = (i_972_ and 0xff) * i_969_ and 0xff00
                                i_972_ = (i_973_ or i_974_ or i_975_) ushr 8
                                var i_976_ = `is`!![i_888_]
                                val i_977_ = i_972_ + i_976_
                                val i_978_ = ((i_972_ and 0xff00ff) + (i_976_ and 0xff00ff))
                                i_976_ = (i_978_ and 0x1000100) + (i_977_ - i_978_ and 0x10000)
                                `is`[i_888_++] = i_977_ - i_976_ or i_976_ - (i_976_ ushr 8)
                            } else i_888_++
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 3) {
                    for (i_979_ in -i_890_..-1) {
                        for (i_980_ in -i_891_..-1) {
                            var i_981_ = (this.anIntArray9933[i_889_++])
                            var i_982_ = i_981_ + i_885_
                            var i_983_ = (i_981_ and 0xff00ff) + (i_885_ and 0xff00ff)
                            var i_984_ = ((i_983_ and 0x1000100) + (i_982_ - i_983_ and 0x10000))
                            i_981_ = i_982_ - i_984_ or i_984_ - (i_984_ ushr 8)
                            i_984_ = `is`!![i_888_]
                            i_982_ = i_981_ + i_984_
                            i_983_ = (i_981_ and 0xff00ff) + (i_984_ and 0xff00ff)
                            i_984_ = (i_983_ and 0x1000100) + (i_982_ - i_983_ and 0x10000)
                            `is`[i_888_++] = i_982_ - i_984_ or i_984_ - (i_984_ ushr 8)
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                if (i_884_ == 2) {
                    val i_985_ = i_885_ ushr 24
                    val i_986_ = 256 - i_985_
                    var i_987_ = (i_885_ and 0xff00ff) * i_986_ and 0xff00ff.inv()
                    var i_988_ = (i_885_ and 0xff00) * i_986_ and 0xff0000
                    i_885_ = (i_987_ or i_988_) ushr 8
                    for (i_989_ in -i_890_..-1) {
                        for (i_990_ in -i_891_..-1) {
                            var i_991_ = (this.anIntArray9933[i_889_++])
                            if (i_991_ != 0) {
                                i_987_ = (i_991_ and 0xff00ff) * i_985_ and 0xff00ff.inv()
                                i_988_ = (i_991_ and 0xff00) * i_985_ and 0xff0000
                                i_991_ = ((i_987_ or i_988_) ushr 8) + i_885_
                                var i_992_ = `is`!![i_888_]
                                val i_993_ = i_991_ + i_992_
                                val i_994_ = ((i_991_ and 0xff00ff) + (i_992_ and 0xff00ff))
                                i_992_ = (i_994_ and 0x1000100) + (i_993_ - i_994_ and 0x10000)
                                `is`[i_888_++] = i_993_ - i_992_ or i_992_ - (i_992_ ushr 8)
                            } else i_888_++
                        }
                        i_888_ += i_892_
                        i_889_ += i_893_
                    }
                    return
                }
                throw IllegalArgumentException()
            }
            throw IllegalArgumentException()
        }
    }
    override fun method996(i: Int, i_995_: Int, i_996_: Int, i_997_: Int, i_998_: Int, i_999_: Int, i_1000_: Int, i_1001_: Int, i_1002_: Int) {
        throw IllegalStateException()
    }

    constructor(var_ha_Sub1: JavaToolkit?, i: Int, i_743_: Int) : super(var_ha_Sub1, i, i_743_) {
        this.anIntArray9933 = IntArray(i * i_743_)
    }

    constructor(var_ha_Sub1: JavaToolkit?, `is`: IntArray, i: Int, i_744_: Int, i_745_: Int, i_746_: Int) : super(var_ha_Sub1, i_745_, i_746_) {
        var i = i
        var i_744_ = i_744_
        this.anIntArray9933 = IntArray(i_745_ * i_746_)
        i_744_ -= this.anInt8471
        var i_747_ = 0
        for (i_748_ in 0..<i_746_) {
            for (i_749_ in 0..<i_745_) {
                val i_750_ = `is`[i++]
                if (i_750_ ushr 24 == 255) this.anIntArray9933[i_747_++] = if ((i_750_ and 0xffffff) == 0) -16777215 else i_750_
                else this.anIntArray9933[i_747_++] = 0
            }
            i += i_744_
        }
    }

    constructor(var_ha_Sub1: JavaToolkit?, `is`: IntArray, i: Int, i_751_: Int) : super(var_ha_Sub1, i, i_751_) {
        this.anIntArray9933 = `is`
    }
}
