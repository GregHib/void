package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object QuestLoader : Loader<QuestDefinitions> {
    val instance = QuestDefinitions()
    override val name = "quests"
    override val label = "quest definition"
    override val file = listOf("definitions.quests")

    override fun load(context: LoadContext): QuestDefinitions = QuestDefinitions().load(context.paths(this).single())

    override fun total(state: QuestDefinitions) = state.ids.size

    override fun install(state: QuestDefinitions) = instance.copyFrom(state)
}
