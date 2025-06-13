package world.gregs.voidps.tools.wiki

import world.gregs.voidps.tools.wiki.model.Wiki

/**
 *  Example usage of the wiki model
 */
object RunescapeWikiPagesParser {

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescapewiki-latest-pages-articles-2011-08-14.xml")

            val page = wiki.page("Agility")
            page.tables.forEach { table ->
                table.print()
                println("\n")
            }

            page.templates.forEach { (name, obj) ->
                println("Template: $name")
                if (obj is List<*>) {
                    obj.forEach {
                        println(it)
                    }
                } else if (obj is Map<*, *>) {
                    obj.forEach { (key, value) ->
                        println("$key = $value")
                    }
                }
                println()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
