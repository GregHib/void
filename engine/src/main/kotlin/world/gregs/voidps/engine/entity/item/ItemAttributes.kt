package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.network.visual.update.player.EquipSlot

enum class Opcodes(val id: Int) {
    StabAttack(0), // 606.csv
    SlashAttack(1),
    CrushAttack(2),
    MagicAttack(3),
    RangeAttack(4),
    StabDefence(5),
    SlashDefence(6),
    CrushDefence(7),
    MagicDefence(8),
    RangeDefence(9),
    PrayerBonus(11),
    AttackSpeed(14),
    //    23 - 912.cs2 - alch price?
    StealingCreation(59),
    //    211..22 - 1864.cs2
    SkillCape(258),
    TrimmedSkillCape(259), // 2720.cs2, 2723.cs2
    MaxedSkill(277),
    //    358 - 2472.cs2
    //    359 - 2472.cs2
    SummongPouchLevel(394), // 751.cs2
    SummoningDefence(417),
    //    457 - 319.cs2, 322.cs2
    WearOptions(528), // 528-531 1612.cs2
    SummoningPouchId(538), // 767.cs2
    SummoningPouchAmount(539), // 766.cs2, 767.cs2
    SpiritShardId(540), // 767.cs2
    SpiritShardAmount(541), //759.cs2, 766.cs2, 767.cs2, 793.cs2
    CharmId(542), // 767.cs2
    CharmAmount(543), // 766.cs2, 767.cs2
    //    599 - 1670.cs2, 322.cs2 summoning scrolls
    //    625 - hats/masks appearance?
    StrengthBonus(641),
    RangedStrength(643),
    RenderAnim(644), // 1608.cs2
    RangeDamage(643),
    MagicDamage(685),
    WeaponStyle(686), // 1142.csv
    SpecialAttack(687), // 1136.csv
    HandCannonWarning(690), // 920.cs2
    // Summoning pouch item creation
    //    697 - 759.cs2, 766.cs2, 767.cs2
    //    698 - 759.cs2, 766.cs2, 767.cs2
    //    699 - 759.cs2, 766.cs2, 767.cs2
    //    700 - 759.cs2, 766.cs2, 767.cs2
    //    701 - 759.cs2, 766.cs2, 767.cs2
    //    702 - 759.cs2, 766.cs2, 767.cs2
    //    703 - 759.cs2, 766.cs2, 767.cs2
    //    704 - 759.cs2, 766.cs2, 767.cs2
    //    705 - 759.cs2, 766.cs2, 767.cs2
    //    706 - 759.cs2, 766.cs2, 767.cs2
    //    707 - 759.cs2, 766.cs2, 767.cs2
    //    708 - 759.cs2, 766.cs2, 767.cs2

    //    740 - 812.cs2, 920.cs2 - unlit bug lantern
    //    741 - 927.cs2, 933.cs2, 934.cs2 - cooking utensils
    //    742 - 933.cs2, 934.cs2 - cooking utensil index
    //    744 - 930.cs2
    //    743 - quest 927.cs2, 930.cs2
    //    745 - 930.cs2
    //    746 - 930.cs2
    //    747 - 930.cs2
    //    748 - 930.cs2
    RequiredCombat(761),
    //    749..767 - skill 927.cs2, 929.cs2, 925.cs2, 928.cs2, 935.cs2, 936.cs2, 932.cs2
    //    750..778 - skill level req 929.cs2, 925.cs2, 928.cs2, 935.cs2, 936.cs2, 932.cs2
    //    770..780 - required use skill 931.cs2
    //    771..781 required use level 931.cs2
    //    802 - 2573.cs2
    //    803 - 2570.cs2, 2573.cs2, 2597.cs2, 2599.cs2
    //    805 - 2570.cs2, 2588.cs2
    //    806 - 2570.cs2, 2591.cs2
    //    823 - 929.cs2
    QuestId(861),
    MagicDamage2(965),
    AbsorbMelee(967),
    AbsorbMagic(969),
    AbsorbRange(968),
    InfiniteAirRunes(972),
    InfiniteWaterRunes(973),
    InfiniteEarthRunes(974),
    InfiniteFireRunes(975),
    DungeoneeringShopMultiplier(1046), // 2262.cs2
    DungeoneeringItem(1047), // 2246.cs2, 912.cs2
    //    1051 - 2246.cs2
    //    1211 - 1612.cs2 - new equipment option
    //    1264 - original inventory option
    //    1265 - 1540.cs2 - replacement inventory option
    Health(1326),
    //    1366 - 4227.cs2 - chompy bird hat points
    //    1367 - chompy bird alternate names
    //    1368 - 4227.cs2 - Colour and feather requirement string
    StageOnDeath(1397), // 4592.cs2, 59.cs2
    //    1429 - 5359.cs2
    //    1430 - 1612.cs2, 5359.cs2
}

fun ItemDefinition.getInt(key: Long, default: Int): Int = params?.getOrDefault(key, default) as? Int ?: default

fun ItemDefinition.getString(key: Long, default: String): String = params?.getOrDefault(key, default) as? String ?: default

fun ItemDefinition.attackSpeed(): Int = getInt(14, 4)

fun ItemDefinition.has(key: Long): Boolean = params != null && params!!.containsKey(key)

fun ItemDefinition.requiredEquipLevel(index: Int = 0): Int = getInt(750L + (index * 2), 1)

fun ItemDefinition.requiredEquipSkill(index: Int = 0): Skill? = (params?.get(749L + (index * 2)) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.requiredUseLevel(index: Int = 0): Int = getInt(771L + (index * 2), 1)

fun ItemDefinition.requiredUseSkill(index: Int = 0): Skill? = (params?.get(770L + (index * 2)) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.getMaxedSkill(): Skill? = (params?.get(277) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.hasRequirements(): Boolean = params?.contains(750L) == true || params?.contains(277L) == true

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
    if (appearance.combatLevel < item.requiredCombat()) {
        return false
    }
    return true
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

fun ItemDefinition.specialAttack(): Int = getInt(687, 0)

fun ItemDefinition.hasSpecialAttack(): Boolean = getInt(687, 0) == 1

fun ItemDefinition.renderAnimationId(): Int = getInt(644, 1426)

fun ItemDefinition.isSkillCape(): Boolean = getInt(258, -1) == 1

fun ItemDefinition.isTrimmedSkillCape(): Boolean = getInt(259, -1) == 1

fun ItemDefinition.quest(): Int = getInt(743, -1)

fun ItemDefinition.requiredCombat(): Int = getInt(761, 0)

fun ItemDefinition.weaponStyle(): Int = getInt(686, 0)

val ItemDefinition.slot: EquipSlot
    get() = this["slot", EquipSlot.None]

val Item.slot: EquipSlot
    get() = def.slot

val ItemDefinition.type: EquipType
    get() = this["type", EquipType.None]

val Item.type: EquipType
    get() = def.type
