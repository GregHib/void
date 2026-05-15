package content.skill.summoning.pet

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.timedLoad

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

    fun load(): IncubatorDefinitions {
        require(Tables.loaded) { "Tables must be loaded before incubator definitions." }
        timedLoad("incubator egg definition") {
            for (row in Tables.get("incubator_eggs").rows()) {
                val def = IncubatorEgg(
                    id = row.rowId,
                    egg = row.item("egg"),
                    product = row.item("product"),
                    summoningLevel = row.int("summoning_level"),
                    incubationSeconds = row.int("incubation_seconds"),
                )
                byId[def.id] = def
                byEgg[def.egg] = def
            }
            byId.size
        }
        return this
    }
}
