package world.gregs.voidps.tools.icon;/* Class213 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class ItemType {
    int id;
    int anInt2752 = -1;
    private int anInt2753;
    static int anInt2754;
    boolean grandExchange;
    private int mesh;
    IterableHashTable params;
    int anInt2758;
    int anInt2759;
    private int anInt2760;
    ItemTypeList list;
    int[] stackIds;
    String[] iop;
    int anInt2764;
    private int resizeZ;
    int anInt2766;
    private int anInt2767;
    private int anInt2770 = -1;
    private short[] aShortArray2771;
    int[] quests;
    static Class238 aClass238_2773;
    int anInt2774;
    private int anInt2775;
    static int anInt2776;
    private short[] recol_s;
    int anInt2778;
    int anInt2779;
    static int anInt2780;
    int anInt2781;
    boolean members;
    int anInt2784;
    private short[] retex_s;
    private int resizeX;
    int anInt2787;
    int anInt2788;
    static long aLong2789;
    static int anInt2790;
    private int ambient;
    private int anInt2792;
    private int resizeY;
    String name;
    static int anInt2796;
    private int anInt2797;
    private int anInt2799;
    static int anInt2800;
    private short[] retex_d;
    int anInt2802;
    private int anInt2803;
    private int anInt2804;
    private int anInt2805;
    static int anInt2806;
    private int anInt2807;
    private int anInt2808;
    int anInt2810;
    String[] op;
    int lendTemplateId;
    static int anInt2814;
    int anInt2815;
    static int anInt2816;
    int anInt2817;
    int anInt2818;
    int anInt2819;
    int stackable;
    private byte[] recol_d_palette;
    private int anInt2822;
    private int anInt2823;
    private int contrast;
    int zoom2d;
    int anInt2826;
    int team;
    static int anInt2829;
    int anInt2830;
    int[] stackAmounts;
    static int anInt2832;
    int notedTemplateId;

    final Mesh method1554(boolean bool, int i) {
        anInt2796++;
        int i_0_ = anInt2792;
        int i_1_ = anInt2767;
        if (bool) {
            i_1_ = anInt2775;
            i_0_ = anInt2822;
        }
        if (i_0_ == -1) return null;
        Mesh mesh = Class300.load(0, this.list.meshes, i_0_, -1);
        if ((~mesh.version) > i) mesh.upscale(2, 54);
        if (i_1_ != -1) {
            Mesh mesh_2_ = Class300.load(0, (this.list.meshes), i_1_, -1);
            if (mesh_2_.version < 13) mesh_2_.upscale(2, i ^ ~0x78);
            Mesh[] meshes = {mesh, mesh_2_};
            mesh = new Mesh(meshes, 2);
        }
        if (recol_s != null) {
            for (int i_3_ = 0; i_3_ < recol_s.length; i_3_++)
                mesh.recolour(recol_s[i_3_], (byte) 126, aShortArray2771[i_3_]);
        }
        if (retex_s != null) {
            for (int i_4_ = 0; i_4_ < retex_s.length; i_4_++)
                mesh.retexture(retex_s[i_4_], 0, retex_d[i_4_]);
        }
        return mesh;
    }

    final void lendGen(ItemType itemType_9_, byte i, ItemType itemType_10_) {
        try {
            anInt2808 = itemType_9_.anInt2808;
            this.anInt2779 = itemType_10_.anInt2779;
            this.anInt2819 = 0;
            retex_s = itemType_9_.retex_s;
            this.anInt2788 = itemType_9_.anInt2788;
            this.anInt2781 = itemType_10_.anInt2781;
            this.op = itemType_9_.op;
            anInt2797 = itemType_9_.anInt2797;
            recol_d_palette = itemType_9_.recol_d_palette;
            aShortArray2771 = itemType_9_.aShortArray2771;
            this.name = itemType_9_.name;
            mesh = itemType_10_.mesh;
            anInt2770 = itemType_9_.anInt2770;
            recol_s = itemType_9_.recol_s;
            this.anInt2815 = itemType_9_.anInt2815;
            anInt2822 = itemType_9_.anInt2822;
            anInt2804 = itemType_9_.anInt2804;
            this.zoom2d = itemType_10_.zoom2d;
            anInt2767 = itemType_9_.anInt2767;
            retex_d = itemType_9_.retex_d;
            this.members = itemType_9_.members;
            this.anInt2810 = itemType_10_.anInt2810;
            this.iop = new String[5];
            if (i > -5) method1554(false, -92);
            anInt2823 = itemType_9_.anInt2823;
            anInt2775 = itemType_9_.anInt2775;
            anInt2832++;
            anInt2807 = itemType_9_.anInt2807;
            anInt2792 = itemType_9_.anInt2792;
            this.params = itemType_9_.params;
            anInt2805 = itemType_9_.anInt2805;
            this.team = itemType_9_.team;
            this.anInt2787 = itemType_10_.anInt2787;
            anInt2760 = itemType_9_.anInt2760;
            anInt2753 = itemType_9_.anInt2753;
            anInt2803 = itemType_9_.anInt2803;
            this.anInt2826 = itemType_10_.anInt2826;
            if (itemType_9_.iop != null) {
                for (int i_11_ = 0; i_11_ < 4; i_11_++)
                    this.iop[i_11_] = itemType_9_.iop[i_11_];
            }
            this.iop[4] = LocalisedText.Discard.method2063((this.list.languageId), 544);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("rq.G(" + (itemType_9_ != null ? "{...}" : "null") + ',' + i + ',' + (itemType_10_ != null ? "{...}" : "null") + ')'));
        }
    }

    private final String formatAmount(int i, int i_12_) {
        anInt2816++;
        if (i_12_ != -11619) this.anInt2788 = -113;
        if (i < 100000) return "<col=ffff00>" + i + "</col>";
        if (i < 10000000) return ("<col=ffffff>" + i / 1000 + LocalisedText.K.method2063((this.list.languageId), 544) + "</col>");
        return ("<col=00ff80>" + i / 1000000 + LocalisedText.M.method2063((this.list.languageId), 544) + "</col>");
    }

    final int[] sprite(int invCount, boolean bool, int graphicShadow, Toolkit toolkit, Toolkit scratchToolkit, Font font, PlayerModel playerModel, int itemNumMode, byte i_40_, int outline) {
        try {
            anInt2806++;
            Mesh mesh = Class300.load(0, (this.list.meshes), this.mesh, i_40_ ^ 0x65);
            if (mesh == null) return null;
            if (mesh.version < 13) mesh.upscale(2, i_40_ ^ ~0xb);
            if (recol_s != null) {
                for (int i_42_ = 0; (recol_s.length > i_42_); i_42_++) {
                    if (recol_d_palette == null || i_42_ >= recol_d_palette.length) mesh.recolour(recol_s[i_42_], (byte) 126, aShortArray2771[i_42_]);
                    else mesh.recolour(recol_s[i_42_], (byte) 126, (Class336.aShortArray4172[recol_d_palette[i_42_] & 0xff]));
                }
            }
            if (retex_s != null) {
                for (int i_43_ = 0; (retex_s.length > i_43_); i_43_++)
                    mesh.retexture(retex_s[i_43_], 0, retex_d[i_43_]);
            }
            if (playerModel != null) {
                for (int i_44_ = 0; i_44_ < 5; i_44_++) {
                    for (int i_45_ = 0; (i_45_ < Class367_Sub2.clientPalette.length); i_45_++) {
                        if ((Class367_Sub2.clientPalette[i_45_][i_44_]).length > playerModel.recol_d[i_44_]) {
                            mesh.recolour((Class136.recol_s[i_45_][i_44_]), (byte) 126, (Class367_Sub2.clientPalette[i_45_][i_44_][(playerModel.recol_d[i_44_])]));
                        }
                    }
                }
            }
            int functionMask = 2048;
            boolean scaled = false;
            if (resizeX != 128 || resizeY != 128 || resizeZ != 128) {
                functionMask |= 0x7;
                scaled = true;
            }
            Model model = scratchToolkit.createModel(mesh, functionMask, 64, ambient + 64, 768 + contrast);
            if (!model.loadedTextures()) return null;
            if (scaled) model.O(resizeX, resizeY, resizeZ);
            Sprite sprite = null;
            if (this.notedTemplateId == -1) {
                if (this.lendTemplateId != -1) {
                    sprite = (this.list.sprite(scratchToolkit, graphicShadow, invCount, font, playerModel, 0, true, (byte) 83, toolkit, this.anInt2778, false, outline));
                    if (sprite == null) return null;
                }
            } else {
                sprite = (this.list.sprite(scratchToolkit, 0, 10, font, playerModel, 0, true, (byte) 83, toolkit, this.anInt2758, true, 1));
                if (sprite == null) return null;
            }
            int zoom;
            if (!bool) {
                if (outline == 2) zoom = ((int) (1.04 * (double) this.zoom2d) << 2);
                else zoom = this.zoom2d << 2;
            } else zoom = ((int) (1.5 * (double) this.zoom2d) << 2);
            scratchToolkit.DA(16, 16, 512, 512);
            Matrix matrix = scratchToolkit.method3654();
            matrix.makeIdentity();
            scratchToolkit.setCamera(matrix);
            scratchToolkit.xa(1.0F);
            scratchToolkit.ZA(16777215, 1.0F, 1.0F, -50.0F, -10.0F, -50.0F);
            Matrix scratch = scratchToolkit.method3705();
            scratch.makeRotationZ(-this.anInt2810 << 3);
            scratch.makeAxisY(this.anInt2781 << 3);
            scratch.translate(this.anInt2779 << 2, ((zoom * (Class70.anIntArray1207[this.anInt2787 << 3]) >> 14) - model.fa() / 2 + (this.anInt2826 << 2)), ((zoom * (Class70.anIntArray1204[this.anInt2787 << 3]) >> 14) - -(this.anInt2826 << 2)));
            scratch.rotateAxisX(this.anInt2787 << 3);
            int i_50_ = scratchToolkit.i();
            int i_51_ = scratchToolkit.XA();
            scratchToolkit.f(50, 2147483647);
            scratchToolkit.ya();
            scratchToolkit.la();
            scratchToolkit.aa(0, 0, 36, 32, 0, 0);
            model.render(scratch, null, 1);
            scratchToolkit.f(i_50_, i_51_);
            int[] image = scratchToolkit.na(0, 0, 36, 32);
            if (i_40_ != -102) method1554(false, 37);
            if (outline >= 1) {
                image = colourBorder(-16777214, -1, image);
                if (outline >= 2) image = colourBorder(-1, -1, image);
            }
            if (graphicShadow != 0) applyShadow(graphicShadow, image, (byte) 119);
            scratchToolkit.createSprite(36, image, (byte) 94, 0, 36, 32).render(0, 0);
            if (this.notedTemplateId == -1) {
                if (this.lendTemplateId != -1) sprite.render(0, 0);
            } else sprite.render(0, 0);
            if (itemNumMode == 1 || (itemNumMode == 2 && (this.stackable == 1 || invCount != 1) && invCount != -1)) {
                font.render(formatAmount(invCount, i_40_ + -11517), -256, 9, 0, -16777215, i_40_ + -15);
            }
            image = scratchToolkit.na(0, 0, 36, 32);
            for (int i_52_ = 0; i_52_ < image.length; i_52_++) {
                if ((0xffffff & image[i_52_]) != 0) image[i_52_] = Class273.or(image[i_52_], -16777216);
                else image[i_52_] = 0;
            }
            return image;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("rq.O(" + invCount + ',' + bool + ',' + graphicShadow + ',' + (toolkit != null ? "{...}" : "null") + ',' + (scratchToolkit != null ? "{...}" : "null") + ',' + (font != null ? "{...}" : "null") + ',' + (playerModel != null ? "{...}" : "null") + ',' + itemNumMode + ',' + i_40_ + ',' + outline + ')'));
        }
    }

    final void postDecode(byte i) {
        if (i < 32) this.anInt2752 = -31;
        anInt2814++;
    }

    public static void method1564(int i) {
        aClass238_2773 = null;
        if (i <= 54) aLong2789 = -74L;
    }

    final boolean method1565(boolean bool, int i) {
        anInt2780++;
        int i_53_ = this.anInt2815;
        int i_54_ = anInt2804;
        int i_55_ = anInt2805;
        if (bool) {
            i_55_ = anInt2770;
            i_53_ = this.anInt2788;
            i_54_ = anInt2760;
        }
        if (i_53_ == -1) return true;
        boolean bool_56_ = true;
        if (!this.list.meshes.requestDownload(-10499, i_53_, 0)) bool_56_ = false;
        if (i_54_ != -1 && !this.list.meshes.requestDownload(i ^ 0x2902, i_54_, 0)) bool_56_ = false;
        if (i != i_55_ && !this.list.meshes.requestDownload(-10499, i_55_, 0)) bool_56_ = false;
        return bool_56_;
    }

    private final void decode(int code, int i_57_, Packet packet) {
        try {
            if (code != 4) method1564(9);
            if (i_57_ != 1) {
                if (i_57_ != 2) {
                    if (i_57_ == 4) this.zoom2d = packet.readUnsignedShort(code ^ 0x3235f8fc);
                    else if (i_57_ == 5) this.anInt2787 = packet.readUnsignedShort(842397944);
                    else if (i_57_ == 6) this.anInt2781 = packet.readUnsignedShort(code ^ 0x3235f8fc);
                    else if (i_57_ == 7) {
                        this.anInt2779 = packet.readUnsignedShort(842397944);
                        if (this.anInt2779 > 32767) this.anInt2779 -= 65536;
                    } else if (i_57_ == 8) {
                        this.anInt2826 = packet.readUnsignedShort(842397944);
                        if (this.anInt2826 > 32767) this.anInt2826 -= 65536;
                    } else if (i_57_ != 11) {
                        if (i_57_ != 12) {
                            if (i_57_ == 16) this.members = true;
                            else if (i_57_ == 18) this.anInt2802 = packet.readUnsignedShort(code ^ 0x3235f8fc);
                            else if (i_57_ == 23) this.anInt2815 = packet.readUnsignedShort(842397944);
                            else if (i_57_ == 24) anInt2804 = packet.readUnsignedShort(code ^ 0x3235f8fc);
                            else if (i_57_ == 25) this.anInt2788 = packet.readUnsignedShort(code + 842397940);
                            else if (i_57_ == 26) anInt2760 = packet.readUnsignedShort(code + 842397940);
                            else if (i_57_ < 30 || i_57_ >= 35) {
                                if (i_57_ >= 35 && i_57_ < 40) this.iop[-35 + i_57_] = packet.readString((byte) 103);
                                else if (i_57_ == 40) {
                                    int i_58_ = packet.readUnsignedByte(255);
                                    recol_s = new short[i_58_];
                                    aShortArray2771 = new short[i_58_];
                                    for (int i_59_ = 0; i_58_ > i_59_; i_59_++) {
                                        recol_s[i_59_] = (short) (packet.readUnsignedShort(842397944));
                                        aShortArray2771[i_59_] = (short) (packet.readUnsignedShort(842397944));
                                    }
                                } else if (i_57_ == 41) {
                                    int i_68_ = packet.readUnsignedByte(code + 251);
                                    retex_d = new short[i_68_];
                                    retex_s = new short[i_68_];
                                    for (int i_69_ = 0; i_69_ < i_68_; i_69_++) {
                                        retex_s[i_69_] = (short) (packet.readUnsignedShort(842397944));
                                        retex_d[i_69_] = (short) (packet.readUnsignedShort(842397944));
                                    }
                                } else if (i_57_ == 42) {
                                    int i_60_ = packet.readUnsignedByte(code + 251);
                                    recol_d_palette = new byte[i_60_];
                                    for (int i_61_ = 0; i_61_ < i_60_; i_61_++)
                                        recol_d_palette[i_61_] = packet.readByte(-114);
                                } else if (i_57_ == 65) this.grandExchange = true;
                                else if (i_57_ == 78) anInt2805 = packet.readUnsignedShort(842397944);
                                else if (i_57_ == 79) anInt2770 = (packet.readUnsignedShort(code ^ 0x3235f8fc));
                                else if (i_57_ == 90) anInt2792 = packet.readUnsignedShort(842397944);
                                else if (i_57_ == 91) anInt2822 = packet.readUnsignedShort(842397944);
                                else if (i_57_ != 92) {
                                    if (i_57_ != 93) {
                                        if (i_57_ != 95) {
                                            if (i_57_ != 96) {
                                                if (i_57_ == 97) this.anInt2758 = (packet.readUnsignedShort(842397944));
                                                else if (i_57_ == 98) this.notedTemplateId = (packet.readUnsignedShort(842397944));
                                                else if ((i_57_ >= 100) && (i_57_ < 110)) {
                                                    if ((this.stackIds) == null) {
                                                        this.stackAmounts = (new int
                                                                [10]);
                                                        this.stackIds = (new int
                                                                [10]);
                                                    }
                                                    this.stackIds[i_57_ - 100] = (packet.readUnsignedShort(842397944));
                                                    this.stackAmounts[i_57_ + -100] = (packet.readUnsignedShort(842397944));
                                                } else if (i_57_ == 110) resizeX = (packet.readUnsignedShort(842397944));
                                                else if (i_57_ != 111) {
                                                    if (i_57_ == 112) resizeZ = (packet.readUnsignedShort(842397944));
                                                    else if (i_57_ != 113) {
                                                        if (i_57_ == 114) contrast = ((packet.readByte(-90)) * 5);
                                                        else if (i_57_ == 115) this.team = (packet.readUnsignedByte(255));
                                                        else if (i_57_ != 121) {
                                                            if (i_57_ != 122) {
                                                                if (i_57_ == 125) {
                                                                    anInt2807 = packet.readByte(-99) << 2;
                                                                    anInt2797 = packet.readByte(code + -99) << 2;
                                                                    anInt2808 = packet.readByte(-111) << 2;
                                                                } else if (i_57_ == 126) {
                                                                    anInt2803 = packet.readByte(-121) << 2;
                                                                    anInt2753 = packet.readByte(-92) << 2;
                                                                    anInt2823 = packet.readByte(-93) << 2;
                                                                } else if (i_57_ == 127) {
                                                                    this.anInt2752 = packet.readUnsignedByte(255);
                                                                    this.anInt2759 = packet.readUnsignedShort(842397944);
                                                                } else if (i_57_ == 128) {
                                                                    this.anInt2764 = packet.readUnsignedByte(255);
                                                                    this.anInt2830 = packet.readUnsignedShort(842397944);
                                                                } else if (i_57_ == 129) {
                                                                    this.anInt2766 = packet.readUnsignedByte(code ^ 0xfb);
                                                                    this.anInt2818 = packet.readUnsignedShort(842397944);
                                                                } else if (i_57_ == 130) {
                                                                    this.anInt2774 = packet.readUnsignedByte(255);
                                                                    this.anInt2817 = packet.readUnsignedShort(842397944);
                                                                } else if (i_57_ == 132) {
                                                                    int i_62_ = packet.readUnsignedByte(code ^ 0xfb);
                                                                    this.quests = new int[i_62_];
                                                                    for (int i_63_ = 0; i_62_ > i_63_; i_63_++)
                                                                        this.quests[i_63_] = packet.readUnsignedShort(842397944);
                                                                } else if (i_57_ == 134) this.anInt2784 = packet.readUnsignedByte(255);
                                                                else if (i_57_ == 249) {
                                                                    int i_64_ = packet.readUnsignedByte(255);
                                                                    if (this.params == null) {
                                                                        int i_65_ = Class33.method340(i_64_, (byte) 108);
                                                                        this.params = new IterableHashTable(i_65_);
                                                                    }
                                                                    for (int i_66_ = 0; i_66_ < i_64_; i_66_++) {
                                                                        boolean bool = packet.readUnsignedByte(255) == 1;
                                                                        int i_67_ = packet.readMedium(-1);
                                                                        Class348 class348;
                                                                        if (bool) class348 = new Class348_Sub50(packet.readString((byte) 107));
                                                                        else class348 = new Class348_Sub35(packet.readInt((byte) -126));
                                                                        this.params.put((byte) 76, i_67_, class348);
                                                                    }
                                                                }
                                                            } else this.lendTemplateId = packet.readUnsignedShort(code + 842397940);
                                                        } else this.anInt2778 = (packet.readUnsignedShort(842397944));
                                                    } else ambient = (packet.readByte(-88));
                                                } else resizeY = (packet.readUnsignedShort(842397944));
                                            } else this.anInt2799 = (packet.readUnsignedByte(255));
                                        } else this.anInt2810 = (packet.readUnsignedShort(code + 842397940));
                                    } else anInt2775 = (packet.readUnsignedShort(code + 842397940));
                                } else anInt2767 = packet.readUnsignedShort(842397944);
                            } else this.op[-30 + i_57_] = packet.readString((byte) 98);
                        } else this.anInt2819 = packet.readInt((byte) -126);
                    } else this.stackable = 1;
                } else this.name = packet.readString((byte) -42);
            } else mesh = packet.readUnsignedShort(code + 842397940);
            anInt2754++;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("rq.L(" + code + ',' + i_57_ + ',' + (packet != null ? "{...}" : "null") + ')'));
        }
    }

    final void decode(int i, Packet packet) {
        try {
            if (i != 768) method1565(true, -71);
            for (; ; ) {
                int code = packet.readUnsignedByte(i + -513);
                if (code == 0) break;
                decode(4, code, packet);
            }
            anInt2800++;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("rq.I(" + i + ',' + (packet != null ? "{...}" : "null") + ')'));
        }
    }

    final void noteGen(int i, ItemType itemType_94_, ItemType itemType_95_) {
        try {
            aShortArray2771 = itemType_95_.aShortArray2771;
            retex_s = itemType_95_.retex_s;
            recol_d_palette = itemType_95_.recol_d_palette;
            this.members = itemType_94_.members;
            this.anInt2787 = itemType_95_.anInt2787;
            this.anInt2810 = itemType_95_.anInt2810;
            anInt2776++;
            this.anInt2779 = itemType_95_.anInt2779;
            retex_d = itemType_95_.retex_d;
            this.anInt2781 = itemType_95_.anInt2781;
            this.anInt2819 = itemType_94_.anInt2819;
            this.anInt2826 = itemType_95_.anInt2826;
            this.stackable = i;
            recol_s = itemType_95_.recol_s;
            this.zoom2d = itemType_95_.zoom2d;
            this.name = itemType_94_.name;
            mesh = itemType_95_.mesh;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("rq.F(" + i + ',' + (itemType_94_ != null ? "{...}" : "null") + ',' + (itemType_95_ != null ? "{...}" : "null") + ')'));
        }
    }

    private final void applyShadow(int i, int[] is, byte i_96_) {
        try {
            if (i_96_ <= 81) anInt2805 = -46;
            for (int i_97_ = 31; i_97_ > 0; i_97_--) {
                int i_98_ = 36 * i_97_;
                for (int i_99_ = 35; i_99_ > 0; i_99_--) {
                    if (is[i_99_ - -i_98_] == 0 && is[i_99_ + i_98_ - 1 + -36] != 0) is[i_98_ + i_99_] = i;
                }
            }
            anInt2790++;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("rq.M(" + i + ',' + (is != null ? "{...}" : "null") + ',' + i_96_ + ')'));
        }
    }

    public ItemType() {
        this.anInt2766 = -1;
        anInt2775 = -1;
        resizeZ = 128;
        this.anInt2778 = -1;
        this.anInt2758 = -1;
        this.name = "null";
        resizeY = 128;
        this.anInt2799 = 0;
        this.anInt2788 = -1;
        this.anInt2784 = 0;
        anInt2804 = -1;
        anInt2792 = -1;
        this.grandExchange = false;
        this.anInt2787 = 0;
        this.lendTemplateId = -1;
        this.anInt2810 = 0;
        anInt2767 = -1;
        anInt2760 = -1;
        this.anInt2817 = -1;
        this.anInt2779 = 0;
        resizeX = 128;
        anInt2808 = 0;
        anInt2803 = 0;
        anInt2822 = -1;
        this.anInt2815 = -1;
        anInt2823 = 0;
        this.anInt2818 = -1;
        this.anInt2781 = 0;
        this.anInt2774 = -1;
        this.anInt2826 = 0;
        anInt2807 = 0;
        ambient = 0;
        anInt2805 = -1;
        this.anInt2764 = -1;
        this.stackable = 0;
        this.anInt2759 = -1;
        this.anInt2802 = -1;
        anInt2797 = 0;
        this.anInt2830 = -1;
        this.team = 0;
        contrast = 0;
        this.anInt2819 = 1;
        anInt2753 = 0;
        this.zoom2d = 2000;
        this.notedTemplateId = -1;
        this.members = false;
    }

    private final int[] colourBorder(int i, int i_100_, int[] is) {
        try {
            anInt2829++;
            if (i_100_ != -1) return null;
            int[] is_101_ = new int[1152];
            int i_102_ = 0;
            for (int i_103_ = 0; i_103_ < 32; i_103_++) {
                for (int i_104_ = 0; i_104_ < 36; i_104_++) {
                    int i_105_ = is[i_102_];
                    if (i_105_ == 0) {
                        if (i_104_ <= 0 || is[i_102_ - 1] == 0) {
                            if (i_103_ > 0 && is[-36 + i_102_] != 0) i_105_ = i;
                            else if (i_104_ < 35 && is[i_102_ + 1] != 0) i_105_ = i;
                            else if (i_103_ < 31 && is[i_102_ + 36] != 0) i_105_ = i;
                        } else i_105_ = i;
                    }
                    is_101_[i_102_++] = i_105_;
                }
            }
            return is_101_;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("rq.K(" + i + ',' + i_100_ + ',' + (is != null ? "{...}" : "null") + ')'));
        }
    }
}
