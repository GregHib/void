package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.pathString

class InventoryDefinitions(
    override var definitions: Array<InventoryDefinition>
) : DefinitionsDecoder<InventoryDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = InventoryDefinition.EMPTY

    fun load(paths: List<String>, itemDefs: ItemDefinitions = get()): InventoryDefinitions {
        timedLoad("inventory extra") {
            val ids = Object2IntOpenHashMap<String>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "defaults" -> {
                                    val defaults = ObjectArrayList<Map<String, Int>>()
                                    while (nextEntry()) {
                                        val item = key()
                                        val value = int()
                                        val default = Object2IntOpenHashMap<String>()
                                        default[item] = value
                                        defaults.add(default)
                                    }
                                    extras[key] = defaults
                                }
                                else -> extras[key] = value()
                            }
                        }
                        if (id > -1) {
                            ids[stringId] = id
                            definitions[id].extras = extras
                            definitions[id].stringId = stringId
                        }
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }
}