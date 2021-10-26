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
import world.gregs.voidps.world.interact.entity.proj.shoot

on<CombatSwing>({ player -> !swung() && player.spell.startsWith("fire_") }, Priority.LOW) { player: Player ->
    player.setAnimation("fire_spell${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("fire_spell_cast")
    val spell = player.spell
    if (spell.endsWith("blast")) {
        player.shoot(id = "${player.spell}_1", target = target)
        player.shoot(id = "${player.spell}_2", target = target)
    } else if (spell.endsWith("wave") || spell.endsWith("surge")) {
        player.shoot(id = "${player.spell}_1", target = target, delay = 54, curve = 16)
        player.shoot(id = "${player.spell}_1", target = target, curve = -10)
        player.shoot(id = "${player.spell}_2", target = target)
    } else {
        player.shoot(id = player.spell, target = target)
    }
    player.hit(target)
    delay = 5
}