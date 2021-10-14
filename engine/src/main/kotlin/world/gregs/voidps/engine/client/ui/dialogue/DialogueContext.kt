package world.gregs.voidps.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

data class DialogueContext(
    private val dialogues: Dialogues,
    val player: Player,
    val npcId: Int = -1,
    val npcName: String = ""
) {

    constructor(dialogues: Dialogues, player: Player, npc: NPC?) : this(dialogues, player, npc?.intId ?: -1, npc?.def?.name ?: "")

    var coroutine: CancellableContinuation<*>? = null
        private set
    var suspensionType: String? = null
        private set

    suspend fun <T> await(type: String): T {
        val result = suspendCancellableCoroutine<T> {
            suspensionType = type
            coroutine = it
            dialogues.add(this)
        }
        close()
        return result
    }

    fun close() {
        player.closeType("dialogue_box")
        player.closeType("dialogue_box_small")
    }

}