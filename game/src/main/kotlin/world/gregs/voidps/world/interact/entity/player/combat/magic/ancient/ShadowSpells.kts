package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.isMultiTargetSpell
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

fun isSpell(spell: String) = spell.startsWith("shadow_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (isMultiTargetSpell(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    if (player.hit(target) != -1) {
        (target as? Player)?.message("You feel slightly weakened.", ChatType.GameFilter)
        val def = definitions.get(player.spell)
        val multiplier: Double = def["drain_multiplier"]
        val skill = Skill.valueOf(def["drain_skill"])
        val drained = target.levels.drain(skill, multiplier = multiplier, stack = target is Player)
        if (target.levels.get(skill) >= multiplier * 100 && drained == 0) {
            player.message("The spell has no effect because the npc has already been weakened.")
        }
    }
    delay = 5
}