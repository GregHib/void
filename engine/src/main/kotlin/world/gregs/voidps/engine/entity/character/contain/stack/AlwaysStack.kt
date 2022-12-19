package world.gregs.voidps.engine.entity.character.contain.stack

object AlwaysStack : ItemStackingRule {
    override fun stackable(id: String): Boolean {
        return true
    }
}