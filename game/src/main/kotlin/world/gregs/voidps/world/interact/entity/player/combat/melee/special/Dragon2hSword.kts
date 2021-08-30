package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack

fun isDragon2hSword(item: Item?) = item != null && item.name.startsWith("dragon_2h_sword")

on<CombatSwing>({ !swung() && it.specialAttack && isDragon2hSword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 600)) {
        delay = -1
        return@on
    }
    player.setAnimation("powerstab")
    player.setGraphic("powerstab")
    val list = mutableListOf<Character>()
    list.add(target)
    val set = if (target is Player) player.viewport.players.current else player.viewport.npcs.current
    val groups = set.filter { it != target }.groupBy { it.tile }
    Direction.values.reversed().forEach { dir ->
        val tile = player.tile.add(dir)
        val group = groups[tile] ?: return@forEach
        list.addAll(group)
    }
    list.take(if (target is Player) 3 else 15).onEach {
        player.hit(it)
    }
    delay = 7
}