package content.skill.farming

import world.gregs.config.Config
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

class FarmingDefinitions {

    val diseaseChances = mutableMapOf<String, Int>()

    fun load(path: String): FarmingDefinitions {
        Config.fileReader(path) {
            while (nextSection()) {
                val name = section()
                while (nextPair()) {
                    var diseaseChance = 0
                    when (key()) {
                        "disease_chance" -> diseaseChance = int()
                    }
                    diseaseChances[name] = diseaseChance
                }
            }
        }
        return this
    }

    fun copyFrom(other: FarmingDefinitions) {
        diseaseChances.clear()
        diseaseChances.putAll(other.diseaseChances)
    }
}

object FarmingDefinitionsLoader : Loader<FarmingDefinitions> {
    val instance = FarmingDefinitions()
    override val name = "farming"
    override val file = listOf("definitions.produce")

    override fun load(context: LoadContext): FarmingDefinitions = FarmingDefinitions().load(context.paths(this).single())

    override fun install(state: FarmingDefinitions) = instance.copyFrom(state)
}
