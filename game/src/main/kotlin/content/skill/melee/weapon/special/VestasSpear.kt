package content.skill.melee.weapon.special

import content.area.wilderness.inMultiCombat
import content.entity.combat.Target
import content.entity.combat.hit.hit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.CharacterSearch
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.spiral

class VestasSpear(val players: Players, val npcs: NPCs) : Script {

    init {
        specialAttackDamage("spear_wall") { target, _ ->
            start("spear_wall", duration = 8)
            if (!inMultiCombat) {
                return@specialAttackDamage
            }
            var remaining = 15
            val characters: CharacterSearch<*> = if (target is Player) players else npcs
            for (tile in tile.spiral(1)) {
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
