import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.move.NPCMoved
import world.gregs.voidps.engine.entity.character.move.PlayerMoved
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerMoveType
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.player.*
import world.gregs.voidps.engine.event.priority
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.utility.inject

val players: Players by inject()
val npcs: NPCs by inject()

Registered priority 9 then {
    when (entity) {
        is Player -> {
            val entity = entity as Player
            players.add(entity)
            entity.viewport.players.add(entity)
            entity.temporaryMoveType = PlayerMoveType.None
            entity.movementType = PlayerMoveType.None
            entity.flagMovementType()
            entity.flagTemporaryMoveType()
            entity.face()
        }
    }
}

PlayerMoved priority 9 then {
    players.update(from, to, player)
}

NPCMoved priority 9 then {
    npcs.update(from, to, npc)
}