package rs.dusk.engine.entity.character.contain

sealed class StackMode {
    object Always : StackMode()
    object Never : StackMode()
    /**
     * Stack determined by item definition
     */
    object Normal : StackMode()
}