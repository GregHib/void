package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.utility.get

fun isMultiTargetSpell(spell: String) = spell.endsWith("_burst") || spell.endsWith("_barrage")

fun Player.drainSpell(target: Character, spell: String) {
    (target as? Player)?.message("You feel slightly weakened.", ChatType.GameFilter)
    val def = get<SpellDefinitions>().get(spell)
    val multiplier: Double = def["drain_multiplier"]
    val skill = Skill.valueOf(def["drain_skill"])
    val drained = target.levels.drain(skill, multiplier = multiplier, stack = target is Player)
    if (target.levels.get(skill) >= multiplier * 100 && drained == 0) {
        message("The spell has no effect because the target has already been weakened.")
    }
}