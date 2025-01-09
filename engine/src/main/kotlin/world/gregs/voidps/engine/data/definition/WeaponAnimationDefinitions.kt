package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.WeaponAnimationDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class WeaponAnimationDefinitions {

    private lateinit var definitions: Map<String, WeaponAnimationDefinition>

    fun get(key: String) = getOrNull(key) ?: WeaponAnimationDefinition.EMPTY

    fun getOrNull(key: String) = definitions[key]

    fun load(yaml: Yaml = get(), path: String = Settings["weaponAnimationDefinitionsPath"]): WeaponAnimationDefinitions {
        timedLoad("weapon animation definition") {
            val definitions = Object2ObjectOpenHashMap<String, WeaponAnimationDefinition>()
            val config = object : YamlReaderConfiguration() {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0) {
                        definitions[key] = if (value is Map<*, *>) {
                            WeaponAnimationDefinition.fromMap(key, value as MutableMap<String, Any>)
                        } else {
                            WeaponAnimationDefinition(stringId = key)
                        }
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }
}