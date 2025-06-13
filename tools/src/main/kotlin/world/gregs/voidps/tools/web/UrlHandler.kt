package world.gregs.voidps.tools.web

object UrlHandler {

    fun offset(url: String, depth: Int): String = if (depth > 0) {
        "${"../".repeat(depth)}$url"
    } else {
        url
    }

    fun trimQuery(url: String): String {
        val index = url.indexOf("?")
        if (index >= 0) {
            return url.substring(0, index)
        }
        return url
    }

    fun trimAnchor(url: String): String {
        val index = url.indexOf("#")
        if (index >= 0) {
            return url.substring(0, index)
        }
        return url
    }

    /**
     * Removes the protocol and host from a url, converting subdomains (except www) to top level
     */
    fun removeDomain(url: String, host: String): String {
        var url = url
        val protocol = if (url.startsWith("http://")) {
            "http://"
        } else if (url.startsWith("https://")) {
            "https://"
        } else {
            ""
        }
        url = url.removePrefix(protocol)

        val web = "www."
        if (url.startsWith(web)) {
            url = url.removePrefix(web)
        }

        val end = url.indexOf("/")
        val hostName = url.substring(0, end + 1)
        url = url.replace(hostName, getTopLevelHost(hostName, host))
        return url
    }

    /**
     * Removes the first domain when two exist in a url
     */
    fun removePrefixDomain(url: String): String {
        var index = url.indexOf("http://", 1)
        if (index != -1) {
            return url.substring(index, url.length)
        }
        index = url.indexOf("https://", 1)
        if (index != -1) {
            return url.substring(index, url.length)
        }
        return url
    }

    fun removeSuffixDomain(url: String): String {
        var index = url.indexOf("http://", 1)
        if (index != -1) {
            return url.substring(0, index)
        }
        index = url.indexOf("https://", 1)
        if (index != -1) {
            return url.substring(0, index)
        }
        return url
    }

    private fun getTopLevelHost(hostName: String, host: String): String = if (hostName.length > host.length + 1) {
        hostName.replace(".$host", "")
    } else {
        hostName.replace("$host/", "")
    }

    /**
     * Converts a urls query into a unique page name
     */
    fun convertQuery(url: String): String {
        val index = url.lastIndexOf("?")
        if (index >= 0) {
            val sb = StringBuilder()
            var lastIndex = url.lastIndexOf("/", index)
            lastIndex = if (lastIndex == -1) 0 else lastIndex
            var name = url.substring(lastIndex, index)
            if (name.contains("?")) {
                name = name.substring(0, name.lastIndexOf("?"))
            }
            var extension = ""
            if (name.contains(".")) {
                val parts = name.split(".")
                name = parts.first()
                extension = ".${parts.last()}"
            }
            if (lastIndex > 0) {
                sb.append(url.substring(0, lastIndex).replace("?", "%3F"))
            }
            sb.append(name)
            val anchorIndex = url.lastIndexOf("#", index)
            val query = url.substring(index + 1, if (anchorIndex >= 0) anchorIndex else url.length)
            for (pair in query.split("&")) {
                if (pair.contains("=")) {
                    val parts = pair.split("=")
                    val key = parts.first()
                    val value = parts.last()
                    sb.append("-").append(key).append("-").append(value)
                } else {
                    sb.append("-").append(pair)
                }
            }
            sb.append(extension)
            if (anchorIndex >= 0) {
                sb.append(url.substring(anchorIndex, url.length))
            }
            return sb.toString()
        } else {
            return url
        }
    }
}
