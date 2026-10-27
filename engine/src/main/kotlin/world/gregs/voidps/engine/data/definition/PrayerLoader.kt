package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object PrayerLoader : Loader<PrayerDefinitions> {
    val instance = PrayerDefinitions()
    override val name = "prayers"
    override val file = listOf("definitions.prayers")

    override fun load(context: LoadContext): PrayerDefinitions = PrayerDefinitions().load(context.paths(this).single())

    override fun install(state: PrayerDefinitions) = instance.copyFrom(state)
}
