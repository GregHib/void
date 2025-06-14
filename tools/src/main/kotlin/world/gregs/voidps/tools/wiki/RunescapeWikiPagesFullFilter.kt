package world.gregs.voidps.tools.wiki

import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.stream.*
import javax.xml.stream.events.XMLEvent

/**
 * Takes full pages_full.xml containing all pages and history from Special:Statistics
 * Filters pages by namespace and revisions by date
 * Outputs xml
 * When output is zipped with bzip2 can be read with [WikiTaxi](https://www.yunqa.de/delphi/products/wikitaxi/index)
 *
 * Fixes:
 * Replace "Template:Infobox item" with "Template:Infobox Item"
 * Replace "Template:Infobox Bonuses Historical" with "Template:Infobox Bonuses"
 */
object RunescapeWikiPagesFullFilter {

    private class Parser(val date: LocalDate, dir: String, val namespacesToDump: Set<String>, val message: Boolean) {

        val file = File(dir, "runescapewiki-latest-pages-articles-${date.toString().replace(" ", "-")}.xml")

        init {
            if (!file.exists()) {
                file.createNewFile()
            }
        }

        val pageEvents = mutableListOf<XMLEvent>()
        val revisionEvents = mutableListOf<XMLEvent>()
        val mostRecentRevisionEvents = mutableListOf<XMLEvent>()
        val eventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(FileWriter(file))!!
        val eventFactory = XMLEventFactory.newInstance()!!
        val redirectPattern = "#(?:REDIRECT|redirect) ?\\[\\[(.*)]]".toRegex()

        var validRevision = false
        var revision = 0
        var type: String? = null
        var pages = 0
        var skipPage = false
        var title: String = ""

        val namespaceIndex = mutableMapOf<String, Int>()
        val namespaces = mutableSetOf<Int>()
        var namespaceId: String? = null

        fun reset() {
            revision = 0
            type = null
            title = ""
            validRevision = false
            pageEvents.clear()
            revisionEvents.clear()
            mostRecentRevisionEvents.clear()
        }

        fun parse(event: XMLEvent) {
            val event = event
            if (skipPage) {
                if (event.eventType == XMLStreamConstants.END_ELEMENT && event.asEndElement().name.localPart == "page") {
                    skipPage = false
                }
                return
            }

            when (event.eventType) {
                XMLStreamConstants.START_ELEMENT -> {
                    type = event.asStartElement().name.localPart
                    when (type) {
                        "namespace" -> namespaceId = event.asStartElement().attributes.next() as? String
                        "revision" -> revision = 1
                    }
                }
                XMLStreamConstants.CHARACTERS -> {
                    when (type) {
                        "namespace" -> {
                            val id = namespaceId?.toInt()
                            if (id != null) {
                                namespaceIndex[event.asCharacters().toString().trim()] = id
                                namespaces.add(id)
                                namespaceId = null
                            }
                        }
                        "timestamp" -> {
                            val timestamp = LocalDate.parse(event.asCharacters().toString(), inputFormatter)
                            validRevision = timestamp.isBefore(date)
                            type = null
                        }
                        "ns" -> {
                            val id = event.asCharacters().toString().toIntOrNull()
                            if (id != null) {
                                if (namespaces.contains(id)) {
                                    skipPage = true
                                    reset()
                                    return
                                } else if (message) {
                                    println(title)
                                }
                            }
                        }
                        "title" -> {
                            val chars = event.asCharacters()
                            if (!chars.isWhiteSpace) {
                                title = chars.toString()
                                if (title.startsWith("Template:Signatures/") || title.startsWith("Template:Signature/") || title.startsWith("Template:Userbox/")) {
                                    skipPage = true
                                    reset()
                                    return
                                }
                            }
                        }
                    }
                }
            }

            when (revision) {
                2 -> revision = 0
                1 -> revisionEvents.add(event)
                else -> pageEvents.add(event)
            }

            when (event.eventType) {
                XMLStreamConstants.END_ELEMENT -> {
                    when (event.asEndElement().name.localPart) {
                        "namespaces" -> {
                            namespaces.removeAll(namespacesToDump.map { namespaceIndex[it] }.toSet())
                        }
                        "revision" -> {
                            if (validRevision) {
                                mostRecentRevisionEvents.clear()
                                mostRecentRevisionEvents.addAll(revisionEvents)
                            }
                            revisionEvents.clear()
                            revision = 2
                        }
                        "siteinfo" -> {
                            pageEvents.forEach {
                                eventWriter.add(it)
                            }
                        }
                        "page" -> {
                            if (!IGNORE_EMTPY_PAGES || mostRecentRevisionEvents.isNotEmpty()) {
                                val pageClose = pageEvents.removeAt(pageEvents.lastIndex)
                                var priorRedirect: String? = null
                                pageEvents.forEach { event ->
                                    eventWriter.add(event)
                                    if (event.eventType == XMLStreamConstants.START_ELEMENT && event.asStartElement().name.localPart == "redirect") {
                                        priorRedirect = event.asStartElement().attributes.next() as? String
                                    }
                                }

                                var priorEventType: String? = null
                                mostRecentRevisionEvents.forEach {
                                    var event = it
                                    // Replace redirect revision text with title from page redirect tag (fixes redirects for rs3 renames)
                                    if (priorRedirect != null && event.eventType == XMLStreamConstants.CHARACTERS && priorEventType == "text") {
                                        val text = event.asCharacters().toString()
                                        if (text.contains("#redirect", true)) {
                                            val result = redirectPattern.matchEntire(text)?.groupValues?.last()
                                            if (result != null && result != priorRedirect && !result.startsWith("$priorRedirect#")) {
                                                event = eventFactory.createCharacters(text.replace(result, priorRedirect!!))
                                            }
                                        }
                                    }
                                    priorEventType = if (event.eventType == XMLStreamConstants.START_ELEMENT) event.asStartElement().name.localPart else null

                                    eventWriter.add(event)
                                }
                                eventWriter.add(pageClose)
                                eventWriter.flush()
                                pages++
                            }
                            reset()
                        }
                        "mediawiki" -> eventWriter.add(event)
                    }
                }
            }
        }

        fun finish() {
            println("Total pages: $pages for $date")
            eventWriter.close()
        }

        companion object {
            private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)!!
        }
    }

    private const val IGNORE_EMTPY_PAGES = true

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            System.setProperty("entityExpansionLimit", "0")
            System.setProperty("totalEntitySizeLimit", "0")
            System.setProperty("jdk.xml.totalEntitySizeLimit", "0")
            val namespaces = setOf(
                "",
                "Template",
                "Category",
                "Update",
                "Exchange",
                "Charm",
                "Calculator",
                "Map",
                "Transcript",
            )
            val directory = "${System.getProperty("user.home")}\\Downloads\\runescape_pages_full\\"
            val dates = mapOf(
                474 to LocalDate.of(2007, 11, 12),
                530 to LocalDate.of(2009, 2, 9),
                550 to LocalDate.of(2009, 7, 7),
                562 to LocalDate.of(2009, 9, 18),
                592 to LocalDate.of(2010, 3, 2),
                614 to LocalDate.of(2010, 8, 24),
                634 to LocalDate.of(2011, 1, 31),
                667 to LocalDate.of(2011, 10, 16),
                718 to LocalDate.of(2012, 6, 13),
                742 to LocalDate.of(2012, 11, 19),
            )
            val parsers = dates.map { (revision, date) -> Parser(date, directory, namespaces, revision == 634) }
            val factory = XMLInputFactory.newInstance()
            val eventReader = factory.createXMLEventReader(FileReader("${directory}runescape_pages_full.xml"))
            val start = System.currentTimeMillis()
            while (eventReader.hasNext()) {
                val event = eventReader.nextEvent()
                parsers.forEach { it.parse(event) }
            }
            parsers.forEach {
                it.finish()
            }
            println("Took ${TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start)} mins")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: XMLStreamException) {
            e.printStackTrace()
        }
    }
}
