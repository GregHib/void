package world.gregs.voidps.world.interact.entity.player.display

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.Parameter
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.get

object CharacterStyle {
    private const val STRUCT = 1048

    fun onStyle(top: Int, block: (StructDefinition) -> Unit) {
        val structs: StructDefinitions = get()
        for (i in 0 until 64) {
            val style = structs.get(STRUCT + i)
            if (style.getParam<Int>(Parameter.CHARACTER_STYLE_TOP) == top) {
                block.invoke(style)
                break
            }
        }
    }
}