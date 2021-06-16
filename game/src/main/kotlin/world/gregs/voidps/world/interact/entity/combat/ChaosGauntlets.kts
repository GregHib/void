import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.spell

on<HitDamageModifier>({ type == "spell" && it.spell.endsWith("_bolt") }, Priority.LOWEST) { player: Player ->
    damage += 30.0
}