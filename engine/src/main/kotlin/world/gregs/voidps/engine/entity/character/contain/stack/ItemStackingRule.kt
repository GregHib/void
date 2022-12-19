package world.gregs.voidps.engine.entity.character.contain.stack

interface ItemStackingRule {
    fun stackable(id: String): Boolean
}