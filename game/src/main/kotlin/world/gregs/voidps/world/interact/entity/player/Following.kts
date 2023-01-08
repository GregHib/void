package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.StopInteraction
import world.gregs.voidps.engine.entity.character.onApproach
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.getOrPut
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.suspend.approachRange

onApproach({ option == "Follow" }) { player: Player, target: Player ->
    player.approachRange(-1) ?: return@onApproach
    target.getOrPut("followers") { mutableListOf<Character>() }.add(player)
    player.watch(target)
    player.movement.queueStep(FollowTargetStrategy(target))
}

on<StopInteraction>({ it.interact.target?.contains("followers") == true}) { player: Player ->
    val followers: MutableList<Character>? = player.interact.target?.getOrNull("followers")
    followers?.remove(player)
}

fun breakFollow(type: ActionType) = type == ActionType.Teleport || type == ActionType.Climb || type == ActionType.Logout || type == ActionType.Dying

on<ActionStarted>({ it.contains("walk_followers") && breakFollow(type) }) { character: Character ->
    val followers: MutableList<Character> = character["walk_followers"]
    for (follower in followers) {
        follower.action.cancel()
    }
    followers.clear()
}