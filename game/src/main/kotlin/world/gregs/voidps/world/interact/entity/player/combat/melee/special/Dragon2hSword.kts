package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

val players: Players by inject()
val npcs: NPCs by inject()

specialAttack("powerstab") { player ->
    player.setAnimation(id)
    player.setGraphic(id)
    if (player.inMultiCombat) {
        val list = mutableListOf<Character>()
        list.add(target)
        val characters: CharacterList<*> = if (target is Player) players else npcs
        Direction.reversed.forEach { dir ->
            val tile = player.tile.add(dir)
            list.addAll(characters[tile])
        }
        list
            .filter { it.inMultiCombat && Target.attackable(player, it) }
            .take(if (target is Player) 3 else 15)
            .onEach {
                player.hit(it)
            }
    } else {
        player.hit(target)
    }
}