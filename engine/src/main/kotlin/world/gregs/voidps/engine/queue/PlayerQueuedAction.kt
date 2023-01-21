package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext

class PlayerQueuedAction(
    override val player: Player,
    priority: ActionPriority,
    delay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    action: suspend PlayerQueuedAction.() -> Unit = {}
) : QueuedAction(priority, delay, behaviour, action as suspend QueuedAction.() -> Unit), PlayerContext