package world.gregs.voidps.engine.entity.character.contain.restrict

object NoRestrictions : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        return false
    }
}