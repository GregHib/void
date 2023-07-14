package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext

class NPCAction(
    override val character: Character,
    name: String,
    priority: ActionPriority,
    delay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    override var onCancel: (() -> Unit)? = null,
    action: suspend NPCAction.() -> Unit = {}
) : Action(name, priority, delay, behaviour, action as suspend Action.() -> Unit), CharacterContext