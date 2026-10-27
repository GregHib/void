package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object WeaponStyleLoader : Loader<WeaponStyleDefinitions> {
    val instance = WeaponStyleDefinitions()
    override val name = "weapon_styles"
    override val label = "weapon style definition"
    override val file = listOf("definitions.weapons.styles")

    override fun load(context: LoadContext): WeaponStyleDefinitions = WeaponStyleDefinitions().load(context.paths(this).single())

    override fun total(state: WeaponStyleDefinitions) = state.ids.size

    override fun install(state: WeaponStyleDefinitions) = instance.copyFrom(state)
}
