package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.config.WeaponAnimationDefinition
import world.gregs.voidps.engine.timedLoad

class WeaponAnimationDefinitions {

    lateinit var definitions: Map<String, WeaponAnimationDefinition>

    fun get(key: String) = getOrNull(key) ?: WeaponAnimationDefinition.EMPTY

    fun getOrNull(key: String) = definitions[key]

    fun load(path: String): WeaponAnimationDefinitions {
        timedLoad("weapon animation definition") {
            val definitions = Object2ObjectOpenHashMap<String, WeaponAnimationDefinition>()
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    val types = Int2ObjectOpenHashMap<String>(4, Hash.VERY_FAST_LOAD_FACTOR)
                    while (nextPair()) {
                        types[Params.id(key())] = string()
                    }
                    definitions[stringId] = WeaponAnimationDefinition(stringId = stringId, attackTypes = types)
                }
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }
}
