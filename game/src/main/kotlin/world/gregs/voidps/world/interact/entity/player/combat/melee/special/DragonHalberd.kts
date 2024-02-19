package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

val players: Players by inject()
val npcs: NPCs by inject()

specialAttackSwing("dragon_halberd") { player ->
    if (!drainSpecialEnergy(player, 300)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("sweep")
    player.setGraphic("sweep")
    val dir = target.tile.delta(player.tile).toDirection()
    val firstTile = target.tile.add(if (dir.isDiagonal()) dir.horizontal() else dir.rotate(2))
    val secondTile = target.tile.add(if (dir.isDiagonal()) dir.vertical() else dir.rotate(-2))
    val list = mutableListOf<Character>()
    list.add(target)
    val set = if (target is Player) players else npcs
    val groups = set.filter { it != target && it.tile.within(player.tile, VIEW_RADIUS) }.groupBy { it.tile }
    list.addAll(groups.getOrDefault(target.tile, emptyList()))
    list.addAll(groups.getOrDefault(firstTile, emptyList()))
    list.addAll(groups.getOrDefault(secondTile, emptyList()))
    list.take(if (target is Player) 3 else 10).onEach {
        player.hit(it)
    }
    if (target.size > 1) {
        player["second_hit"] = true
        player.hit(target)
        player.clear("second_hit")
    }
    delay = 7
}