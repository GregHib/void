package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.data.MiningRock
import world.gregs.voidps.engine.entity.definition.data.Tree
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ObjectDefinitions(
    override val decoder: ObjectDecoder
) : DefinitionsDecoder<ObjectDefinition, ObjectDecoder>() {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    private lateinit var ids: Map<String, Int>
    private var all = arrayOfNulls<ObjectDefinition>(size + 1)
    private val blank = ObjectDefinition(stringId = "-1")

    override fun getOrNull(id: Int): ObjectDefinition? = all[id]

    override fun getOrNull(id: String): ObjectDefinition? {
        return getOrNull(ids[id] ?: return null)
    }

    override fun get(id: Int): ObjectDefinition {
        return super.getOrNull(id) ?: blank
    }

    override fun get(id: String): ObjectDefinition {
        return super.getOrNull(id) ?: blank
    }

    init {
        modifications.map("woodcutting") { Tree(it) }
        modifications.map("mining") { MiningRock(it) }
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("objectDefinitionsPath")): ObjectDefinitions {
        timedLoad("object definition") {
            decoder.clear()
            val size = load(storage.load<Map<String, Any>>(path).mapIds())
            for (i in indices) {
                all[i] = super.getOrNull(i)
            }
            size
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        extras = data.mapModifications()
        names = extras.map { it.value["id"] as Int to it.key }.toMap()
        ids = extras.map { it.key to it.value["id"] as Int }.toMap()
        return names.size
    }
}