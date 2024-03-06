package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackHit

val players: Players by inject()
val npcs: NPCs by inject()

specialAttackHit("powerstab") { player ->
    if (!player.inMultiCombat) {
        return@specialAttackHit
    }
    val characters: CharacterList<*> = if (target is Player) players else npcs
    var remaining = if (target is Player) 2 else 14
    for (direction in Direction.reversed) {
        val tile = player.tile.add(direction)
        for (char in characters[tile]) {
            if (char == player || char == target || !char.inMultiCombat || !Target.attackable(player, char)) {
                continue
            }
            player.hit(char)
            if (--remaining <= 0) {
                return@specialAttackHit
            }
        }
    }
}