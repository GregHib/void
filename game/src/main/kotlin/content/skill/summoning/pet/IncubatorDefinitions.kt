package content.skill.summoning.pet

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad

/** A single egg the player can place in an incubator. */
data class IncubatorEgg(
    val id: String,
    val egg: String,
    val product: String,
    val summoningLevel: Int,
    val incubationSeconds: Int,
)

class IncubatorDefinitions {

    private val byId = Object2ObjectOpenHashMap<String, IncubatorEgg>()
    private val byEgg = Object2ObjectOpenHashMap<String, IncubatorEgg>()

    val all: Collection<IncubatorEgg> get() = byId.values

    fun get(id: String): IncubatorEgg? = byId[id]
    fun forEgg(item: String): IncubatorEgg? = byEgg[item]

    fun load(path: String): IncubatorDefinitions {
        timedLoad("incubator egg definition") {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var egg = ""
                    var product = ""
                    var level = 0
                    var seconds = 0
                    while (nextPair()) {
                        when (key()) {
                            "egg" -> egg = string()
                            "product" -> product = string()
                            "summoning_level" -> level = int()
                            "incubation_seconds" -> seconds = int()
                        }
                    }
                    require(egg.isNotBlank() && product.isNotBlank()) {
                        "Incubator egg '$id' requires egg and product."
                    }
                    val def = IncubatorEgg(id, egg, product, level, seconds)
                    byId[id] = def
                    byEgg[egg] = def
                }
            }
            byId.size
        }
        return this
    }
}
