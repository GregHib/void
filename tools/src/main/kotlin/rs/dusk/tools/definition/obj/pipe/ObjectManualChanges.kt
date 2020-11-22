package rs.dusk.tools.definition.obj.pipe

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

class ObjectManualChanges : Pipeline.Modifier<MutableMap<Int, Extras>> {

    val uids: Map<Int, String> = mapOf(
    )

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (id, content) ->
            val (builder, extras) = content
            uids.forEach { (match, replacement) ->
                if (id == match) {
                    builder.uid = replacement
                }
            }
        }
        // Manual changes go here
        return content
    }
}