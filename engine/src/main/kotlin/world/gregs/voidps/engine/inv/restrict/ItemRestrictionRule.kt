package world.gregs.voidps.engine.inv.restrict

interface ItemRestrictionRule {
    fun restricted(id: String): Boolean
}