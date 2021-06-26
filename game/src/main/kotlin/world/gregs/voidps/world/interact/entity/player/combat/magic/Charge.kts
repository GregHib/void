package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.spell

fun isArenaSpell(spell: String): Boolean = spell == "saradomin_strike" || spell == "claws_of_guthix" || spell == "flames_of_zamorak"

on<HitDamageModifier>(
    { player -> type == "spell" && player.hasEffect("charge") && isArenaSpell(player.spell) },
    priority = Priority.HIGHEST
) { _: Player ->
    damage += 100.0
}