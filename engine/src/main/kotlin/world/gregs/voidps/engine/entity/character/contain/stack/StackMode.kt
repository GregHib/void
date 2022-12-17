package world.gregs.voidps.engine.entity.character.contain.stack

enum class StackMode {
    Always,
    Never,
    /**
     * Stack determined by item definition
     */
    Normal;
}