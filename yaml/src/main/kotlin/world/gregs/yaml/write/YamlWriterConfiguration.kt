package world.gregs.yaml.write

/**
 * Handles writing and modifying objects into strings before
 */
open class YamlWriterConfiguration(
    val quoteStrings: Boolean = false,
    val forceExplicit: Boolean = false,
    val quoteKeys: Boolean = false,
    val formatExplicitMap: Boolean = false,
    val formatExplicitListSizeLimit: Int = 25
) {

    open fun write(value: Any?, indent: Int, parentMap: String?): Any? {
        return value
    }

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
            quoteStrings = true,
            forceExplicit = true,
            quoteKeys = true,
            formatExplicitMap = true,
            formatExplicitListSizeLimit = 25
        )
    }
}