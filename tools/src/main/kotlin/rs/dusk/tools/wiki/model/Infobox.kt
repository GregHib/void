package rs.dusk.tools.wiki.model

object Infobox {

    fun indexSuffix(key: String, index: Int) = "$key${if (index > 0) (index + 1).toString() else ""}"

    fun splitByVersion(page: WikiPage?, templateName: String, id: Int, defaultToFirst: Boolean, function: (Map<String, Any>, String) -> Unit) {
        val template = page?.getTemplateMap(templateName) ?: return
        if (template.any { it.key.startsWith("version") }) {
            val entries = template.entries.filter { it.key.startsWith("id") }
            var versionEntry = entries.firstOrNull { entry -> (entry.value as String).split(",").any { it.trim().toIntOrNull() == id } }
            if (versionEntry == null && defaultToFirst) {
                versionEntry = entries.first()
            }
            val version = versionEntry?.key?.removePrefix("id")?.toIntOrNull()
            if (version != null) {
                function(template, version.toString())
                return
            }
        }
        function(template, "")
    }

}