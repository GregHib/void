package rs.dusk.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player

data class DialogueContext(
    private val dialogues: Dialogues,
    val player: Player,
    val npcId: Int = -1,
    val npcName: String = ""
) {

    constructor(dialogues: Dialogues, player: Player, npc: NPC?) : this(dialogues, player, npc?.id ?: -1, npc?.def?.name ?: "")

    var coroutine: CancellableContinuation<*>? = null
        private set
    var suspensionType: String? = null
        private set

    suspend fun <T> await(type: String) = suspendCancellableCoroutine<T> {
        suspensionType = type
        coroutine = it
        dialogues.add(this)
    }

}