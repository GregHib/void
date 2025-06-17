package world.gregs.voidps.tools.definition.obj.pipe

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras

class RemoveNullEmptyExtras : Pipeline.Modifier<MutableMap<Int, Extras>> {

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content
            .filter { it.value.first.uid.startsWith("null", true) && !it.value.second.any { entry -> entry.key != "id" } }
            .keys
            .forEach {
                content.remove(it)
            }
        return content
    }
}
