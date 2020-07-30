package rs.dusk.engine.model.entity.character.player

data class PlayerGameFrame(
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
        get() = if(resizable) GAME_FRAME_RESIZE_NAME else GAME_FRAME_NAME

    companion object {
        const val FIXED_SCREEN = 1
        const val RESIZABLE_SCREEN = 2
        const val FULL_SCREEN = 3

        const val GAME_FRAME_NAME = "toplevel"
        const val GAME_FRAME_RESIZE_NAME = "toplevel_full"
    }
}

fun Player.setDisplayMode(displayMode: Int): Boolean {
    val current = gameFrame.name
    if (interfaces.contains(current)) {
        gameFrame.displayMode = displayMode
        interfaces.remove(current)
        interfaces.open(gameFrame.name)
        interfaces.refresh()
        return true
    }
    return false
}