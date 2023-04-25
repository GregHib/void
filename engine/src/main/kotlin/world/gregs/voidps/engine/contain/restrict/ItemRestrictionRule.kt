package world.gregs.voidps.engine.contain.restrict

interface ItemRestrictionRule {
    fun restricted(id: String): Boolean
}