package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

data class ForceChat(var text: String = "") : Visual {
    override fun needsReset(): Boolean {
        return text.isNotEmpty()
    }

    override fun reset() {
        text = ""
    }
}