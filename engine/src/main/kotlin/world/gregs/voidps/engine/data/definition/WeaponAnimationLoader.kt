package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object WeaponAnimationLoader : Loader<WeaponAnimationDefinitions> {
    val instance = WeaponAnimationDefinitions()
    override val name = "weapon_animations"
    override val file = listOf("definitions.weapons.animations")

    override fun load(context: LoadContext): WeaponAnimationDefinitions = WeaponAnimationDefinitions().load(context.paths(this).single())

    override fun install(state: WeaponAnimationDefinitions) = instance.copyFrom(state)
}
