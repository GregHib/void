package world.gregs.yaml.write

/**
 * Handles writing and modifying objects into strings before
 *
 * @param quoteStrings Double quote only string values with spaces
 * @param forceQuoteStrings Double quote all string values except anchors and aliases
 * @param quoteKeys Double quote only keys with spaces
 * @param forceQuoteKeys Double quote around all keys
 * @param formatExplicitMap Write all nested maps explicitly (in explicit writer)
 * @param formatExplicitListSizeLimit Size limit before explicit list are written line by line
 */
open class YamlWriterConfiguration(
    val quoteStrings: Boolean = true,
    val forceQuoteStrings: Boolean = false,
    val forceExplicit: Boolean = false,
    val quoteKeys: Boolean = true,
    val forceQuoteKeys: Boolean = false,
    val formatExplicitMap: Boolean = false,
    val formatExplicitListSizeLimit: Int = 25,
) {

    open fun explicit(list: List<*>, indent: Int, parentMap: String?): Boolean = list.firstOrNull() !is Map<*, *> && indent != 0

    open fun write(value: Any?, indent: Int, parentMap: String?): Any? = value

    open fun toString(value: Any?, indent: Int, parentMap: String?): String {
        if (value is Double) {
            var bigDecimal = value.toBigDecimal()
            if (!bigDecimal.toPlainString().contains(".")) {
                bigDecimal = bigDecimal.setScale(1)
            }
            return bigDecimal.toPlainString()
        }
        return value.toString()
    }

    companion object {
        val json = YamlWriterConfiguration(
            forceQuoteStrings = true,
            forceExplicit = true,
            forceQuoteKeys = true,
            formatExplicitMap = true,
            formatExplicitListSizeLimit = 25,
        )
    }
}
