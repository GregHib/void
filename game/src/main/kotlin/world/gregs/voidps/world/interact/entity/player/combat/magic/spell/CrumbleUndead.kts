package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.height
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "crumble_undead"

fun isUndead(category: String) = category == "shade" || category == "zombie" || category == "skeleton" || category == "ghost" || category == "zogre" || category == "ankou"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.HIGHEST) { player: Player ->
    if (target is NPC && !isUndead(target.def["category", ""])) {
        player.clearVar("autocast")
        player.message("This spell only affects skeletons, zombies, ghosts and shades")
        delay = -1
        return@on
    }
}

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("crumble_undead")
    player.setGraphic("crumble_undead_cast")
    player.shoot(name = player.spell, target = target, delay = 43, height = player.height + 4, endHeight = target.height + 4, curve = 16)
    player["spell_damage"] = 150.0
    player["spell_experience"] = 24.5
    player.hit(target)
    delay = 5
}