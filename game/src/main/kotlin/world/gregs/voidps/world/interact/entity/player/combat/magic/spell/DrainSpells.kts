package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.drainSpell
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isDrainSpell(spell: String) = spell == "confuse" || spell == "weaken" || spell == "curse" || spell == "vulnerability" || spell == "enfeeble" || spell == "stun"

on<CombatSwing>({ player -> !swung() && isDrainSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("${spell}${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("${spell}_cast")
    player.shoot(id = spell, target = target)
    if (player.hit(target) != -1) {
        player.drainSpell(target, spell)
    }
    delay = 5
}