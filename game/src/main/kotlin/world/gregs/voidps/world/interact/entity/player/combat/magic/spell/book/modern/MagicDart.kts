package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.spellSwing
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

spellSwing("magic_dart", Priority.LOW) { player ->
    player.setAnimation("magic_dart")
    player.setGraphic("magic_dart_cast")
    player.shoot(id = player.spell, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.magicDelay(distance))
    delay = 5
}