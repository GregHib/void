package rs.dusk.tools.definition.item.pipe.page

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

class UniqueIdentifiers : Pipeline.Modifier<MutableMap<Int, Extras>> {

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        val nameMap = mutableSetOf<String>()
        // Identified id's take priority
        content.filter { it.value.first.rs3Idd || it.value.first.osrsIdd || it.value.first.rs2Idd }.forEach { (_, pair) ->
            val (builder, _) = pair
            nameMap.makeUniqueId(builder)
        }
        // The rest
        content.filter { !it.value.first.rs3Idd && !it.value.first.osrsIdd && !it.value.first.rs2Idd }.forEach { (_, pair) ->
            val (builder, _) = pair
            nameMap.makeUniqueId(builder)
        }
        return content
    }

    private fun MutableSet<String>.makeUniqueId(builder: PageCollector) {
        var uid = builder.uid
        var count = 2
        while (contains(uid)) {
            uid = "${builder.uid}_${count++}"
        }
        builder.uid = uid
        add(uid)
    }

}