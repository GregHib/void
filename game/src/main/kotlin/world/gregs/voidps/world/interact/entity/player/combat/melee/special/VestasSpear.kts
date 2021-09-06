package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.update.task.viewport.Spiral
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isVestasSpear(item: Item?) = item != null && (item.name.startsWith("vestas_spear") || item.name.startsWith("corrupt_vestas_spear"))

val players: Players by inject()
val npcs: NPCs by inject()

on<HitChanceModifier>({ target != null && type == "melee" && target.hasEffect("spear_wall") }, priority = Priority.MEDIUM) { character: Character ->
    chance = 0.0
}

on<CombatSwing>({ !swung() && it.specialAttack && isVestasSpear(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.start("spear_wall", ticks = 8)
    player.setAnimation("spear_wall")
    player.setGraphic("spear_wall")
    if (player.inMultiCombat) {
        val list = mutableListOf<Character>()
        list.add(target)
        val characters: PooledMapList<out Character> = if (target is Player) players else npcs
        Spiral.spiral(player.tile, 1) { tile ->
            list.addAll(characters[tile]?.filterNotNull() ?: return@spiral)
        }
        list
            .filter { it.inMultiCombat && canAttack(player, it) }
            .take(16)
            .onEach {
                player.hit(it)
            }
    } else {
        player.hit(target)
    }
    delay = 5
}