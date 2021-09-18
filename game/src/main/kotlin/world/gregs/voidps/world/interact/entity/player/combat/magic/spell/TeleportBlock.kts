package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "teleport_block"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    if (target is NPC) {
        delay = -1
        player.message("Nothing interesting happens.")
        return@on
    }
    player.setAnimation("teleport_block_cast")
    player.setGraphic("teleport_block_cast")
    player.shoot(name = player.spell, target = target)
    player["spell_damage"] = 30.0
    player["spell_experience"] = 80.0
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isSpell(spell) }) { character: Character ->
    if (character.hasEffect("teleport_block")) {
        (source as? Player)?.message("This player is already effected by this spell.", ChatType.GameFilter)
    } else if (!character.hasEffect("teleport_block")) {
        val protect = character.hasEffect("prayer_deflect_magic") || character.hasEffect("prayer_protect_from_magic")
        val duration = if (protect) 166 else 500
        character.start("teleport_block", duration)
    }
}