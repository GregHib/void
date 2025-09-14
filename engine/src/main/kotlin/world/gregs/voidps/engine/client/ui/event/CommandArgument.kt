package world.gregs.voidps.engine.client.ui.event

/**
 * Command argument
 */
data class CommandArgument(
    val key: String,
    val type: ArgType = ArgType.String,
    val optional: Boolean = false,
    val autofill: (() -> Set<String>)? = null,
    val description: String = "",
) {
    override fun toString(): String {
        val type = ":${type.name.lowercase()}"
        return if (optional) "[$key$type]" else "($key$type)"
    }

    fun canParse(value: String): Boolean {
        return when (type) {
            ArgType.String -> true
            ArgType.Int -> value.toIntOrNull() != null
            ArgType.Double -> value.toDoubleOrNull() != null
            ArgType.Boolean -> value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)
        }
    }

}

enum class ArgType { String, Int, Double, Boolean }


/**
 * Dynamic [autofill] values
 */
inline fun <reified T : Any> arg(key: String, optional: Boolean = false, desc: String = "", noinline autofill: (() -> Set<String>)? = null) = CommandArgument(
    key,
    when (T::class) {
        Boolean::class -> ArgType.Boolean
        Double::class -> ArgType.Double
        Int::class -> ArgType.Int
        else -> ArgType.String
    },
    optional = optional,
    autofill = autofill,
    description = desc,
)


inline fun <reified T : Any> arg(key: String, optional: Boolean = false, desc: String = "", autofill: Set<String>) = CommandArgument(
    key,
    when (T::class) {
        Boolean::class -> ArgType.Boolean
        Double::class -> ArgType.Double
        Int::class -> ArgType.Int
        else -> ArgType.String
    },
    optional = optional,
    autofill = { autofill },
    description = desc,
)