package world.gregs.voidps.tools.photobooth.render

/**
 * Supplies raw model file bytes (cache index 7) by model id. Backed by void's CacheDelegate so the
 * renderer uses the same cache handle and revision as the definition decoders.
 */
fun interface ModelDataSource {
    fun modelData(id: Int): ByteArray?
}
