import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Moved
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.move
import world.gregs.voidps.engine.utility.inject

val collisions: Collisions by inject()
val npcs: NPCs by inject()
val players: Players by inject()

on<Moved> { character: Character ->
    collisions.move(character, from, to)
}

on<Moved> { player: Player ->
    players.update(from, to, player)
}

on<Moved> { npc: NPC ->
    npcs.update(from, to, npc)
}