package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object MidiLoader : Loader<MidiDefinitions> {
    val instance = MidiDefinitions()
    override val name = "midis"
    override val label = "midi definition"
    override val files = listOf("definitions.midis")

    override fun load(context: LoadContext): MidiDefinitions = MidiDefinitions().load(context.paths(this))

    override fun total(state: MidiDefinitions) = state.ids.size

    override fun install(state: MidiDefinitions) = instance.copyFrom(state)
}
