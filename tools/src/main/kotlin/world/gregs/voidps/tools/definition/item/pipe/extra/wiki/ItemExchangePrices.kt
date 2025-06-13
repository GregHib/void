package world.gregs.voidps.tools.definition.item.pipe.extra.wiki

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.wiki.model.Wiki
import world.gregs.voidps.tools.wiki.model.WikiPage

class ItemExchangePrices(val wiki: Wiki) : Pipeline.Modifier<Extras> {

    private val itemIds = mutableMapOf<Int, WikiPage>()

    init {
        wiki.pages.forEach { page ->
            val text = page.revision.text
            when {
                text.contains("exchangeitem", true) -> {
                    val map = page.getTemplateMap("exchangeitem") ?: return@forEach
                    val id = (map["ItemId"] as? String)?.toInt()
                    if (id == null) {
                        println("Unknown id ${map["ItemId"]}")
                    } else {
                        itemIds[id] = page
                    }
                }
            }
        }
    }

    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, _, _, _, _, _) = builder
        val page = itemIds[id] ?: return content
        val template = page.getTemplateMap("exchangeitem") ?: return content
        template.forEach { (key, value) ->
            when (key.lowercase()) {
                "price" -> {
                    extras[key.lowercase()] = (value as String).replace(",", "").toInt()
                }
                else -> return@forEach
            }
        }
        return content
    }
}
