package world.gregs.voidps.world.activity.skill.slayer

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.Bonus
import world.gregs.voidps.world.interact.entity.combat.hit.HitRatingModifier

on<HitRatingModifier>({ offense }, priority = Priority.HIGH) { player: Player ->
    rating = (rating * Bonus.slayer(player, target, type, false)).toInt()
}