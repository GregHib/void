package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object QuestLoader : Loader<QuestDefinitions> {
    val instance = QuestDefinitions()
    override val name = "quests"
    override val file = listOf("definitions.quests")

    override fun load(context: LoadContext): QuestDefinitions = QuestDefinitions().load(context.paths(this).single())

    override fun install(state: QuestDefinitions) = instance.copyFrom(state)
}
