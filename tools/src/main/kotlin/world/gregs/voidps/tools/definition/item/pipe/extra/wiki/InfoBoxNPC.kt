package world.gregs.voidps.tools.definition.item.pipe.extra.wiki

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.definition.item.pipe.extra.wiki.InfoBoxItem.Companion.removeLinks
import world.gregs.voidps.tools.wiki.model.WikiPage

class InfoBoxNPC : Pipeline.Modifier<Extras> {
    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (_, _, _, _, rs3, _) = builder
        process(extras, rs3)
        return content
    }

    private fun process(extras: MutableMap<String, Any>, page: WikiPage?) {
        val template = page?.getTemplateMap("infobox non-player character") ?: return
        template.forEach { (key, value) ->
            if (value is ArrayList<*>) {
                println("Unknown al $value")
                return@forEach
            }
            when (key) {
                "examine" -> {
                    val text = removeLinks(value as String)
                    InfoBoxItem.splitExamine(text, extras, key, "", false)
                }
                else -> return@forEach
            }
        }
    }

}