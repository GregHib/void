package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCContext

class NPCAction(
    override val npc: NPC,
    priority: ActionPriority,
    delay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    action: suspend NPCAction.() -> Unit = {}
) : Action(priority, delay, behaviour, action as suspend Action.() -> Unit), NPCContext