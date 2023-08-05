package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.flagMovementType
import world.gregs.voidps.engine.entity.character.player.flagTemporaryMoveType
import world.gregs.voidps.engine.entity.character.turn
import world.gregs.voidps.engine.event.on

on<Registered>({ !it.isBot }) { player: Player ->
    player.options.send(2)
    player.options.send(4)
    player.options.send(7)
    player.flagMovementType()
    player.flagTemporaryMoveType()
    player.flagAppearance()
    player.turn()
}