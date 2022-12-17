package world.gregs.voidps.engine.entity.character.contain.stack

object AlwaysStack : ItemStackingRule {
    override fun stack(id: String): Boolean {
        return true
    }
}