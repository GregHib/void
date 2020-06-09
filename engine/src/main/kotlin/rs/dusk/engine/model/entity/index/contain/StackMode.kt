package rs.dusk.engine.model.entity.index.contain

sealed class StackMode {
    object Always : StackMode()
    object Never : StackMode()
    /**
     * Stack determined by item definition
     */
    object Normal : StackMode()
}