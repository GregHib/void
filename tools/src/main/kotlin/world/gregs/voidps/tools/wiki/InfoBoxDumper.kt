package world.gregs.voidps.tools.wiki

import world.gregs.voidps.tools.wiki.model.Wiki

object InfoBoxDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        val wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescapewiki-latest-pages-articles-2011-08-14.xml")

        val monsters = mutableMapOf<String, Any>()
        val items = mutableMapOf<String, Any>()
        val exchangables = mutableMapOf<String, Any>()
        val charms = mutableMapOf<String, Any>()
        var monsterCount = 0
        var itemCount = 0
        var exchangeCount = 0
        var charmCount = 0
        wiki.pages.filter { it.namespace.key == 0 }.forEach { page ->
            val text = page.revision.text

            if (text.contains("infobox monster", true)) {
                val template = page.templates.first { it.first.equals("infobox monster", true) }
                monsters[page.title] = template.second
                monsterCount++
            }
            if (text.contains("infobox item", true)) {
                val template = page.templates.firstOrNull { it.first.equals("infobox item", true) || it.first == "Template:Infobox item" }
                if (template != null) {
                    items[page.title] = template.second
                    itemCount++
                } else {
                    println("Cant find ${page.title} ${page.templates.map { it.first }}")
                }
            }
        }
        wiki.pages.filter { it.namespace.key == 112 }.forEach { page ->
            val text = page.revision.text
            if (text.contains("exchangeitem", true)) {
                val template = page.templates.first { it.first.equals("exchangeitem", true) || it.first == "ExchangeItemNat" }
                exchangables[page.title] = template.second
                exchangeCount++
            }
        }
        wiki.pages.filter { it.namespace.key == 114 }.forEach { page ->
            val text = page.revision.text
            if (text.contains("charm data", true)) {
                val template = page.templates.first { it.first.equals("charm data", true) }
                charms[page.title] = template.second
                charmCount++
            }
        }

        println(monsters)

        println("Found ${monsters.size} $monsterCount monsters, ${charms.size} $charmCount charm tables, ${items.size} $itemCount items, ${exchangables.size} $exchangeCount g.e items with details.")
    }
}
