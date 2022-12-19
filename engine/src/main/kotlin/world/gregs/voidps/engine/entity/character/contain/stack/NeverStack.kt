package world.gregs.voidps.engine.entity.character.contain.stack

object NeverStack : ItemStackingRule {
    override fun stackable(id: String): Boolean {
        return false
    }
}