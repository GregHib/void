package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.characterSpellSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

characterSpellSwing("fire_*", Priority.LOW) { character ->
    character.setAnimation("fire_spell${if (character.weapon.def["category", ""] == "staff") "_staff" else ""}")
    character.setGraphic("fire_spell_cast")
    val spell = character.spell
    if (spell.endsWith("blast")) {
        character.shoot(id = "${character.spell}_1", target = target)
        character.shoot(id = "${character.spell}_2", target = target)
    } else if (spell.endsWith("wave") || spell.endsWith("surge")) {
        character.shoot(id = "${character.spell}_1", target = target, delay = 54, curve = 16)
        character.shoot(id = "${character.spell}_1", target = target, curve = -10)
        character.shoot(id = "${character.spell}_2", target = target)
    } else {
        character.shoot(id = character.spell, target = target)
    }
    val distance = character.tile.distanceTo(target)
    character.hit(target, delay = Hit.magicDelay(distance))
    delay = 5
}