package world.gregs.voidps.tools.wiki.model

import org.w3c.dom.Node

data class WikiNamespace(val key: Int, val name: String) {
    companion object {
        operator fun invoke(node: Node): WikiNamespace {
            val key = node.attributes.getNamedItem("key").textContent.toInt()
            val name = node.textContent
            return WikiNamespace(key, name)
        }
    }
}
