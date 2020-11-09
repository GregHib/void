package rs.dusk.tools.definition.item.pipe.page

import rs.dusk.tools.wiki.model.WikiPage

/**
 * Idd = Identified by id
 */
data class PageCollector(
    val id: Int,
    val name: String,
    var rs2: WikiPage? = null,
    var rs2Idd: Boolean = false,
    var rs3: WikiPage? = null,
    var rs3Idd: Boolean = false,
    var osrs: WikiPage? = null,
    var osrsIdd: Boolean = false,
    var uid: String = ""
)