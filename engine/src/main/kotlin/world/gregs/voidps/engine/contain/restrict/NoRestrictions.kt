package world.gregs.voidps.engine.contain.restrict

object NoRestrictions : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        return false
    }
}