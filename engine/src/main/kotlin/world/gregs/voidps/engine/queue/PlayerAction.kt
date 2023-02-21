package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext

class PlayerAction(
    override val player: Player,
    name: String,
    priority: ActionPriority,
    delay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    override var onCancel: (() -> Unit)? = { player.clearAnimation() },
    action: suspend PlayerAction.() -> Unit = {}
) : Action(name, priority, delay, behaviour, action as suspend Action.() -> Unit), PlayerContext