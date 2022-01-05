package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.entity.character.move.follow
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.on

on<PlayerOption>({ option == "Follow" }) { player: Player ->
    player.follow(target)
}