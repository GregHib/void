package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.config.WeaponAnimationDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

class WeaponAnimationDefinitions {

    lateinit var definitions: Map<String, WeaponAnimationDefinition>

    fun get(key: String) = getOrNull(key) ?: WeaponAnimationDefinition.EMPTY

    fun getOrNull(key: String) = definitions[key]

    fun load(path: String): WeaponAnimationDefinitions {
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
        return this
    }

    internal fun copyFrom(other: WeaponAnimationDefinitions) {
        definitions = other.definitions
    }
}

object WeaponAnimationLoader : Loader<WeaponAnimationDefinitions> {
    val instance = WeaponAnimationDefinitions()
    override val name = "weapon_animations"
    override val file = listOf("definitions.weapons.animations")

    override fun load(context: LoadContext): WeaponAnimationDefinitions = WeaponAnimationDefinitions().load(context.paths(this).single())

    override fun install(state: WeaponAnimationDefinitions) = instance.copyFrom(state)
}
