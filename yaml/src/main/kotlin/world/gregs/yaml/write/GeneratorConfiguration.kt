package world.gregs.yaml.write

data class GeneratorConfiguration(
    val quoteStrings: Boolean = false,
    val forceExplicit: Boolean = false,
    val quoteKeys: Boolean = false,
)