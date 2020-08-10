package rs.dusk.engine.entity.character.contain

enum class StackMode {
    Always,
    Never,
    /**
     * Stack determined by item definition
     */
    Normal;
}