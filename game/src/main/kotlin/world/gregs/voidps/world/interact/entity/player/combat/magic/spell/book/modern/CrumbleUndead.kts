package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.activity.skill.slayer.undead
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

combatSwing(spell = "crumble_undead", type = "magic", priority = Priority.HIGHEST) { player ->
    if (target is NPC && !target.undead) {
        player.clear("autocast")
        player.message("This spell only affects skeletons, zombies, ghosts and shades")
        delay = -1
        return@combatSwing
    }
}

combatSwing(spell = "crumble_undead", type = "magic", priority = Priority.LOW) { player ->
    player.setAnimation("crumble_undead")
    player.setGraphic("crumble_undead_cast")
    player.shoot(id = player.spell, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.magicDelay(distance))
    delay = 5
}