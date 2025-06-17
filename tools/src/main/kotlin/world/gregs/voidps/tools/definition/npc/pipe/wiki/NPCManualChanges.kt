package world.gregs.voidps.tools.definition.npc.pipe.wiki

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras

class NPCManualChanges : Pipeline.Modifier<MutableMap<Int, Extras>> {

    val numberRegex = "([0-9]+)".toRegex()

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (id, content) ->
            val (builder, extras) = content
            val uid = builder.uid

            // When has multiple values; select by index
            val suffix = getSuffixNumber(uid, 1)
            content.select(suffix)

            if (extras.containsKey("examine")) {
                extras["examine"] = (extras["examine"] as String).removePrefix("Female - ").removePrefix("Male - ")
            }
            if (uid.contains("farm_teaser")) {
                builder.rs3Idd = false
            }
        }

        // Manual changes go here
        return content
    }

    private fun getSuffixNumber(text: String, defaultValue: Int): Int = numberRegex.find(text)?.groupValues?.last()?.toIntOrNull() ?: defaultValue

    private val exceptions = setOf("master")

    private fun Extras.select(index: Int) {
        val list = mutableListOf<Pair<String, Any>>()
        val it = second.iterator()
        while (it.hasNext()) {
            val (key, value) = it.next()
            val suffix = getSuffixNumber(key, 1)
            if (suffix == 1 || exceptions.any { key.startsWith(it) }) {
                continue
            }
            it.remove()
            if (suffix == index) {
                list.add(key.removeSuffix(suffix.toString()) to value)
            }
        }
        list.forEach { (key, value) ->
            second[key] = value
        }
    }
}
