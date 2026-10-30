package content.bot.chat

/**
 * Maps string ids ("iron_ore", "raw_shrimps", "seers_village") onto a quick chat enum index ("iron", "shrimp", "Seers' Village")
 */
class QuickChatEnumResolver(values: Map<Int, Any>) {
    private val index = HashMap<String, Int>()

    init {
        for ((key, value) in values) {
            if (value is String) {
                index.putIfAbsent(normalise(value), key)
            }
        }
    }

    fun index(key: String): Int? {
        val normal = normalise(key)
        val raw = normal.removePrefix("raw")
        for (candidate in arrayOf(normal, raw, raw.removeSuffix("ore"), raw.removeSuffix("logs"), raw.removeSuffix("s"), raw.removeSuffix("es"))) {
            val value = index[candidate]
            if (value != null) {
                return value
            }
        }
        return null
    }

    private fun normalise(value: String) = value.lowercase().filter { it.isLetterOrDigit() }
}
