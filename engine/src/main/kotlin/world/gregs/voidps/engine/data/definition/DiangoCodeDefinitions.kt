package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.DiangoCodeDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.entity.item.Item

class DiangoCodeDefinitions {

    private lateinit var definitions: Map<String, DiangoCodeDefinition>

    fun get(code: String) = getOrNull(code) ?: DiangoCodeDefinition.EMPTY

    fun getOrNull(code: String) = definitions[code]

    fun load(path: String): DiangoCodeDefinitions {
        val definitions = Object2ObjectOpenHashMap<String, DiangoCodeDefinition>(1, Hash.VERY_FAST_LOAD_FACTOR)
        Config.fileReader(path, 50) {
            while (nextSection()) {
                val stringId = section()
                var variable = ""
                val items = ObjectArrayList<Item>(2)
                while (nextPair()) {
                    val key = key()
                    when (key) {
                        "variable" -> variable = string()
                        "add" -> {
                            while (nextElement()) {
                                val id = string()
                                require(!ItemDefinitions.loaded || ItemDefinitions.contains(id)) { "Invalid diango item id: $id" }
                                items.add(Item(id))
                            }
                        }
                    }
                }
                definitions[stringId] = DiangoCodeDefinition(variable, items)
            }
        }
        this.definitions = definitions
        return this
    }

    internal fun copyFrom(other: DiangoCodeDefinitions) {
        definitions = other.definitions
    }
}

object DiangoCodeLoader : Loader<DiangoCodeDefinitions> {
    val instance = DiangoCodeDefinitions()
    override val name = "diango_codes"
    override val file = listOf("definitions.diangoCodes")
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader)

    override fun load(context: LoadContext): DiangoCodeDefinitions = DiangoCodeDefinitions().load(context.paths(this).single())

    override fun install(state: DiangoCodeDefinitions) = instance.copyFrom(state)
}
