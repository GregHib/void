package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance

fun ItemDefinition.getInt(key: Long, default: Int): Int = params?.getOrDefault(key, default) as? Int ?: default

fun ItemDefinition.getString(key: Long, default: String): String = params?.getOrDefault(key, default) as? String ?: default

fun ItemDefinition.attackSpeed(): Int = getInt(14, 4)

fun ItemDefinition.has(key: Long): Boolean = params != null && params!!.containsKey(key)

fun ItemDefinition.requiredLevel(index: Int = 0): Int = getInt(750L + (index * 2), 1)

fun ItemDefinition.requiredSkill(index: Int = 0): Skill? = (params?.get(749L + (index * 2)) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.getMaxedSkill(): Skill? = (params?.get(277) as? Int)?.let { Skill.all[it] }

fun ItemDefinition.hasRequirements(): Boolean = params?.contains(750L) == true || params?.contains(277L) == true

fun Player.hasRequirements(item: ItemDefinition, message: Boolean = false): Boolean {
    for (i in 0 until 10) {
        val skill = item.requiredSkill(i) ?: break
        val level = item.requiredLevel(i)
        if (!has(skill, level, message)) {
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

fun ItemDefinition.specialAttack(): Int = getInt(687, 0)

fun ItemDefinition.hasSpecialAttack(): Boolean = getInt(687, 0) == 1

fun ItemDefinition.renderAnimationId(): Int = getInt(644, 1426)

fun ItemDefinition.isSkillCape(): Boolean = getInt(258, -1) == 1

fun ItemDefinition.isTrimmedSkillCape(): Boolean = getInt(259, -1) == 1

fun ItemDefinition.quest(): Int = getInt(743, -1)

fun ItemDefinition.requiredCombat(): Int = getInt(761, 0)

fun ItemDefinition.weaponStyle(): Int = getInt(686, 0)
