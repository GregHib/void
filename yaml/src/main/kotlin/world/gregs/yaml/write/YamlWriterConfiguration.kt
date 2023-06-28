package world.gregs.yaml.write

/**
 * Handles writing and modifying objects into strings before
 */
open class YamlWriterConfiguration(
    val quoteStrings: Boolean = false,
    val forceExplicit: Boolean = false,
    val quoteKeys: Boolean = false,
    val formatExplicit: Boolean = false
) {

    open fun write(value: Any?, indent: Int, parentMap: String?): Any? {
        return value
    }

    open fun toString(value: Any?, indent: Int, parentMap: String?): String {
        return value.toString()
    }

    companion object {
        val json = YamlWriterConfiguration(
            quoteStrings = true,
            forceExplicit = true,
            quoteKeys = true,
            formatExplicit = true
        )
    }
}