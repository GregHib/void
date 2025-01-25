package content.entity.player.modal

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.get

object CharacterStyle {
    private const val STRUCT = 1048

    fun onStyle(top: Int, block: (StructDefinition) -> Unit) {
        val structs: StructDefinitions = get()
        for (i in 0 until 64) {
            val style = structs.get(STRUCT + i)
            if (style.get<Int>("character_style_top") == top) {
                block.invoke(style)
                break
            }
        }
    }
}