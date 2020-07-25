package rs.dusk.engine.client.ui

import rs.dusk.engine.data.file.FileLoader

class InterfaceDetails(val files: FileLoader) {

    class InterfaceDetails(
        val id: Int,
        val name: String,
        parent: String? = null,
        val fixedParent: String? = parent ?: "FixedGameframe",
        val resizeParent: String? = parent ?: "ResizableGameframe",
        index: Int? = null,
        val fixedIndex: Int? = index,
        val resizeIndex: Int? = index,
        val resizable: Boolean = true
    ) {
        override fun toString(): String {
            return "InterfaceDetails(id=$id, name='$name', fixedParent=$fixedParent, resizeParent=$resizeParent, fixedIndex=$fixedIndex, resizeIndex=$resizeIndex, resizable=$resizable)"
        }
    }

    fun load() {
        val path: String = "../data/interfaces.yml"//getProperty("interfacesPath")
        val points: Array<InterfaceDetails> = files.load(path)
        println(points.toList())
    }

    fun getName(id: Int) = ""
    fun getId(name: String) = 1
}