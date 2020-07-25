package rs.dusk.engine.client.ui

import rs.dusk.engine.model.entity.character.player.Player

data class GameFrame(
    var displayMode: Int = 0,
    var width: Int = 0,
    var height: Int = 0
) {
    var resizable: Boolean
        get() = displayMode == RESIZABLE_SCREEN || displayMode == FULL_SCREEN
        set(value) {
            displayMode = if (value) RESIZABLE_SCREEN else FIXED_SCREEN
        }

    val name: String
        get() = if(resizable) "toplevel_full" else "toplevel"

    companion object {
        const val FIXED_SCREEN = 1
        const val RESIZABLE_SCREEN = 2
        const val FULL_SCREEN = 3
    }
}


fun Player.setDisplayMode(displayMode: Int): Boolean {
    val current = gameframe.name
    if (interfaces.contains(current)) {
        gameframe.displayMode = displayMode
        interfaces.remove(current)
        interfaces.open(gameframe.name)
        interfaces.refresh()
        return true
    }
    return false
}