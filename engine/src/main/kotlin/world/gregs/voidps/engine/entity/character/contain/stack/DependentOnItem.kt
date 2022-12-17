package world.gregs.voidps.engine.entity.character.contain.stack

import world.gregs.voidps.engine.entity.definition.ItemDefinitions

class DependentOnItem(
    private val definitions: ItemDefinitions
) : ItemStackingRule {
    override fun stack(id: String): Boolean {
        return definitions.get(id).stackable == 1
    }
}