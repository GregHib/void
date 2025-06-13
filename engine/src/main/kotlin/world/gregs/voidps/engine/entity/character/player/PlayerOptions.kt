package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.network.login.protocol.encode.contextMenuOption

class PlayerOptions(
    private val player: Player,
) {

    private val options: Array<String> = arrayOf(
        "Walk here",
        EMPTY_OPTION,
        "Follow",
        EMPTY_OPTION,
        "Trade with",
        EMPTY_OPTION,
        EMPTY_OPTION,
        "Req Assist",
        EMPTY_OPTION,
    )

    fun set(slot: Int, option: String, top: Boolean = false): Boolean {
        if (slot <= 0 || slot >= OPTION_SIZE) {
            return false
        }
        if (has(slot)) {
            return false
        }
        options[slot] = option
        send(slot, top)
        return true
    }

    fun indexOf(option: String): Int = options.indexOf(option)

    fun get(slot: Int): String = options.getOrNull(slot) ?: EMPTY_OPTION

    fun has(slot: Int): Boolean = get(slot) != EMPTY_OPTION

    fun remove(slot: Int) {
        options[slot] = EMPTY_OPTION
        send(slot, false)
    }

    fun send(slot: Int, top: Boolean = false) {
        player.client?.contextMenuOption(options[slot], slot, top)
    }

    fun remove(option: String): Boolean {
        val slot = options.indexOf(option)
        if (slot == -1) {
            return false
        }
        remove(slot)
        return true
    }

    companion object {
        private const val OPTION_SIZE = 8
        const val EMPTY_OPTION = "null"
    }
}
