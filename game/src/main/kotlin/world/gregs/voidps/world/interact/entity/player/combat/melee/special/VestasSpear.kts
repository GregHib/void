package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isVestasSpear(item: Item?) = item != null && (item.id.startsWith("vestas_spear") || item.id.startsWith("corrupt_vestas_spear"))

val players: Players by inject()
val npcs: NPCs by inject()

on<HitChanceModifier>({ target != null && type == "melee" && target.hasClock("spear_wall") }, priority = Priority.MEDIUM) { _: Character ->
    chance = 0.0
}

on<CombatSwing>({ !swung() && it.specialAttack && isVestasSpear(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.start("spear_wall", duration = 8)
    player.setAnimation("spear_wall")
    player.setGraphic("spear_wall")
    if (player.inMultiCombat) {
        val list = mutableListOf<Character>()
        list.add(target)
        val characters: CharacterList<*> = if (target is Player) players else npcs
        for (tile in player.tile.spiral(1)) {
            list.addAll(characters[tile])
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