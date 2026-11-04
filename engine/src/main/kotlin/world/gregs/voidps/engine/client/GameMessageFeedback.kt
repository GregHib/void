package world.gregs.voidps.engine.client

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

/** Observe game feedback after its normal chat packet has been sent. */
object GameMessageFeedback : AutoCloseable {
    private val handlers = mutableListOf<(Player, String, ChatType) -> Unit>()
    init {
        Script.interfaces.add(this)
    }
    fun listen(handler: (Player, String, ChatType) -> Unit) {
        Script.checkLoading()
        handlers.add(handler)
    }

    @JvmStatic
    fun emit(player: Character, text: String, type: ChatType) {
        if (player !is Player) return
        for (handler in handlers) handler(player, text, type)
    }
    override fun close() {
        handlers.clear()
    }
}
