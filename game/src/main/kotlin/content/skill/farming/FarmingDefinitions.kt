package content.skill.farming

import world.gregs.config.Config

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
