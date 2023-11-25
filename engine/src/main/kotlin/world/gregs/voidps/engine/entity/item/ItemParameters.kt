package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.Parameter.MAXED_SKILL
import world.gregs.voidps.cache.definition.Parameter.QUEST_REQUIREMENT_SLOT_ID
import world.gregs.voidps.cache.definition.Parameter.SKILL_CAPE
import world.gregs.voidps.cache.definition.Parameter.SPECIAL_ATTACK
import world.gregs.voidps.cache.definition.Parameter.TRIMMED_SKILL_CAPE
import world.gregs.voidps.cache.definition.Parameter.WEAPON_STYLE
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.network.visual.update.player.EquipSlot

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
