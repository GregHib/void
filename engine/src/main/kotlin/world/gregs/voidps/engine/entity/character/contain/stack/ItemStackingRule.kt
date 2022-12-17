package world.gregs.voidps.engine.entity.character.contain.stack

interface ItemStackingRule {
    fun stack(id: String): Boolean
}