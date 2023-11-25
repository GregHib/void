package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.item.ItemParameters.MAXED_SKILL
import world.gregs.voidps.engine.entity.item.ItemParameters.QUEST_REQUIREMENT_SLOT_ID
import world.gregs.voidps.engine.entity.item.ItemParameters.SKILL_CAPE
import world.gregs.voidps.engine.entity.item.ItemParameters.SPECIAL_ATTACK
import world.gregs.voidps.engine.entity.item.ItemParameters.TRIMMED_SKILL_CAPE
import world.gregs.voidps.engine.entity.item.ItemParameters.WEAPON_STYLE
import world.gregs.voidps.network.visual.update.player.EquipSlot

object ItemParameters {
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
    const val SHOP_ITEM_LEVEL = 23L // 912.cs2
    const val UNBANKABLE = 59L
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
    const val GRAVESTONE_TIME = 356L // 687.cs2
    const val SUMMONING_BEAST_OF_BURDEN_CAPACITY = 379L
    const val SUMMONING_POUCH_LEVEL = 394L // 751.cs2
    const val SUMMONING_DEFENCE = 417L
    const val SUMMONING_TIME_MINUTES = 424L
    const val SUMMONING_ITEM = 457L // 319.cs2, 322.cs2
    const val WEAR_OPTION_1 = 528L // 1612.cs2
    const val WEAR_OPTION_2 = 529L // 1612.cs2
    const val WEAR_OPTION_3 = 530L // 1612.cs2
    const val WEAR_OPTION_4 = 531L // 1612.cs2
    const val SUMMONING_POUCH_ID = 538L // 767.cs2
    const val SUMMONING_POUCH_AMOUNT = 539L // 766.cs2, 767.cs2
    const val SUMMONING_SHARD_ID = 540L // 767.cs2
    const val SUMMONING_SHARD_AMOUNT = 541L // 759.cs2, 766.cs2, 767.cs2, 793.cs2
    const val SUMMONING_CHARM_ID = 542L // 767.cs2
    const val SUMMONING_CHARM_AMOUNT = 543L // 766.cs2, 767.cs2
    const val SUMMONING_SCROLL = 599L // 322.cs2, 1670.cs2
    const val EQUIP_TYPE_HIDE_HAIR = 624L
    const val EQUIP_TYPE_HAT = 625L
    const val STRENGTH = 641L
    const val RANGED_STRENGTH = 643L
    const val RENDER_ANIMATION = 644L // 1608.cs2
    const val MAGIC_DAMAGE = 685L
    const val WEAPON_STYLE = 686L // 1142.cs2
    const val SPECIAL_ATTACK = 687L // 1136.cs2
    const val HAND_CANNON_WARNING = 690L // 920.cs2
    const val SUMMONING_POUCH_REQ_ITEM_ID_1 = 697L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_AMOUNT_1 = 698L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_ID_2 = 699L // 759.cs2, 766.cs2, 767.cs2
    const val SUMMONING_POUCH_REQ_ITEM_AMOUNT_2 = 700L // 759.cs2, 766.cs2, 767.cs2
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
    const val MOBILISING_ARMIES_SQUAD = 802L // 2573.cs2
    const val MOBILISING_ARMIES_SQUAD_DEFEATED = 803L // 2570.cs2, 2573.cs2, 2597.cs2, 2599.cs2
    const val MOBILISING_ARMIES_SQUAD_TYPE = 805L // 2570.cs2, 2588.cs2
    const val MOBILISING_ARMIES_SQUAD_HEAVY = 806L // 2570.cs2, 2591.cs2
    const val MOBILISING_ARMIES_SQUAD_DEFEATED_ID = 811L
    const val MOBILISING_ARMIES_SQUAD_ORIGINAL_ID = 814L
    const val MOBILISING_ARMIES_REWARD_CREDITS_COST = 821L
    const val BUG_LANTERN = 823L // 929.cs2
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
    const val DUNGEONEERING_ITEM = 1047L // 912.cs2, 2246.cs2
    const val DUNGEONEERING_BOUND_ITEM = 1050L
    const val DUNGEONEERING_BOUND_AMMO = 1051L // 2246.cs2
    const val GOD_BOW_ID = 1091L
    const val GOD_BOW_AMOUNT = 1092L
    const val EXTRA_EQUIPMENT_OPTION = 1211L // 1612.cs2
    const val VOID_STARES_BACK_KEY_BLOCK_LABEL = 1225L
    const val VOID_STARES_BACK_KEY_BLOCK_WEIGHT = 1226L
    const val DYNAMIC_INVENTORY_OPTION_ORIGINAL = 1264L
    const val DYNAMIC_INVENTORY_OPTION_REPLACEMENT = 1265L // 1540.cs2
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
    const val CONQUEST_UNIT_PARAM_15 = 1229L
    const val CONQUEST_UNIT_PARAM_16 = 1230L
    const val SUMMONING_BEAST_OF_BURDEN = 1323L
    const val CHOMPY_BIRD_KILLS = 1366L // 4227.cs2
    const val CHOMPY_BIRD_HAT_NAME = 1367L
    const val CHOMPY_BIRD_REQUIREMENT_STRING = 1368L // 4227.cs2
    const val STAGE_ON_DEATH = 1397L // 59.cs2, 4592.cs2
    const val SHADOW = 1912L
    const val LINKED_SHADOW_NPC = 2098L
    const val CATEGORY = 2195L

    object Category {
        const val THROWABLE = 1
        const val ARROW = 2
        const val BOLT = 3
        const val CONSTRUCTION = 4
        const val FURNITURE = 5
        const val UNCOOKED_FOOD = 6
        const val CONSTRUCTION_STORABLE_CLOTHE = 7
        const val CRAFTING = 8
        const val SUMMONING_POUCHE = 9
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
    }

    val parameters = mapOf(
        STAB_ATTACK to "stab",
        SLASH_ATTACK to "slash",
        CRUSH_ATTACK to "crush",
        MAGIC_ATTACK to "magic",
        RANGE_ATTACK to "range",
        STAB_DEFENCE to "stab_def_2",
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
    )
}

fun ItemDefinition.getInt(key: Long, default: Int): Int = params?.getOrDefault(key, default) as? Int ?: default

fun ItemDefinition.has(key: Long): Boolean = params != null && params!!.containsKey(key)

fun ItemDefinition.getMaxedSkill(): Skill? = (params?.get(MAXED_SKILL) as? Int)?.let { Skill.all[it] }

fun Player.hasRequirements(item: Item, message: Boolean = false): Boolean {
    val requirements = item.def.getOrNull<Map<Skill, Int>>("equip_req")
    if (requirements != null) {
        for ((skill, level) in requirements) {
            if (if (skill == Skill.Prayer) !hasMax(skill, level, message) else !has(skill, level, message)) {
                return false
            }
        }
    }
    val skill = item.def.getOrNull<Skill>("max_skill")
    if (skill != null && !has(skill, skill.maximum(), message)) {
        return false
    }
    return appearance.combatLevel >= item.def["combat_req", 0]
}

fun Player.hasUseRequirements(item: Item, message: Boolean = false, skills: Set<Skill> = emptySet()): Boolean {
    val requirements = item.def.getOrNull<Map<Skill, Int>>("skill_req") ?: return true
    for ((skill, level) in requirements) {
        if ((skills.isEmpty() || skills.contains(skill)) && !has(skill, level, message)) {
            return false
        }
    }
    return true
}

fun ItemDefinition.specialAttack(): Int = getInt(SPECIAL_ATTACK, 0)

fun ItemDefinition.isSkillCape(): Boolean = getInt(SKILL_CAPE, -1) == 1

fun ItemDefinition.isTrimmedSkillCape(): Boolean = getInt(TRIMMED_SKILL_CAPE, -1) == 1

fun ItemDefinition.quest(): Int = getInt(QUEST_REQUIREMENT_SLOT_ID, -1)

fun ItemDefinition.weaponStyle(): Int = getInt(WEAPON_STYLE, 0)

val ItemDefinition.slot: EquipSlot
    get() = this["slot", EquipSlot.None]

val Item.slot: EquipSlot
    get() = def.slot

val ItemDefinition.type: EquipType
    get() = this["type", EquipType.None]

val Item.type: EquipType
    get() = def.type