package rs.dusk.tools.definition.item.pipe.page

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

class UniqueIdentifiers : Pipeline.Modifier<MutableMap<Int, Extras>> {

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        val nameMap = mutableMapOf<String, Int>()
        // Identified id's take priority
        content.filter { it.value.first.rs3Idd }.forEach { (_, pair) ->
            val (builder, _) = pair
            nameMap.makeUniqueId(builder)
        }
        // The rest
        content.filter { !it.value.first.rs3Idd }.forEach { (_, pair) ->
            val (builder, _) = pair
            nameMap.makeUniqueId(builder)
        }
        return content
    }

    fun MutableMap<String, Int>.makeUniqueId(builder: PageCollector) {
        val duplicate = containsKey(builder.uid)
        this[builder.uid] = getOrDefault(builder.uid, 0) + 1
        if (duplicate) {
            builder.uid = "${builder.uid}_${this[builder.uid]}"
        }
    }
}