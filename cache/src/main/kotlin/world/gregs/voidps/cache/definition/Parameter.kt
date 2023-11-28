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
    const val QUEST_LIST_ENUM = 61L // 2145.cs2, 2148.cs2, 2150.cs2, 2151.cs2, 2152.cs2, 2160.cs2, 2162.cs2, 2164.cs2
    const val WORLD_SPRITE_1 = 130L // 285.cs2, 292.cs2, 4.cs2
    const val WORLD_SPRITE_2 = 131L // 4.cs2
    const val WORLD_SPRITE_3 = 134L // 4.cs2
    const val WORLD_SPRITE_4 = 135L // 4.cs2
    const val QUEST_LIST_INTERFACE = 152L // 2160.cs2, 2162.cs2, 2164.cs2
    const val QUEST_LIST_SCROLL = 153L // 2160.cs2, 2162.cs2
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
    const val SPELLBOOK_ROOT_INTERFACE = 316L // 2059.cs2
    const val MOBILISING_ARMIES_ATTACKABLE_OBJ = 351L
    const val MOBILISING_ARMIES_COLLECTABLE_OBJ = 352L
    const val GRAVESTONE_TIME = 356L // 687.cs2
    const val SUMMONING_BEAST_OF_BURDEN_CAPACITY = 379L
    const val SUMMONING_POUCH_LEVEL = 394L // 751.cs2
    const val SUMMONING_DEFENCE = 417L
    const val SUMMONING_TIME_MINUTES = 424L
    const val SUMMONING_ITEM = 457L // 319.cs2, 322.cs2
    const val SIGNPOST = 457L
    const val MINI_MAP_ICON_CATEGORY = 477L // 1840.cs2
    const val MINI_MAP_ICON_IS_F2P = 478L // 1839.cs2
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
    const val CLAN_WARS_ARENA_NAME = 555L // 1799.cs2
    const val CLAN_WARS_ARENA_DESCRIPTION = 556L // 1799.cs2
    const val CLAN_WARS_ARENA_MEMBERS = 557L // 1781.cs2, 1799.cs2, 1833.cs2
    const val CLAN_WARS_VIEWING_ORB_REPLACEMENT_OBJECT = 580L
    const val CLAN_WARS_VIEWING_ORB_MODEL = 581L // 1769.cs2
    const val CLAN_WARS_VIEWING_ORB_ANIMATION = 582L // 1769.cs2
    const val CLAN_WARS_VIEWING_ORB_DISTANCE = 583L // 1769.cs2
    const val CLAN_WARS_VIEWING_ORB_ROTATE_X = 584L
    const val CLAN_WARS_VIEWING_ORB_ROTATE_Y = 585L
    const val CLAN_WARS_VIEWING_ORB_ROTATE_Z = 586L
    const val CLAN_WARS_VIEWING_ORB_POSITION_Y = 587L
    const val MINI_MAP_ICON_SPRITE = 595L // 1840.cs2
    const val MINI_MAP_ICON_NAME = 596L // 1840.cs2, 2010.cs2, 2689.cs2, 3788.cs2, 3789.cs2, 3790.cs2, 3791.cs2, 3792.cs2, 3793.cs2, 3794.cs2, 3795.cs2, 3796.cs2, 3797.cs2, 3798.cs2, 3799.cs2, 3800.cs2, 3801.cs2, 3802.cs2, 3803.cs2, 3804.cs2, 3805.cs2, 3806.cs2, 3807.cs2, 3808.cs2, 3809.cs2, 3810.cs2, 3811.cs2, 3812.cs2, 3813.cs2, 3814.cs2, 3815.cs2, 3816.cs2, 3817.cs2, 3818.cs2, 3819.cs2, 3820.cs2, 3821.cs2, 3822.cs2, 3823.cs2, 3824.cs2, 3825.cs2, 3826.cs2, 3827.cs2, 3828.cs2, 3829.cs2, 3830.cs2, 3831.cs2, 3832.cs2, 3833.cs2, 3834.cs2, 3835.cs2, 3836.cs2, 3837.cs2, 3838.cs2, 3839.cs2, 3840.cs2, 3841.cs2, 3842.cs2, 3843.cs2, 3844.cs2, 3845.cs2, 3846.cs2, 3847.cs2, 3852.cs2, 3853.cs2, 3854.cs2, 3855.cs2, 3856.cs2, 3857.cs2, 3858.cs2, 3859.cs2, 3860.cs2, 3861.cs2, 3862.cs2, 3863.cs2, 3864.cs2, 3865.cs2, 3866.cs2, 3867.cs2, 3871.cs2, 3873.cs2, 3874.cs2, 3875.cs2, 3876.cs2, 3877.cs2, 3878.cs2, 4576.cs2, 4577.cs2, 4578.cs2, 4579.cs2, 4751.cs2
    const val MINI_MAP_ICON_CHILDREN_COUNT = 597L // 1839.cs2, 1843.cs2
    const val SUMMONING_SCROLL = 599L // 322.cs2, 1670.cs2
    const val EQUIP_TYPE_HIDE_HAIR = 624L
    const val EQUIP_TYPE_HAT = 625L
    const val STRENGTH = 641L
    const val RANGED_STRENGTH = 643L
    const val RENDER_ANIMATION = 644L // 1608.cs2
    const val SPELLBOOK_Y = 654L // 1119.cs2, 2059.cs2
    const val SPELLBOOK_X = 655L // 1119.cs2, 2059.cs2
    const val SPELLBOOK_OFFSET_Y_1 = 656L // 1119.cs2
    const val SPELLBOOK_OFFSET_X_1 = 657L
    const val SPELLBOOK_OFFSET_X_2 = 658L // 1119.cs2, 2059.cs2
    const val SPELLBOOK_OFFSET_Y_2 = 659L // 1119.cs2, 2059.cs2
    const val SPELLBOOK_LINE_LENGTH = 660L // 1119.cs2, 2059.cs2
    const val SPELLBOOK_TOOLTIP_INTERFACE = 661L // 2060.cs2, 2061.cs2
    const val SPELLBOOK_FILTER_ENUM = 662L // 1119.cs2, 2059.cs2
    const val SPELLBOOK_SORT_ENUM = 663L // 2059.cs2
    const val SPELLBOOK_SORT_INDEX_ENUM = 664L // 2060.cs2, 2061.cs2
    const val SPELLBOOK_SORT_OPTION_ENUM = 665L // 2061.cs2
    const val QUEST_LIST_TITLE = 670L // 2162.cs2
    const val QUEST_LIST_OVERLAY_INTERFACE = 671L
    const val QUEST_LIST_SORTED_TITLE_ENUM = 673L // 2162.cs2
    const val QUEST_LIST_SORTED_TITLE_INDEX_ENUM = 675L // 2162.cs2
    const val QUEST_LIST_CHECK_EMPTY_ENUM = 676L // 2162.cs2
    const val QUEST_STAGES_2 = 677L // 2162.cs2
    const val QUEST_STAGES_4 = 678L // 2162.cs2
    const val GRAPHICS_OPTIONS_APPLICABLE = 682L // 3387.cs2
    const val GRAPHICS_OPTIONS_DROP_DOWN_ENUM = 683L // 2692.cs2, 2695.cs2, 3387.cs2
    const val SPELLBOOK_SCROLL_INTERFACE = 684L // 2059.cs2
    const val MAGIC_DAMAGE = 685L
    const val WEAPON_STYLE = 686L // 1142.cs2
    const val SPECIAL_ATTACK = 687L // 1136.cs2
    const val SPELLBOOK_SPELL_INV_INTERFACE = 688L // 2059.cs2
    const val HAND_CANNON_WARNING = 690L // 920.cs2
    const val QUEST_PARAM_691 = 691L
    const val QUEST_PARAM_692 = 693L
    const val QUEST_JOURNAL_HINTS = 694L // 2162.cs2
    const val QUEST_PARAM_695 = 695L
    const val QUEST_PARAM_696 = 696L
    const val SUMMONING_POUCH_REQ_ITEM_ID_1 = 697L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_AMOUNT_1 = 698L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_ID_2 = 699L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_AMOUNT_2 = 700L // 759.cs2, 766.cs2, 767.cs2
    const val PRAYER_TOOLTIP_TEXT = 734L // 1237.cs2
    const val PRAYER_SPRITE_ACTIVE = 735L // 1237.cs2, 2788.cs2
    const val PRAYER_SPRITE_INACTIVE = 736L // 1237.cs2
    const val PRAYER_INDEX = 737L // 1237.cs2, 2295.cs2
    const val PRAYER_REQUIREMENT_TEXT = 738L
    const val PRAYER_IS_MEMBERS = 739L // 2294.cs2
    const val UNLIT_BUG_LANTERN = 740L // 812.cs2, 920.cs2
    const val QUEST_ITEM_TYPE = 741L // 927.cs2, 933.cs2, 934.cs2
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
    const val QUEST_GROUP = 782L // 943.cs2, 945.cs2, 947.cs2
    const val BODY_LOOK_ID = 788L // 350.cs2, 391.cs2
    const val BODY_LOOK_INDEX = 789L
    const val BODY_LOOK_PARAM_790 = 790L
    const val BODY_LOOK_PARAM_791 = 791L
    const val BODY_LOOK_NAME = 792L // 2790.cs2, 391.cs2
    const val MOBILISING_ARMIES_CAVE = 799L
    const val MOBILISING_ARMIES_FISSURE = 800L
    const val MOBILISING_ARMIES_RESCUED = 801L
    const val MOBILISING_ARMIES_SQUAD = 802L // 2573.cs2
    const val MOBILISING_ARMIES_SQUAD_DEFEATED = 803L // 2570.cs2, 2573.cs2, 2597.cs2, 2599.cs2
    const val MOBILISING_ARMIES_SQUAD_TYPE = 805L // 2570.cs2, 2588.cs2
    const val MOBILISING_ARMIES_SQUAD_HEAVY = 806L // 2570.cs2, 2591.cs2
    const val MOBILISING_ARMIES_SQUAD_DEFEATED_ID = 811L
    const val BOOK_ITEM = 813L // 1692.cs2
    const val MOBILISING_ARMIES_SQUAD_ORIGINAL_ID = 814L
    const val MOBILISING_ARMIES_REWARD_CREDITS_COST = 821L
    const val BUG_LANTERN = 823L // 929.cs2
    const val INTERFACE_TEXT = 845L // 1692.cs2, 2145.cs2, 2160.cs2, 2163.cs2, 2180.cs2, 3222.cs2, 3387.cs2
    const val INTERFACE_TEXT_SORTABLE = 846L // 1693.cs2, 2163.cs2
    const val QUEST_ID = 847L // 2149.cs2
    const val QUEST_STAGES_3 = 848L // 2162.cs2
    const val QUEST_PARAM_850 = 850L
    const val QUEST_PARAM_851 = 851L
    const val QUEST_PARAM_853 = 853L
    const val QUEST_PARAM_854 = 854L
    const val QUEST_PARAM_855 = 855L
    const val QUEST_STAGES_1 = 856L // 2162.cs2
    const val QUEST_STAGE_1 = 859L // 2155.cs2
    const val QUEST_STAGE_2 = 860L // 2155.cs2
    const val QUEST_STAGE_3 = 861L // 2155.cs2
    const val QUEST_STAGE_4 = 862L // 2155.cs2
    const val QUEST_STAGE_5 = 863L // 2155.cs2
    const val QUEST_STAGE_6 = 864L // 2155.cs2
    const val QUEST_STAGE_7 = 865L // 2155.cs2
    const val QUEST_STAGE_8 = 866L // 2155.cs2
    const val QUEST_STAGE_9 = 867L // 2155.cs2
    const val QUEST_STAGE_10 = 868L // 2155.cs2
    const val QUEST_STAGE_11 = 869L // 2155.cs2
    const val QUEST_STAGE_12 = 870L // 2155.cs2
    const val QUEST_SKILL_1 = 871L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_1 = 872L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_2 = 873L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_2 = 874L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_3 = 875L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_3 = 876L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_4 = 877L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_4 = 878L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_5 = 879L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_5 = 880L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_6 = 881L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_6 = 882L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_7 = 883L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_7 = 884L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_8 = 885L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_8 = 886L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_9 = 887L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_9 = 888L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_10 = 889L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_10 = 890L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_11 = 891L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_11 = 892L // 2145.cs2, 2153.cs2
    const val QUEST_SKILL_12 = 893L // 2145.cs2, 2153.cs2
    const val QUEST_LEVEL_12 = 894L // 2145.cs2, 2153.cs2
    const val QUEST_POINTS_REQUIRED = 895L // 2149.cs2, 2150.cs2
    const val QUEST_COMBAT_LEVEL_REQ = 896L // 2149.cs2, 2151.cs2
    const val QUEST_ATT_STR_REQ = 897L // 2153.cs2
    const val QUEST_PARAM_898 = 898L // big quests=1, void dance=0
    const val BOOK_QUEST_STRUCT = 923L // 1692.cs2, 1693.cs2
    const val BOOK_NAME = 924L // 1692.cs2
    const val BOOK_DESCRIPTION = 925L // 1692.cs2
    const val NPC_CONTACT_NAME = 935L // 2791.cs2
    const val NPC_CONTACT_MODEL_GREY = 936L // 2791.cs2
    const val NPC_CONTACT_ROTATION = 937L // 2791.cs2
    const val NPC_CONTACT_MODEL = 938L // 2791.cs2
    const val NPC_CONTACT_ANIM = 939L // 2791.cs2
    const val NPC_CONTACT_OVERRIDE = 940L // 2791.cs2
    const val NPC_CONTACT_WHILE_GUTHIX_SLEEPS_TEXT = 941L // 2791.cs2
    const val NPC_CONTACT_WHILE_GUTHIX_SLEEPS_MODEL = 942L // 2791.cs2
    const val NPC_CONTACT_WHILE_GUTHIX_SLEEPS_ROTATION = 943L // 2791.cs2
    const val NPC_CONTACT_WHILE_GUTHIX_SLEEPS_ANIMATION = 944L // 2791.cs2
    const val QUEST_JOURNAL_HINT_TEXT = 948L
    const val QUEST_ITEM_REQUIREMENT_TEXT = 949L
    const val QUEST_LEVEL_REQUIREMENT_TEXT = 950L
    const val QUEST_REWARD_TEXT_0 = 951L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_QUEST_SPRITE = 952L // 3969.cs2, 3971.cs2, 3977.cs2, 3989.cs2, 3991.cs2, 4243.cs2
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
    const val DUNGEONEERING_REWARD_ITEM = 1070L // 2247.cs2, 2250.cs2
    const val DUNGEONEERING_REWARD_LEVEL = 1071L // 2247.cs2, 2250.cs2
    const val DUNGEONEERING_REWARD_TOKENS = 1072L // 2247.cs2, 2250.cs2
    const val DUNGEONEERING_REWARD_SKILL_1 = 1073L
    const val DUNGEONEERING_REWARD_LEVEL_1 = 1074L
    const val DUNGEONEERING_REWARD_SKILL_2 = 1075L
    const val DUNGEONEERING_REWARD_LEVEL_2 = 1076L
    const val DUNGEONEERING_REWARD_LEVEL_REQ_TEXT = 1077L
    const val DUNGEONEERING_REWARD_DESCRIPTION = 1078L // 2250.cs2
    const val DUNGEONEERING_CLASS_NAME = 1089L // 3494.cs2
    const val DUNGEONEERING_CLASS_DESCRIPTION = 1090L // 3494.cs2
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
    const val CONQUEST_COMMAND_INDEX = 1149L // 498.cs2
    const val CONQUEST_COMMAND_NAME = 1150L // 427.cs2, 432.cs2, 490.cs2, 493.cs2, 499.cs2
    const val CONQUEST_COMMAND_DESCRIPTION = 1151L // 432.cs2, 493.cs2
    const val CONQUEST_COMMAND_DESCRIPTION_SHORT = 1152L // 499.cs2
    const val CONQUEST_COMMAND_SPRITE = 1153L // 427.cs2, 490.cs2, 493.cs2, 498.cs2
    const val CONQUEST_COMMAND_POINTS = 1154L // 427.cs2, 429.cs2, 430.cs2, 432.cs2, 493.cs2
    const val CONQUEST_COMMAND_COOLDOWN = 1155L // 432.cs2, 493.cs2
    const val CONQUEST_COMMAND_PARAM_1156 = 1156L
    const val CONQUEST_COMMAND_CURSOR = 1157L // 431.cs2
    const val CONSTRUCTION_FLOOR_1 = 1158L
    const val CONSTRUCTION_FLOOR_2 = 1159L
    const val CHARACTER_CREATION_STYLE_NAME = 1160L // 387.cs2
    const val CHARACTER_CREATION_STYLE_SPRITE_MALE = 1161L // 387.cs2
    const val CHARACTER_CREATION_STYLE_SPRITE_FEMALE = 1162L // 387.cs2
    const val CHARACTER_CREATION_STYLE_INVENTORY = 1163L // 359.cs2
    const val CHARACTER_CREATION_STYLE_ANIMATION_MALE = 1164L // 392.cs2
    const val CHARACTER_CREATION_STYLE_ANIMATION_FEMALE = 1165L // 392.cs2
    const val CHARACTER_CREATION_STYLE_MODEL = 1166L // 392.cs2
    const val CHARACTER_CREATION_STYLE_BACKGROUND_ANIMATION_MALE = 1167L // 392.cs2
    const val CHARACTER_CREATION_STYLE_BACKGROUND_ANIMATION_FEMALE = 1168L // 392.cs2
    const val CHARACTER_CREATION_SUB_STYLE_MALE_0 = 1169L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_MALE_1 = 1170L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_MALE_2 = 1171L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_MALE_3 = 1172L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_MALE_4 = 1173L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_MALE_5 = 1174L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_FEMALE_0 = 1175L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_FEMALE_1 = 1176L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_FEMALE_2 = 1177L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_FEMALE_3 = 1178L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_FEMALE_4 = 1179L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_FEMALE_5 = 1180L // 384.cs2
    const val CHARACTER_CREATION_SUB_STYLE_SPRITE = 1181L // 390.cs2
    const val CHARACTER_STYLE_TOP = 1182L
    const val CHARACTER_STYLE_ARMS = 1183L
    const val CHARACTER_STYLE_WRISTS = 1184L
    const val CHARACTER_STYLE_LEGS = 1185L
    const val CHARACTER_STYLE_SHOES = 1186L
    const val CHARACTER_STYLE_TOP_COLOUR_0 = 1187L // 358.cs2
    const val CHARACTER_STYLE_LEGS_COLOUR_0 = 1188L // 358.cs2
    const val CHARACTER_STYLE_SHOES_COLOUR_0 = 1189L // 358.cs2, 360.cs2
    const val CHARACTER_STYLE_COLOUR_TOP_1 = 1190L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_LEGS_1 = 1191L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_SHOES_1 = 1192L // 358.cs2, 360.cs2
    const val CHARACTER_STYLE_COLOUR_TOP_2 = 1193L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_LEGS_2 = 1194L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_SHOES_2 = 1195L // 358.cs2, 360.cs2
    const val CHARACTER_STYLE_COLOUR_TOP_3 = 1196L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_LEGS_3 = 1197L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_SHOES_3 = 1198L // 358.cs2, 360.cs2
    const val CHARACTER_STYLE_COLOUR_TOP_4 = 1199L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_LEGS_4 = 1200L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_SHOES_4 = 1201L // 358.cs2, 360.cs2
    const val CHARACTER_STYLE_COLOUR_TOP_5 = 1202L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_LEGS_5 = 1203L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_SHOES_5 = 1204L // 358.cs2, 360.cs2
    const val CHARACTER_STYLE_COLOUR_TOP_6 = 1205L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_LEGS_6 = 1206L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_SHOES_6 = 1207L // 358.cs2, 360.cs2
    const val CHARACTER_STYLE_COLOUR_TOP_7 = 1208L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_LEGS_7 = 1209L // 358.cs2
    const val CHARACTER_STYLE_COLOUR_SHOES_7 = 1210L // 358.cs2, 360.cs2
    const val EXTRA_EQUIPMENT_OPTION = 1211L // 1612.cs2
    const val QUEST_REWARD_TEXT_1 = 1212L
    const val VOID_STARES_BACK_KEY_BLOCK_LABEL = 1225L
    const val VOID_STARES_BACK_KEY_BLOCK_WEIGHT = 1226L
    const val CONQUEST_UNIT_PARAM_15 = 1229L
    const val CONQUEST_UNIT_PARAM_16 = 1230L
    const val DYNAMIC_INVENTORY_OPTION_ORIGINAL = 1264L
    const val DYNAMIC_INVENTORY_OPTION_REPLACEMENT = 1265L // 1540.cs2
    const val ACHIEVEMENT_NAME = 1266L // 3969.cs2, 3971.cs2, 3977.cs2, 3979.cs2, 3988.cs2, 3989.cs2, 3991.cs2, 4000.cs2, 4243.cs2
    const val ACHIEVEMENT_NO_PIN = 1267L // 3977.cs2, 3989.cs2, 3991.cs2
    const val ACHIEVEMENT_COMPLETE_STAGE = 1268L // 3994.cs2
    const val ACHIEVEMENT_CAN_PIN = 1269L // 3989.cs2
    const val ACHIEVEMENT_QUEST_ID = 1270L // 3227.cs2, 3969.cs2, 3971.cs2, 3977.cs2, 3989.cs2, 3991.cs2, 4243.cs2
    const val ACHIEVEMENT_SPRITE = 1271L // 3969.cs2, 3971.cs2, 3977.cs2, 3988.cs2, 3989.cs2, 3991.cs2, 4243.cs2
    const val ACHIEVEMENT_STATUS_SPRITE = 1272L // 3971.cs2, 3977.cs2, 3989.cs2, 3991.cs2
    const val ACHIEVEMENT_DESCRIPTION = 1273L // 3971.cs2, 3977.cs2, 3979.cs2, 3988.cs2, 3989.cs2, 3991.cs2, 4243.cs2
    const val ACHIEVEMENT_INSTRUCTION_1 = 1274L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_INSTRUCTION_2 = 1275L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_INSTRUCTION_3 = 1276L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_INSTRUCTION_4 = 1277L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_INSTRUCTION_5 = 1278L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_INSTRUCTION_6 = 1279L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_TEXT_ROL = 1280L
    const val ACHIEVEMENT_SELECTABLE_1 = 1282L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_SELECTABLE_2 = 1283L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_SELECTABLE_3 = 1284L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_SELECTABLE_4 = 1285L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_SELECTABLE_5 = 1286L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_SELECTABLE_6 = 1287L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_MEMBERS = 1290L // 3995.cs2, 3996.cs2
    const val ACHIEVEMENT_GROUP_TEXT = 1292L // 3977.cs2, 3991.cs2
    const val ACHIEVEMENT_SPRITE_OFFSET = 1293L // 3971.cs2, 3989.cs2, 3991.cs2
    const val ACHIEVEMENT_SKILL_1 = 1294L // 3222.cs2, 3995.cs2
    const val ACHIEVEMENT_LEVEL_1 = 1295L // 3222.cs2
    const val ACHIEVEMENT_SKILL_2 = 1296L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_2 = 1297L // 3222.cs2
    const val ACHIEVEMENT_SKILL_3 = 1298L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_3 = 1299L // 3222.cs2
    const val ACHIEVEMENT_SKILL_4 = 1300L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_4 = 1301L // 3222.cs2
    const val ACHIEVEMENT_SKILL_5 = 1302L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_5 = 1303L // 3222.cs2
    const val ACHIEVEMENT_SKILL_6 = 1304L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_6 = 1305L // 3222.cs2
    const val ACHIEVEMENT_SKILL_7 = 1306L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_7 = 1307L // 3222.cs2
    const val ACHIEVEMENT_SKILL_8 = 1308L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_8 = 1309L // 3222.cs2
    const val ACHIEVEMENT_SKILL_9 = 1310L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_9 = 1311L // 3222.cs2
    const val ACHIEVEMENT_SKILL_10 = 1312L // 3222.cs2
    const val ACHIEVEMENT_LEVEL_10 = 1313L // 3222.cs2
    const val ACHIEVEMENT_OFFSET = 1322L // 5175.cs2
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