package world.gregs.yaml.write

data class YamlWriterConfiguration(
    val quoteStrings: Boolean = false,
    val forceExplicit: Boolean = false,
    val quoteKeys: Boolean = false,
    val formatExplicit: Boolean = false
) {
    companion object {
        val json = YamlWriterConfiguration(
            quoteStrings = true,
            forceExplicit = true,
            quoteKeys = true,
            formatExplicit = true
        )
    }
}