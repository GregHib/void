package world.gregs.voidps.engine.entity.character.contain.stack

object NeverStack : ItemStackingRule {
    override fun stack(id: String): Boolean {
        return false
    }
}