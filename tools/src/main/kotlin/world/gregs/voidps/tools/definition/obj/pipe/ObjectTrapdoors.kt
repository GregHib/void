package world.gregs.voidps.tools.definition.obj.pipe

import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras

class ObjectTrapdoors(private val decoder: Array<ObjectDefinitionFull>) : Pipeline.Modifier<MutableMap<Int, Extras>> {

    private fun check(id: Int): Boolean {
        val def = decoder[id]
        if (!def.name.contains("trap", true)) {
            return false
        }
        return when (def.options?.first()) {
            "Go-down", "Climb-down", "Enter" -> true
            else -> false
        }
    }

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (id, content) ->
            val (_, extras) = content
            val def = decoder[id]
            val name = def.name.replace(" ", "")
            if (name.contains("trapdoor", true)) {
                val option = def.options?.first()
                if (option == "Open") {
                    extras["open"] = when {
                        check(def.id + 1) -> def.id + 1
                        check(def.id - 1) -> def.id - 1
                        check(def.id - 2) -> def.id - 2
                        else -> return@forEach
                    }
                }
            }
        }
        return content
    }
}
