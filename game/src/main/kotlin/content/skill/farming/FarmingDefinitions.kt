package content.skill.farming

import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad

class FarmingDefinitions {

    val diseaseChances = mutableMapOf<String, Int>()

    fun load(path: String): FarmingDefinitions {
        timedLoad("farming produce definition") {
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
            diseaseChances.size
        }
        return this
    }
}
