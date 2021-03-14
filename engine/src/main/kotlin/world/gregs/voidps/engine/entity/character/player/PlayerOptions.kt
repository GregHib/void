package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.network.encode.contextMenuOption

class PlayerOptions(
    private val player: Player
) {

    private val options: Array<String> = Array(OPTION_SIZE) {
        if (it == 0) "Walk here" else EMPTY_OPTION
    }

    fun set(slot: Int, option: String, top: Boolean = false): Boolean {
        if (slot <= 0 || slot >= OPTION_SIZE) {
            return false
        }
        if (has(slot)) {
            return false
        }
        options[slot] = option
        update(slot, top)
        return true
    }

    fun get(slot: Int): String {
        return options[slot]
    }

    fun has(slot: Int): Boolean {
        return get(slot) != EMPTY_OPTION
    }

    fun remove(slot: Int) {
        options[slot] = EMPTY_OPTION
        update(slot, false)
    }

    private fun update(slot: Int, top: Boolean) {
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