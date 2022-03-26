package world.gregs.voidps.world.interact.entity.player.display

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.entity.definition.StructDefinitions
import world.gregs.voidps.engine.utility.get

object CharacterStyle {
    private const val struct = 1048
    const val legsParam = 1185L
    const val topParam = 1182L
    const val armParam = 1183L
    const val wristParam = 1184L
    const val shoesParam = 1186L

    fun onStyle(top: Int, block: (StructDefinition) -> Unit) {
        val structs: StructDefinitions = get()
        for (i in 0 until 64) {
            val style = structs.get(struct + i)
            if (style.getParam<Int>(topParam) == top) {
                block.invoke(style)
                break
            }
        }
    }
}