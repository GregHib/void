package world.gregs.yaml.write

data class YamlWriterConfiguration(
    val quoteStrings: Boolean = false,
    val forceExplicit: Boolean = false,
    val quoteKeys: Boolean = false,
)