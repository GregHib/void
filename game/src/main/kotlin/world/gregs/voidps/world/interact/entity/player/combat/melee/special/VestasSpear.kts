package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat

val players: Players by inject()
val npcs: NPCs by inject()

combatSwing("vestas_spear*", "melee", special = true) { player ->
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
            .filter { it.inMultiCombat && Target.attackable(player, it) }
            .take(16)
            .onEach {
                player.hit(it)
            }
    } else {
        player.hit(target)
    }
}
