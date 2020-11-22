package rs.dusk.tools.definition.obj.pipe

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

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