package world.gregs.voidps.engine.entity.character.contain.restrict

interface ItemRestrictionRule {
    fun restricted(id: String): Boolean
}