package rs.dusk.tools.definition.obj.pipe

import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

class ObjectTrapdoors(private val decoder: ObjectDecoder) : Pipeline.Modifier<MutableMap<Int, Extras>> {

    private fun check(id: Int): Boolean {
        val def = decoder.get(id)
        if (!def.name.contains("trap", true)) {
            return false
        }
        return when(def.options.first()) {
            "Go-down", "Climb-down", "Enter" -> true
            else -> false
        }
    }

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (id, content) ->
            val (_, extras) = content
            val def = decoder.get(id)
            val name = def.name.replace(" ", "")
            if (name.contains("trapdoor", true)) {
                val option = def.options.first()
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