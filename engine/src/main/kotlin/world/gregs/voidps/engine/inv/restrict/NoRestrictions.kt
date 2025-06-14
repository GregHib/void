package world.gregs.voidps.engine.inv.restrict

object NoRestrictions : ItemRestrictionRule {
    override fun restricted(id: String): Boolean = false
}
