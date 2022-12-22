package world.gregs.voidps.engine.entity.character.contain.restrict

import world.gregs.voidps.engine.entity.character.contain.ContainerData

class ShopRestrictions(
    private val container: ContainerData
) : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        return container.items.indexOfFirst { it.id == id } == -1
    }
}