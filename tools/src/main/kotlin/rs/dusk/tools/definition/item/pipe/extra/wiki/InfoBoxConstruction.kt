package rs.dusk.tools.definition.item.pipe.extra.wiki

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.ItemExtras
import rs.dusk.tools.wiki.model.WikiPage

class InfoBoxConstruction : Pipeline.Modifier<ItemExtras> {
    override fun modify(content: ItemExtras): ItemExtras {
        val (builder, extras) = content
        val (_, _, page, rs3Page, _) = builder
        process(extras, rs3Page)
        process(extras, page)
        return content
    }

    private fun process(extras: MutableMap<String, Any>, page: WikiPage?) {
        val template = page?.getTemplateMap("infobox construction") ?: return
        template.forEach { (key, value) ->
            when (key) {
                "examine" -> {
                    val text = InfoBoxItem.removeLinks(value as String)
                    InfoBoxItem.splitByLineBreak(text, extras, key, "", false)
                }
                else -> return@forEach
            }
        }
    }

}