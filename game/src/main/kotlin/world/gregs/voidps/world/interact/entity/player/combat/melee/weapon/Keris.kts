package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import kotlin.math.floor

fun isKalphite(target: Character): Boolean = target is NPC && target.race == "kalphite"

on<HitDamageModifier>({ type == "melee" && weapon.id == "keris" && isKalphite(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * if (random.nextDouble() < 0.51) 3.0 else 1.0 + 1.0 / 3.0)
}