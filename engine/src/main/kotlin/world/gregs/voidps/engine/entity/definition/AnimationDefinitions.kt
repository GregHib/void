package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class AnimationDefinitions(
    decoder: AnimationDecoder
) : DefinitionsDecoded<AnimationDefinition> {

    override val definitions: Array<AnimationDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("animation definition", definitions.size, start)
    }

    override fun empty() = AnimationDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("animationDefinitionsPath")): AnimationDefinitions {
        timedLoad("animation extra") {
            val data = storage.loadMapIds(path)
            val names = data.map { it.value["id"] as Int to it.key }.toMap()
            ids = data.map { it.key to it.value["id"] as Int }.toMap()
            apply(names, data)
            names.size
        }
        return this
    }

}