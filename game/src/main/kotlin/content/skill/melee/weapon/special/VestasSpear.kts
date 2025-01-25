package content.skill.melee.weapon.special

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import content.entity.combat.Target
import content.entity.combat.hit.hit
import content.area.wilderness.inMultiCombat
import content.entity.player.combat.special.specialAttackHit

val players: Players by inject()
val npcs: NPCs by inject()

specialAttackHit("spear_wall", noHit = false) { player ->
    player.start(id, duration = 8)
    if (!player.inMultiCombat) {
        return@specialAttackHit
    }
    var remaining = 15
    val characters: CharacterList<*> = if (target is Player) players else npcs
    for (tile in player.tile.spiral(1)) {
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
