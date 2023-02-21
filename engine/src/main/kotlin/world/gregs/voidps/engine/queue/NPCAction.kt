package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCContext

class NPCAction(
    override val npc: NPC,
    name: String,
    priority: ActionPriority,
    delay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    override var onCancel: (() -> Unit)? = null,
    action: suspend NPCAction.() -> Unit = {}
) : Action(name, priority, delay, behaviour, action as suspend Action.() -> Unit), NPCContext