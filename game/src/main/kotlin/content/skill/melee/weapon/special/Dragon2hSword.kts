package content.skill.melee.weapon.special

import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import content.entity.combat.Target
import content.entity.combat.hit.hit
import content.area.wilderness.inMultiCombat
import content.entity.player.combat.special.specialAttackDamage

val players: Players by inject()
val npcs: NPCs by inject()

specialAttackDamage("powerstab") { player ->
    if (!player.inMultiCombat) {
        return@specialAttackDamage
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
                return@specialAttackDamage
            }
        }
    }
}