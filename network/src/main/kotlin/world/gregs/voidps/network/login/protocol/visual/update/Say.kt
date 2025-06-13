package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

data class Say(var text: String = "") : Visual {
    override fun needsReset(): Boolean = text.isNotEmpty()

    override fun reset() {
        text = ""
    }
}
