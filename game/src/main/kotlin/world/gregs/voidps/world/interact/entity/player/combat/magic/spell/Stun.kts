package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "stun"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("stun${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("stun_cast")
    player.shoot(name = player.spell, target = target, delay = 43)
    player["spell_damage"] = 50.0
    player["spell_experience"] = 90.0
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isSpell(spell) }) { character: Character ->
    (character as? Player)?.message("You feel slightly weakened.", ChatType.GameFilter)
    val drained = character.levels.drain(Skill.Attack, multiplier = 0.1, stack = character is Player)
    if (character.levels.get(Skill.Attack) >= 10 && drained == 0) {
        (source as? Player)?.message("The spell has no effect because the npc has already been weakened.")
    }
}