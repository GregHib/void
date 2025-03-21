package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.WeaponAnimationDefinition
import world.gregs.voidps.engine.timedLoad

class WeaponAnimationDefinitions {

    private lateinit var definitions: Map<String, WeaponAnimationDefinition>

    fun get(key: String) = getOrNull(key) ?: WeaponAnimationDefinition.EMPTY

    fun getOrNull(key: String) = definitions[key]

    fun load(path: String = Settings["definitions.weapons.animations"]): WeaponAnimationDefinitions {
        timedLoad("weapon animation definition") {
            val definitions = Object2ObjectOpenHashMap<String, WeaponAnimationDefinition>()
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    val types = Object2ObjectOpenHashMap<String, String>(4, Hash.VERY_FAST_LOAD_FACTOR)
                    while (nextPair()) {
                        types[key()] = string()
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