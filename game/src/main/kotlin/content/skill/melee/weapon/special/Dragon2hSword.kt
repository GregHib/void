package content.skill.melee.weapon.special

import content.area.wilderness.inMultiCombat
import content.entity.combat.Target
import content.entity.combat.hit.hit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.CharacterSearch
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction

class Dragon2hSword : Script {

    val players: Players by inject()
    val npcs: NPCs by inject()

    init {
        specialAttackDamage("powerstab") { target, damage ->
            if (!inMultiCombat || damage < 0) {
                return@specialAttackDamage
            }
            val characters: CharacterSearch<*> = if (target is Player) players else npcs
            var remaining = if (target is Player) 2 else 14
            for (direction in Direction.reversed) {
                val tile = tile.add(direction)
                for (char in characters[tile]) {
                    if (char == this || char == target || !char.inMultiCombat || !Target.attackable(this, char)) {
                        continue
                    }
                    hit(char)
                    if (--remaining <= 0) {
                        return@specialAttackDamage
                    }
                }
            }
        }
    }
}
