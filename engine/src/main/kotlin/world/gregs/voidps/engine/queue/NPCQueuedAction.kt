package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCContext

class NPCQueuedAction(
    override val npc: NPC,
    priority: ActionPriority,
    delay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    action: suspend NPCQueuedAction.() -> Unit = {}
) : QueuedAction(priority, delay, behaviour, action as suspend QueuedAction.() -> Unit), NPCContext