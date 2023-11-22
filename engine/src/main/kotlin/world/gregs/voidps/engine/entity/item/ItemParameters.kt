package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.item.ItemParameters.ATTACK_SPEED
import world.gregs.voidps.engine.entity.item.ItemParameters.EQUIP_LEVEL_1
import world.gregs.voidps.engine.entity.item.ItemParameters.EQUIP_SKILL_1
import world.gregs.voidps.engine.entity.item.ItemParameters.MAXED_SKILL
import world.gregs.voidps.engine.entity.item.ItemParameters.QUEST_REQUIREMENT_SLOT_ID
import world.gregs.voidps.engine.entity.item.ItemParameters.RENDER_ANIMATION
import world.gregs.voidps.engine.entity.item.ItemParameters.REQUIRED_COMBAT
import world.gregs.voidps.engine.entity.item.ItemParameters.SKILL_CAPE
import world.gregs.voidps.engine.entity.item.ItemParameters.SPECIAL_ATTACK
import world.gregs.voidps.engine.entity.item.ItemParameters.TRIMMED_SKILL_CAPE
import world.gregs.voidps.engine.entity.item.ItemParameters.USE_LEVEL_1
import world.gregs.voidps.engine.entity.item.ItemParameters.USE_SKILL_1
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
    const val SUMMONING_POUCH_LEVEL = 394L // 751.cs2
    const val SUMMONING_DEFENCE = 417L
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
    const val CHOMPY_BIRD_KILLS = 1366L // 4227.cs2
    const val CHOMPY_BIRD_HAT_NAME = 1367L
    const val CHOMPY_BIRD_REQUIREMENT_STRING = 1368L // 4227.cs2
    const val STAGE_ON_DEATH = 1397L // 59.cs2, 4592.cs2
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
}

fun ItemDefinition.getInt(key: Long, default: Int): Int = params?.getOrDefault(key, default) as? Int ?: default

fun ItemDefinition.getString(key: Long, default: String): String = params?.getOrDefault(key, default) as? String ?: default

fun ItemDefinition.attackSpeed(): Int = getInt(ATTACK_SPEED, 4)

fun ItemDefinition.has(key: Long): Boolean = params != null && params!!.containsKey(key)

fun ItemDefinition.requiredEquipLevel(index: Int = 0): Int = getInt(EQUIP_LEVEL_1 + (index * 2), 1)

fun ItemDefinition.requiredEquipSkill(index: Int = 0): Skill? = (params?.get(EQUIP_SKILL_1 + (index * 2)) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.requiredUseLevel(index: Int = 0): Int = getInt(USE_LEVEL_1 + (index * 2), 1)

fun ItemDefinition.requiredUseSkill(index: Int = 0): Skill? = (params?.get(USE_SKILL_1 + (index * 2)) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.getMaxedSkill(): Skill? = (params?.get(MAXED_SKILL) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.hasRequirements(): Boolean = params?.contains(EQUIP_LEVEL_1) == true || params?.contains(MAXED_SKILL) == true

fun Player.hasRequirements(item: Item, message: Boolean = false) = hasRequirements(item.def, message)

fun Player.hasRequirements(item: ItemDefinition, message: Boolean = false): Boolean {
    for (i in 0 until 10) {
        val skill = item.requiredEquipSkill(i) ?: break
        val level = item.requiredEquipLevel(i)
        if (if (skill == Skill.Prayer) !hasMax(skill, level, message) else !has(skill, level, message)) {
            return false
        }
    }
    item.getMaxedSkill()?.let { skill ->
        if (!has(skill, skill.maximum(), message)) {
            return false
        }
    }
    return appearance.combatLevel >= item.requiredCombat()
}

fun Player.hasUseRequirements(item: Item, message: Boolean = false) = hasUseRequirements(item.def, message)

fun Player.hasUseRequirements(item: ItemDefinition, message: Boolean = false): Boolean {
    for (i in 0 until 6) {
        val skill = item.requiredUseSkill(i) ?: break
        val level = item.requiredUseLevel(i)
        if (!has(skill, level, message)) {
            return false
        }
    }
    return true
}

fun ItemDefinition.specialAttack(): Int = getInt(SPECIAL_ATTACK, 0)

fun ItemDefinition.hasSpecialAttack(): Boolean = getInt(SPECIAL_ATTACK, 0) == 1

fun ItemDefinition.renderAnimationId(): Int = getInt(RENDER_ANIMATION, 1426)

fun ItemDefinition.isSkillCape(): Boolean = getInt(SKILL_CAPE, -1) == 1

fun ItemDefinition.isTrimmedSkillCape(): Boolean = getInt(TRIMMED_SKILL_CAPE, -1) == 1

fun ItemDefinition.quest(): Int = getInt(QUEST_REQUIREMENT_SLOT_ID, -1)

fun ItemDefinition.requiredCombat(): Int = getInt(REQUIRED_COMBAT, 0)

fun ItemDefinition.weaponStyle(): Int = getInt(WEAPON_STYLE, 0)

val ItemDefinition.slot: EquipSlot
    get() = this["slot", EquipSlot.None]

val Item.slot: EquipSlot
    get() = def.slot

val ItemDefinition.type: EquipType
    get() = this["type", EquipType.None]

val Item.type: EquipType
    get() = def.type
