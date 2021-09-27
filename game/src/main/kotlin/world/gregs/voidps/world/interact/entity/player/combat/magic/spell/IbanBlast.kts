package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

on<CombatSwing>({ player -> !swung() && player.spell == "iban_blast" }, Priority.LOW) { player: Player ->
    player.setAnimation("iban_blast")
    player.setGraphic("iban_blast_cast")
    player.shoot(name = player.spell, target = target)
    player.hit(target)
    delay = 5
}