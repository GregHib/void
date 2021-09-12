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

fun isSpell(spell: String) = spell == "confuse"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOWER) { player: Player ->
    val staff = if (player.weapon.name.endsWith("staff")) "_staff" else ""
    player.setAnimation("confuse${staff}")
    player.setGraphic("confuse_cast", height = 100)
    player.shoot(name = player.spell, target = target, delay = 46, height = player.height + 3, endHeight = target.height, curve = 6)
    player["spell_damage"] = 0.0
    player["spell_experience"] = 13.0
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isSpell(spell) }) { character: Character ->
    (character as? Player)?.message("You feel slightly weakened.", ChatType.GameFilter)
    val drained = character.levels.drain(Skill.Attack, multiplier = 0.05, stack = character is Player)
    if (character.levels.get(Skill.Attack) >= 5 && drained == 0) {
        (source as? Player)?.message("The spell has no effect because the npc has already been weakened.")
    }
}