package rs.dusk.tools.definition.item.pipe.extra.wiki

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.ItemExtras
import rs.dusk.tools.wiki.model.Wiki
import rs.dusk.tools.wiki.model.WikiPage

class ItemExchangePrices(val wiki: Wiki) : Pipeline.Modifier<ItemExtras> {

    private val itemIds = mutableMapOf<Int, WikiPage>()

    init {
        wiki.pages.forEach { page ->
            val text = page.revision.text
            when {
                text.contains("exchangeitem", true) -> {
                    val map = page.getTemplateMap("exchangeitem") ?: return@forEach
                    val id = (map["ItemId"] as String).toInt()
                    itemIds[id] = page
                }
            }
        }
    }

    override fun modify(content: ItemExtras): ItemExtras {
        val (builder, extras) = content
        val (id, _, _, _, _, _) = builder
        val page = itemIds[id] ?: return content
        val template = page.getTemplateMap("exchangeitem") ?: return content
        template.forEach { (key, value) ->
            when (key.toLowerCase()) {
                "price" -> {
                    extras[key.toLowerCase()] = (value as String).replace(",", "").toInt()
                }
                else -> return@forEach
            }
        }
        return content
    }
}