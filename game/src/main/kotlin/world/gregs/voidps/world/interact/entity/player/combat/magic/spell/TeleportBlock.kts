package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

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
    val def = definitions.getValue(player.spell)
    player["spell_damage"] = def.damage
    player["spell_experience"] = def.experience
    if (player.hit(target) != -1) {
        if (target.hasEffect("teleport_block")) {
            player.message("This player is already effected by this spell.", ChatType.GameFilter)
        } else if (!target.hasEffect("teleport_block")) {
            val protect = target.hasEffect("prayer_deflect_magic") || target.hasEffect("prayer_protect_from_magic")
            val duration: Int = def["block_ticks"]
            target.start("teleport_block", if (protect) duration / 2 else duration)
        }
    }
    delay = 5
}