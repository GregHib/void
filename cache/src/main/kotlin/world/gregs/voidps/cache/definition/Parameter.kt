package world.gregs.voidps.cache.definition

@Suppress("MemberVisibilityCanBePrivate")
object Parameter {// TODO convert to config file
    const val STAB_ATTACK = 0L // 606.cs2
    const val SLASH_ATTACK = 1L
    const val CRUSH_ATTACK = 2L
    const val MAGIC_ATTACK = 3L
    const val RANGE_ATTACK = 4L
    const val STAB_DEFENCE = 5L
    const val SLASH_DEFENCE = 6L
    const val CRUSH_DEFENCE = 7L
    const val MAGIC_DEFENCE = 8L
    const val RANGE_DEFENCE = 9L
    const val PRAYER_BONUS = 11L
    const val ATTACK_SPEED = 14L
    const val PARAM_21 = 21L // bows and crossbows - projectile?
    const val REQUIRED_LEVEL = 23L // 912.cs2
    const val UNBANKABLE = 59L
    const val PARAM_61 = 61L // 2145.cs2, 2148.cs2, 2150.cs2, 2151.cs2, 2152.cs2, 2160.cs2, 2162.cs2, 2164.cs2
    const val PARAM_130 = 130L // 285.cs2, 292.cs2, 4.cs2
    const val PARAM_131 = 131L // 4.cs2
    const val PARAM_134 = 134L // 4.cs2
    const val PARAM_135 = 135L // 4.cs2
    const val PARAM_152 = 152L // 2160.cs2, 2162.cs2, 2164.cs2
    const val PARAM_153 = 153L // 2160.cs2, 2162.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_ID_1 = 211L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_AMOUNT_1 = 212L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_ID_2 = 213L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_AMOUNT_2 = 214L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_ID_3 = 215L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_AMOUNT_3 = 216L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_ID_4 = 217L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_AMOUNT_4 = 218L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_ID_5 = 219L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_AMOUNT_5 = 220L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_ID_6 = 221L // 1864.cs2
    const val CONSTRUCTION_REQUIRED_ITEM_AMOUNT_6 = 222L // 1864.cs2
    const val CONSTRUCTION_ITEM = 226L
    const val ELITE_CLUE_SCROLL = 235L
    const val ELITE_CLUE_SCROLL_NEXT = 236L
    const val GOD_ARROW = 237L
    const val SKILL_CAPE = 258L
    const val TRIMMED_SKILL_CAPE = 259L // 2720.cs2, 2723.cs2
    const val MAXED_SKILL = 277L
    const val PARAM_316 = 316L // 2059.cs2
    const val MOBILISING_ARMIES_ATTACKABLE_OBJ = 351L
    const val MOBILISING_ARMIES_COLLECTABLE_OBJ = 352L
    const val GRAVESTONE_TIME = 356L // 687.cs2
    const val SUMMONING_BEAST_OF_BURDEN_CAPACITY = 379L
    const val SUMMONING_POUCH_LEVEL = 394L // 751.cs2
    const val SUMMONING_DEFENCE = 417L
    const val SUMMONING_TIME_MINUTES = 424L
    const val SUMMONING_ITEM = 457L // 319.cs2, 322.cs2
    const val SIGNPOST = 457L
    const val PARAM_477 = 477L // 1840.cs2
    const val PARAM_478 = 478L // 1839.cs2
    const val EQUIPPED_OPTION_1 = 528L // 1612.cs2
    const val EQUIPPED_OPTION_2 = 529L // 1612.cs2
    const val EQUIPPED_OPTION_3 = 530L // 1612.cs2
    const val EQUIPPED_OPTION_4 = 531L // 1612.cs2
    const val SUMMONING_POUCH_ID = 538L // 767.cs2
    const val SUMMONING_POUCH_AMOUNT = 539L // 766.cs2, 767.cs2
    const val SUMMONING_SHARD_ID = 540L // 767.cs2
    const val SUMMONING_SHARD_AMOUNT = 541L // 759.cs2, 766.cs2, 767.cs2, 793.cs2
    const val SUMMONING_CHARM_ID = 542L // 767.cs2
    const val SUMMONING_CHARM_AMOUNT = 543L // 766.cs2, 767.cs2
    const val PARAM_555 = 555L // 1799.cs2
    const val PARAM_556 = 556L // 1799.cs2
    const val PARAM_557 = 557L // 1781.cs2, 1799.cs2, 1833.cs2
    const val VIEWING_ORB_REPLACEMENT_OBJ = 580L
    const val PARAM_581 = 581L // 1769.cs2
    const val PARAM_582 = 582L // 1769.cs2
    const val PARAM_583 = 583L // 1769.cs2
    const val PARAM_584 = 584L
    const val PARAM_585 = 585L
    const val PARAM_586 = 586L
    const val PARAM_587 = 587L
    const val PARAM_595 = 595L // 1840.cs2
    const val PARAM_596 = 596L // 1840.cs2, 2010.cs2, 2689.cs2, 3788.cs2, 3789.cs2, 3790.cs2, 3791.cs2, 3792.cs2, 3793.cs2, 3794.cs2, 3795.cs2, 3796.cs2, 3797.cs2, 3798.cs2, 3799.cs2, 3800.cs2, 3801.cs2, 3802.cs2, 3803.cs2, 3804.cs2, 3805.cs2, 3806.cs2, 3807.cs2, 3808.cs2, 3809.cs2, 3810.cs2, 3811.cs2, 3812.cs2, 3813.cs2, 3814.cs2, 3815.cs2, 3816.cs2, 3817.cs2, 3818.cs2, 3819.cs2, 3820.cs2, 3821.cs2, 3822.cs2, 3823.cs2, 3824.cs2, 3825.cs2, 3826.cs2, 3827.cs2, 3828.cs2, 3829.cs2, 3830.cs2, 3831.cs2, 3832.cs2, 3833.cs2, 3834.cs2, 3835.cs2, 3836.cs2, 3837.cs2, 3838.cs2, 3839.cs2, 3840.cs2, 3841.cs2, 3842.cs2, 3843.cs2, 3844.cs2, 3845.cs2, 3846.cs2, 3847.cs2, 3852.cs2, 3853.cs2, 3854.cs2, 3855.cs2, 3856.cs2, 3857.cs2, 3858.cs2, 3859.cs2, 3860.cs2, 3861.cs2, 3862.cs2, 3863.cs2, 3864.cs2, 3865.cs2, 3866.cs2, 3867.cs2, 3871.cs2, 3873.cs2, 3874.cs2, 3875.cs2, 3876.cs2, 3877.cs2, 3878.cs2, 4576.cs2, 4577.cs2, 4578.cs2, 4579.cs2, 4751.cs2
    const val PARAM_597 = 597L // 1839.cs2, 1843.cs2
    const val SUMMONING_SCROLL = 599L // 322.cs2, 1670.cs2
    const val EQUIP_TYPE_HIDE_HAIR = 624L
    const val EQUIP_TYPE_HAT = 625L
    const val STRENGTH = 641L
    const val RANGED_STRENGTH = 643L
    const val RENDER_ANIMATION = 644L // 1608.cs2
    const val PARAM_654 = 654L // 1119.cs2, 2059.cs2
    const val PARAM_655 = 655L // 1119.cs2, 2059.cs2
    const val PARAM_656 = 656L // 1119.cs2
    const val PARAM_657 = 657L
    const val PARAM_658 = 658L // 1119.cs2, 2059.cs2
    const val PARAM_659 = 659L // 1119.cs2, 2059.cs2
    const val PARAM_660 = 660L // 1119.cs2, 2059.cs2
    const val PARAM_661 = 661L // 2060.cs2, 2061.cs2
    const val PARAM_662 = 662L // 1119.cs2, 2059.cs2
    const val PARAM_663 = 663L // 2059.cs2
    const val PARAM_664 = 664L // 2060.cs2, 2061.cs2
    const val PARAM_665 = 665L // 2061.cs2
    const val PARAM_670 = 670L // 2162.cs2
    const val PARAM_671 = 671L
    const val PARAM_673 = 673L // 2162.cs2
    const val PARAM_675 = 675L // 2162.cs2
    const val PARAM_676 = 676L // 2162.cs2
    const val PARAM_682 = 682L // 3387.cs2
    const val PARAM_683 = 683L // 2692.cs2, 2695.cs2, 3387.cs2
    const val PARAM_684 = 684L // 2059.cs2
    const val MAGIC_DAMAGE = 685L
    const val WEAPON_STYLE = 686L // 1142.cs2
    const val SPECIAL_ATTACK = 687L // 1136.cs2
    const val PARAM_688 = 688L // 2059.cs2
    const val HAND_CANNON_WARNING = 690L // 920.cs2
    const val PARAM_691 = 691L
    const val PARAM_694 = 694L // 2162.cs2
    const val PARAM_695 = 695L
    const val PARAM_696 = 696L
    const val SUMMONING_POUCH_REQ_ITEM_ID_1 = 697L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_AMOUNT_1 = 698L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_ID_2 = 699L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_AMOUNT_2 = 700L // 759.cs2, 766.cs2, 767.cs2
    const val PARAM_734 = 734L // 1237.cs2
    const val PARAM_735 = 735L // 1237.cs2, 2788.cs2
    const val PARAM_736 = 736L // 1237.cs2
    const val PARAM_737 = 737L // 1237.cs2, 2295.cs2
    const val PARAM_738 = 738L
    const val PARAM_739 = 739L // 2294.cs2
    const val UNLIT_BUG_LANTERN = 740L // 812.cs2, 920.cs2
    const val PARAM_741 = 741L // 927.cs2, 933.cs2, 934.cs2 cooking utensils, hasta's and dagon'hai items
    const val RECIPE_FOR_DISASTER_SAVE_COUNT = 742L // 933.cs2, 934.cs2
    const val QUEST_REQUIREMENT_SLOT_ID = 743L // 927.cs2, 930.cs2
    const val EQUIP_SKILL_1 = 749L // 927.cs2, 929.cs2
    const val EQUIP_LEVEL_1 = 750L // 929.cs2
    const val EQUIP_SKILL_2 = 751L // 929.cs2
    const val EQUIP_LEVEL_2 = 752L // 929.cs2
    const val EQUIP_SKILL_3 = 753L // 929.cs2
    const val EQUIP_LEVEL_3 = 754L // 929.cs2
    const val EQUIP_SKILL_4 = 755L // 929.cs2
    const val EQUIP_LEVEL_4 = 756L // 929.cs2
    const val EQUIP_SKILL_5 = 757L // 929.cs2
    const val EQUIP_LEVEL_5 = 758L // 929.cs2
    const val EQUIP_SKILL_6 = 759L // 929.cs2
    const val EQUIP_LEVEL_6 = 760L // 929.cs2
    const val REQUIRED_COMBAT = 761L // 925.cs2
    const val REQUIRED_QUEST_ID_1 = 762L // 928.cs2, 935.cs2, 936.cs2
    const val REQUIRED_QUEST_ID_2 = 764L // 928.cs2, 932.cs2
    const val REQUIRED_QUEST_ID_3 = 765L // 932.cs2
    const val USE_SKILL_1 = 770L // 928.cs2, 931.cs2
    const val USE_LEVEL_1 = 771L // 931.cs2
    const val USE_SKILL_2 = 772L // 931.cs2
    const val USE_LEVEL_2 = 773L // 931.cs2
    const val USE_SKILL_3 = 774L // 931.cs2
    const val USE_LEVEL_3 = 775L // 931.cs2
    const val USE_SKILL_4 = 776L // 931.cs2
    const val USE_LEVEL_4 = 777L // 931.cs2
    const val USE_SKILL_5 = 778L // 931.cs2
    const val USE_LEVEL_5 = 779L // 931.cs2
    const val USE_SKILL_6 = 780L // 931.cs2
    const val USE_LEVEL_6 = 781L // 931.cs2
    const val PARAM_782 = 782L // 943.cs2, 945.cs2, 947.cs2
    const val PARAM_788 = 788L // 2059.cs2, 2790.cs2, 350.cs2, 391.cs2
    const val PARAM_789 = 789L
    const val PARAM_790 = 790L
    const val PARAM_791 = 791L
    const val PARAM_792 = 792L // 2790.cs2, 391.cs2
    const val MOBILISING_ARMIES_CAVE = 799L
    const val MOBILISING_ARMIES_FISSURE = 800L
    const val MOBILISING_ARMIES_RESCUED = 801L
    const val MOBILISING_ARMIES_SQUAD = 802L // 2573.cs2
    const val MOBILISING_ARMIES_SQUAD_DEFEATED = 803L // 2570.cs2, 2573.cs2, 2597.cs2, 2599.cs2
    const val MOBILISING_ARMIES_SQUAD_TYPE = 805L // 2570.cs2, 2588.cs2
    const val MOBILISING_ARMIES_SQUAD_HEAVY = 806L // 2570.cs2, 2591.cs2
    const val MOBILISING_ARMIES_SQUAD_DEFEATED_ID = 811L
    const val PARAM_813 = 813L // 1692.cs2
    const val MOBILISING_ARMIES_SQUAD_ORIGINAL_ID = 814L
    const val MOBILISING_ARMIES_REWARD_CREDITS_COST = 821L
    const val BUG_LANTERN = 823L // 929.cs2
    const val PARAM_845 = 845L // 1692.cs2, 2145.cs2, 2160.cs2, 2163.cs2, 2180.cs2, 3222.cs2, 3387.cs2
    const val PARAM_846 = 846L // 1693.cs2, 2163.cs2
    const val PARAM_847 = 847L // 2149.cs2
    const val PARAM_848 = 848L // 2162.cs2
    const val PARAM_850 = 850L
    const val PARAM_851 = 851L
    const val PARAM_853 = 853L
    const val PARAM_854 = 854L
    const val PARAM_855 = 855L
    const val PARAM_856 = 856L // 2162.cs2
    const val PARAM_859 = 859L // 2155.cs2
    const val PARAM_860 = 860L // 2155.cs2
    const val PARAM_861 = 861L // 2155.cs2
    const val PARAM_862 = 862L // 2155.cs2
    const val PARAM_863 = 863L // 2155.cs2
    const val PARAM_864 = 864L // 2155.cs2
    const val PARAM_865 = 865L // 2155.cs2
    const val PARAM_866 = 866L // 2155.cs2
    const val PARAM_867 = 867L // 2155.cs2
    const val PARAM_868 = 868L // 2155.cs2
    const val PARAM_869 = 869L // 2155.cs2
    const val PARAM_870 = 870L // 2155.cs2
    const val PARAM_871 = 871L // 2145.cs2, 2153.cs2
    const val PARAM_872 = 872L // 2145.cs2, 2153.cs2
    const val PARAM_873 = 873L // 2145.cs2, 2153.cs2
    const val PARAM_874 = 874L // 2145.cs2, 2153.cs2
    const val PARAM_875 = 875L // 2145.cs2, 2153.cs2
    const val PARAM_876 = 876L // 2145.cs2, 2153.cs2
    const val PARAM_877 = 877L // 2145.cs2, 2153.cs2
    const val PARAM_878 = 878L // 2145.cs2, 2153.cs2
    const val PARAM_879 = 879L // 2145.cs2, 2153.cs2
    const val PARAM_880 = 880L // 2145.cs2, 2153.cs2
    const val PARAM_881 = 881L // 2145.cs2, 2153.cs2
    const val PARAM_882 = 882L // 2145.cs2, 2153.cs2
    const val PARAM_883 = 883L // 2145.cs2, 2153.cs2
    const val PARAM_884 = 884L // 2145.cs2, 2153.cs2
    const val PARAM_885 = 885L // 2145.cs2, 2153.cs2
    const val PARAM_886 = 886L // 2145.cs2, 2153.cs2
    const val PARAM_887 = 887L // 2145.cs2, 2153.cs2
    const val PARAM_888 = 888L // 2145.cs2, 2153.cs2
    const val PARAM_889 = 889L // 2145.cs2, 2153.cs2
    const val PARAM_890 = 890L // 2145.cs2, 2153.cs2
    const val PARAM_895 = 895L // 2149.cs2, 2150.cs2
    const val PARAM_896 = 896L // 2149.cs2, 2151.cs2
    const val PARAM_897 = 897L // 2153.cs2
    const val PARAM_898 = 898L
    const val PARAM_923 = 923L // 1692.cs2, 1693.cs2
    const val PARAM_924 = 924L // 1692.cs2
    const val PARAM_925 = 925L // 1692.cs2
    const val PARAM_935 = 935L // 2791.cs2
    const val PARAM_936 = 936L // 2791.cs2
    const val PARAM_937 = 937L // 2791.cs2
    const val PARAM_938 = 938L // 2791.cs2
    const val PARAM_940 = 940L // 2791.cs2
    const val PARAM_941 = 941L // 2791.cs2
    const val PARAM_942 = 942L // 2791.cs2
    const val PARAM_943 = 943L // 2791.cs2
    const val PARAM_944 = 944L // 2791.cs2
    const val PARAM_948 = 948L
    const val PARAM_949 = 949L
    const val PARAM_950 = 950L
    const val PARAM_951 = 951L // 3977.cs2, 3991.cs2
    const val PARAM_952 = 952L // 3969.cs2, 3971.cs2, 3977.cs2, 3989.cs2, 3991.cs2, 4243.cs2
    const val BARBARIAN_ASSAULT_REWARD = 954L
    const val BARBARIAN_ASSAULT_TICKET_WAVE = 955L
    const val FAMILIARISATION_FAMILIAR_1 = 956L // 2889.cs2
    const val FAMILIARISATION_FAMILIAR_2 = 957L // 2889.cs2
    const val FAMILIARISATION_TYPE = 958L
    const val MAGIC_STRENGTH = 965L
    const val ABSORB_MELEE = 967L
    const val ABSORB_RANGE = 968L
    const val ABSORB_MAGIC = 969L
    const val INFINITE_AIR_RUNES = 972L
    const val INFINITE_WATER_RUNES = 973L
    const val INFINITE_EARTH_RUNES = 974L
    const val INFINITE_FIRE_RUNES = 975L
    const val DUNGEONEERING_SHOP_MULTIPLIER = 1046L // 2262.cs2
    const val DUNGEONEERING = 1047L // 912.cs2, 2246.cs2
    const val DUNGEONEERING_BOUND_ITEM = 1050L
    const val DUNGEONEERING_BOUND_AMMO = 1051L // 2246.cs2
    const val PARAM_1070 = 1070L // 2247.cs2, 2250.cs2
    const val PARAM_1071 = 1071L // 2247.cs2, 2250.cs2
    const val PARAM_1072 = 1072L // 2247.cs2, 2250.cs2
    const val PARAM_1073 = 1073L
    const val PARAM_1074 = 1074L
    const val PARAM_1075 = 1075L
    const val PARAM_1076 = 1076L
    const val PARAM_1077 = 1077L
    const val PARAM_1078 = 1078L // 2250.cs2
    const val PARAM_1089 = 1089L // 3494.cs2
    const val PARAM_1090 = 1090L // 3494.cs2
    const val GOD_BOW_ID = 1091L
    const val GOD_BOW_AMOUNT = 1092L
    const val CONQUEST_UNIT_MOVEMENT = 1134L // 484.cs2, 485.cs2, 497.cs2
    const val CONQUEST_PARAM_2 = 1135L // 484.cs2, 485.cs2, 497.cs2
    const val CONQUEST_PARAM_3 = 1136L // 484.cs2, 485.cs2, 497.cs2
    const val CONQUEST_UNIT_RANGE = 1137L // 484.cs2, 485.cs2, 497.cs2
    const val CONQUEST_UNIT_COST = 1138L // 484.cs2, 485.cs2
    const val CONQUEST_UNIT_NAME = 1139L // 1417.cs2, 1139.cs2, 484.cs2, 485.cs2
    const val CONQUEST_UNIT_CHARACTER = 1140L // 1377.cs2
    const val CONQUEST_PARAM_8 = 1141L
    const val CONQUEST_SHADOW_NPC = 1142L
    const val CONQUEST_PARAM_9 = 1143L
    const val CONQUEST_PARAM_10 = 1144L
    const val CONQUEST_PARAM_11 = 1145L
    const val CONQUEST_PARAM_12 = 1146L
    const val CONQUEST_PARAM_13 = 1147L
    const val CONQUEST_PARAM_14 = 1148L // 484.cs2, 485.cs2
    const val PARAM_1149 = 1149L // 498.cs2
    const val PARAM_1150 = 1150L // 427.cs2, 432.cs2, 490.cs2, 493.cs2, 499.cs2
    const val PARAM_1151 = 1151L // 432.cs2, 493.cs2
    const val PARAM_1152 = 1152L // 499.cs2
    const val PARAM_1153 = 1153L // 427.cs2, 490.cs2, 493.cs2, 498.cs2
    const val PARAM_1154 = 1154L // 427.cs2, 429.cs2, 430.cs2, 432.cs2, 493.cs2
    const val PARAM_1155 = 1155L // 432.cs2, 493.cs2
    const val PARAM_1156 = 1156L
    const val PARAM_1157 = 1157L // 431.cs2
    const val CONSTRUCTION_FLOOR_1 = 1158L
    const val CONSTRUCTION_FLOOR_2 = 1159L
    const val PARAM_1160 = 1160L // 387.cs2
    const val PARAM_1161 = 1161L // 387.cs2
    const val PARAM_1162 = 1162L // 387.cs2
    const val PARAM_1163 = 1163L // 359.cs2
    const val PARAM_1164 = 1164L // 392.cs2
    const val PARAM_1165 = 1165L // 392.cs2
    const val PARAM_1166 = 1166L // 392.cs2
    const val PARAM_1167 = 1167L // 392.cs2
    const val PARAM_1168 = 1168L // 392.cs2
    const val PARAM_1169 = 1169L // 384.cs2
    const val PARAM_1170 = 1170L // 384.cs2
    const val PARAM_1171 = 1171L // 384.cs2
    const val PARAM_1172 = 1172L // 384.cs2
    const val PARAM_1173 = 1173L // 384.cs2
    const val PARAM_1174 = 1174L // 384.cs2
    const val PARAM_1175 = 1175L // 384.cs2
    const val PARAM_1176 = 1176L // 384.cs2
    const val PARAM_1177 = 1177L // 384.cs2
    const val PARAM_1178 = 1178L // 384.cs2
    const val PARAM_1179 = 1179L // 384.cs2
    const val PARAM_1180 = 1180L // 384.cs2
    const val PARAM_1181 = 1181L // 390.cs2
    const val CHARACTER_STYLE_TOP = 1182L
    const val CHARACTER_STYLE_ARMS = 1183L
    const val CHARACTER_STYLE_WRISTS = 1184L
    const val CHARACTER_STYLE_LEGS = 1185L
    const val CHARACTER_STYLE_SHOES = 1186L
    const val CHARACTER_STYLE_TOP_COLOUR = 1187L
    const val CHARACTER_STYLE_LEGS_COLOUR = 1188L
    const val CHARACTER_STYLE_SHOES_COLOUR = 1189L
    const val PARAM_1182 = 1182L // TODO 1514.cs2, 355.cs2, 359.cs2, 361.cs2
    const val PARAM_1183 = 1183L // TODO 1514.cs2, 355.cs2, 359.cs2, 361.cs2
    const val PARAM_1184 = 1184L // TODO 1514.cs2, 355.cs2, 359.cs2, 361.cs2
    const val PARAM_1185 = 1185L // TODO 359.cs2, 361.cs2
    const val PARAM_1186 = 1186L // TODO 359.cs2
    const val PARAM_1187 = 1187L // TODO 358.cs2
    const val PARAM_1188 = 1188L // TODO 358.cs2
    const val PARAM_1189 = 1189L // TODO 358.cs2, 360.cs2
    const val PARAM_1190 = 1190L // 358.cs2
    const val PARAM_1191 = 1191L // 358.cs2
    const val PARAM_1192 = 1192L // 358.cs2, 360.cs2
    const val PARAM_1193 = 1193L // 358.cs2
    const val PARAM_1194 = 1194L // 358.cs2
    const val PARAM_1195 = 1195L // 358.cs2, 360.cs2
    const val PARAM_1196 = 1196L // 358.cs2
    const val PARAM_1197 = 1197L // 358.cs2
    const val PARAM_1198 = 1198L // 358.cs2, 360.cs2
    const val PARAM_1199 = 1199L // 358.cs2
    const val PARAM_1200 = 1200L // 358.cs2
    const val PARAM_1201 = 1201L // 358.cs2, 360.cs2
    const val PARAM_1202 = 1202L // 358.cs2
    const val PARAM_1203 = 1203L // 358.cs2
    const val PARAM_1204 = 1204L // 358.cs2, 360.cs2
    const val PARAM_1205 = 1205L // 358.cs2
    const val PARAM_1206 = 1206L // 358.cs2
    const val PARAM_1207 = 1207L // 358.cs2, 360.cs2
    const val PARAM_1208 = 1208L // 358.cs2
    const val PARAM_1209 = 1209L // 358.cs2
    const val PARAM_1210 = 1210L // 358.cs2, 360.cs2
    const val EXTRA_EQUIPMENT_OPTION = 1211L // 1612.cs2
    const val PARAM_1212 = 1212L
    const val VOID_STARES_BACK_KEY_BLOCK_LABEL = 1225L
    const val VOID_STARES_BACK_KEY_BLOCK_WEIGHT = 1226L
    const val CONQUEST_UNIT_PARAM_15 = 1229L
    const val CONQUEST_UNIT_PARAM_16 = 1230L
    const val DYNAMIC_INVENTORY_OPTION_ORIGINAL = 1264L
    const val DYNAMIC_INVENTORY_OPTION_REPLACEMENT = 1265L // 1540.cs2
    const val PARAM_1266 = 1266L // 3969.cs2, 3971.cs2, 3977.cs2, 3979.cs2, 3988.cs2, 3989.cs2, 3991.cs2, 4000.cs2, 4243.cs2
    const val PARAM_1267 = 1267L // 3977.cs2, 3989.cs2, 3991.cs2
    const val PARAM_1268 = 1268L // 3994.cs2
    const val PARAM_1269 = 1269L // 3989.cs2
    const val PARAM_1270 = 1270L // 3227.cs2, 3969.cs2, 3971.cs2, 3977.cs2, 3989.cs2, 3991.cs2, 4243.cs2
    const val PARAM_1271 = 1271L // 3969.cs2, 3971.cs2, 3977.cs2, 3988.cs2, 3989.cs2, 3991.cs2, 4243.cs2
    const val PARAM_1272 = 1272L // 3971.cs2, 3977.cs2, 3989.cs2, 3991.cs2
    const val PARAM_1273 = 1273L // 3971.cs2, 3977.cs2, 3979.cs2, 3988.cs2, 3989.cs2, 3991.cs2, 4243.cs2
    const val PARAM_1274 = 1274L // 3977.cs2, 3991.cs2
    const val PARAM_1275 = 1275L // 3977.cs2, 3991.cs2
    const val PARAM_1276 = 1276L // 3977.cs2, 3991.cs2
    const val PARAM_1277 = 1277L // 3977.cs2, 3991.cs2
    const val PARAM_1278 = 1278L // 3977.cs2, 3991.cs2
    const val PARAM_1279 = 1279L // 3977.cs2, 3991.cs2
    const val PARAM_1280 = 1280L
    const val PARAM_1282 = 1282L // 3977.cs2, 3991.cs2
    const val PARAM_1283 = 1283L // 3977.cs2, 3991.cs2
    const val PARAM_1284 = 1284L // 3977.cs2, 3991.cs2
    const val PARAM_1285 = 1285L // 3977.cs2, 3991.cs2
    const val PARAM_1286 = 1286L // 3977.cs2, 3991.cs2
    const val PARAM_1290 = 1290L // 3995.cs2, 3996.cs2
    const val PARAM_1292 = 1292L // 3977.cs2, 3991.cs2
    const val PARAM_1293 = 1293L // 3971.cs2, 3989.cs2, 3991.cs2
    const val PARAM_1294 = 1294L // 3222.cs2, 3995.cs2
    const val PARAM_1295 = 1295L // 3222.cs2
    const val PARAM_1296 = 1296L // 3222.cs2
    const val PARAM_1297 = 1297L // 3222.cs2
    const val PARAM_1298 = 1298L // 3222.cs2
    const val PARAM_1299 = 1299L // 3222.cs2
    const val PARAM_1300 = 1300L // 3222.cs2
    const val PARAM_1301 = 1301L // 3222.cs2
    const val PARAM_1302 = 1302L // 3222.cs2
    const val PARAM_1303 = 1303L // 3222.cs2
    const val PARAM_1304 = 1304L // 3222.cs2
    const val PARAM_1305 = 1305L // 3222.cs2
    const val PARAM_1322 = 1322L // 5175.cs2
    const val SUMMONING_BEAST_OF_BURDEN = 1323L
    const val CHOMPY_BIRD_KILLS = 1366L // 4227.cs2
    const val CHOMPY_BIRD_HAT_NAME = 1367L
    const val CHOMPY_BIRD_REQUIREMENT_STRING = 1368L // 4227.cs2
    const val STAGE_ON_DEATH = 1397L // 59.cs2, 4592.cs2
    const val SHADOW = 1912L
    const val LINKED_SHADOW_NPC = 2098L
    const val CATEGORY = 2195L
    val names = mapOf(
        STAB_ATTACK to "stab",
        SLASH_ATTACK to "slash",
        CRUSH_ATTACK to "crush",
        MAGIC_ATTACK to "magic",
        RANGE_ATTACK to "range",
        STAB_DEFENCE to "stab_def",
        SLASH_DEFENCE to "slash_def",
        CRUSH_DEFENCE to "crush_def",
        MAGIC_DEFENCE to "magic_def",
        RANGE_DEFENCE to "range_def",
        PRAYER_BONUS to "prayer",
        STRENGTH to "str",
        RANGED_STRENGTH to "range_str",
        MAGIC_DAMAGE to "magic_damage",
        MAGIC_STRENGTH to "magic_str",
        ABSORB_MELEE to "absorb_melee",
        ABSORB_RANGE to "absorb_range",
        ABSORB_MAGIC to "absorb_magic",
        ATTACK_SPEED to "attack_speed",
        SPECIAL_ATTACK to "special_attack",
        RENDER_ANIMATION to "render_anim",
        SKILL_CAPE to "skillcape",
        TRIMMED_SKILL_CAPE to "skillcape_t",
        REQUIRED_COMBAT to "combat_req",
        WEAPON_STYLE to "weapon_style",
        EQUIP_SKILL_1 to "equip_req",
        USE_SKILL_1 to "skill_req",
        MAXED_SKILL to "max_skill",
        QUEST_REQUIREMENT_SLOT_ID to "quest_info",
        CATEGORY to "category",
        EQUIPPED_OPTION_1 to "equipped_options",
        REQUIRED_LEVEL to "secondary_use_level",
        UNBANKABLE to "unbankable",
    )

    object Category {
        const val THROWABLE = 1
        const val ARROW = 2
        const val BOLT = 3
        const val CONSTRUCTION = 4
        const val FURNITURE = 5
        const val UNCOOKED_FOOD = 6
        const val CONSTRUCTION_STORABLE_CLOTHES = 7
        const val CRAFTING = 8
        const val SUMMONING_POUCHES = 9
        const val CONSTRUCTION_PLANT = 10
        const val FLETCHING = 11
        const val EDIBLE = 12
        const val HERBLORE = 13
        const val HUNTER_REQUIRED_ITEM = 14
        const val HUNTER_REWARD = 15
        const val JEWELLERY = 16
        const val MAGIC_ARMOUR = 17
        const val MAGIC_WEAPON = 18
        const val MELEE_ARMOUR_LOW = 19
        const val MELEE_ARMOUR_MID = 20
        const val MELEE_ARMOUR_HIGH = 21
        const val MELEE_WEAPON_LOW = 22
        const val MELEE_WEAPON_MID = 23
        const val MELEE_WEAPON_HIGH = 24
        const val MINING_SMELTING = 25
        const val POTION = 26
        const val PRAYER_ARMOUR = 27
        const val PRAYER_CONSUMABLE = 28
        const val RANGE_ARMOUR = 29
        const val RANGE_WEAPON = 30
        const val RUNECRAFTING = 31
        const val TELEPORT = 32
        const val SEED = 33
        const val SUMMONING_SCROLL = 34
        const val ITEM_ON_ITEM = 35
        const val LOG = 36

        val names = mapOf(
            THROWABLE to "throwable",
            ARROW to "arrow",
            BOLT to "bolt",
            CONSTRUCTION to "construction",
            FURNITURE to "furniture",
            UNCOOKED_FOOD to "uncooked_food",
            CONSTRUCTION_STORABLE_CLOTHES to "construction_storable_clothes",
            CRAFTING to "crafting",
            SUMMONING_POUCHES to "summoning_pouch",
            CONSTRUCTION_PLANT to "construction_plant",
            FLETCHING to "fletching",
            EDIBLE to "edible",
            HERBLORE to "herblore",
            HUNTER_REQUIRED_ITEM to "hunter_required_item",
            HUNTER_REWARD to "hunter_reward",
            JEWELLERY to "jewellery",
            MAGIC_ARMOUR to "magic_armour",
            MAGIC_WEAPON to "magic_weapon",
            MELEE_ARMOUR_LOW to "melee_armour_low",
            MELEE_ARMOUR_MID to "melee_armour_mid",
            MELEE_ARMOUR_HIGH to "melee_armour_high",
            MELEE_WEAPON_LOW to "melee_weapon_low",
            MELEE_WEAPON_MID to "melee_weapon_mid",
            MELEE_WEAPON_HIGH to "melee_weapon_high",
            MINING_SMELTING to "mining_smelting",
            POTION to "potion",
            PRAYER_ARMOUR to "prayer_armour",
            PRAYER_CONSUMABLE to "prayer_consumable",
            RANGE_ARMOUR to "range_armour",
            RANGE_WEAPON to "range_weapon",
            RUNECRAFTING to "runecrafting",
            TELEPORT to "teleport",
            SEED to "seed",
            SUMMONING_SCROLL to "summoning_scroll",
            ITEM_ON_ITEM to "item_on_item",
            LOG to "log"
        )
    }

}