package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.follow
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.event.PlayerOption
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on

on<PlayerOption>({ option == "Follow" }) { player: Player ->
    player.follow(target)
}

fun breakFollow(type: ActionType) = type == ActionType.Teleport || type == ActionType.Climb || type == ActionType.Logout || type == ActionType.Dying

on<ActionStarted>({ it.contains("followers") && breakFollow(type) }) { character: Character ->
    val followers: List<Character> = character["followers"]
    for (follower in followers) {
        follower.action.cancel()
    }
}