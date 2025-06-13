package world.gregs.voidps.tools.wiki.model

object Infobox {

    fun indexSuffix(key: String, index: Int) = "$key${if (index > 0) (index + 1).toString() else ""}"

    fun splitByVersion(page: WikiPage?, templateName: String, id: Int, defaultToFirst: Boolean, function: (Map<String, Any>, String) -> Unit) = splitByVersion(page, listOf(templateName), id, defaultToFirst, function)

    fun splitByVersion(page: WikiPage?, templateNames: List<String>, id: Int, defaultToFirst: Boolean, function: (Map<String, Any>, String) -> Unit) {
        val template = getFirstMap(page, templateNames) ?: return
        if (template.any { it.key.startsWith("version") }) {
            val entries = template.entries.filter { it.key.startsWith("id") }
            var versionEntry = entries.firstOrNull { entry -> (entry.value as String).split(",").any { it.trim().toIntOrNull() == id } }
            if (versionEntry == null && defaultToFirst) {
                versionEntry = entries.firstOrNull()
            }
            val version = versionEntry?.key?.removePrefix("id")?.toIntOrNull()
            if (version != null) {
                function(template, version.toString())
            }
        }
        function(template, "")
    }

    fun forEachVersion(page: WikiPage?, templateNames: List<String>, function: (Map<String, Any>, String) -> Unit) {
        val template = getFirstMap(page, templateNames) ?: return
        if (template.any { it.key.startsWith("version") }) {
            function(template, "")
            template.entries.forEach {
                val version = it.key.removePrefix("id").toIntOrNull()
                if (version != null) {
                    function(template, version.toString())
                }
            }
        } else {
            function(template, "")
        }
    }

    fun getFirstMap(page: WikiPage?, templateNames: List<String>): Map<String, Any>? {
        if (page == null) {
            return null
        }
        for (name in templateNames) {
            val map = page.getTemplateMap(name)
            if (map != null) {
                return map
            }
        }
        return null
    }

    fun getFirstList(page: WikiPage?, templateNames: List<String>): List<Pair<String, Any>>? {
        if (page == null) {
            return null
        }
        for (name in templateNames) {
            val map = page.getTemplateList(name)
            if (map != null) {
                return map
            }
        }
        return null
    }
}
