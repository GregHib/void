package world.gregs.voidps.engine.client.command

import world.gregs.voidps.engine.client.ui.chat.toSIIntOrNull

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
            ArgType.Int -> value.toSIIntOrNull() != null
            ArgType.Double -> value.toDoubleOrNull() != null
            ArgType.Boolean -> value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)
        }
    }

}

enum class ArgType { String, Int, Double, Boolean }

fun intArg(key: String, desc: String = "", optional: Boolean = false, autofill: (() -> Set<String>)? = null) = CommandArgument(key, ArgType.Int, optional = optional, autofill = autofill, description = desc,)

fun intArg(key: String, desc: String = "", optional: Boolean = false, autofill: Set<String>) = CommandArgument(key, ArgType.Int, optional = optional, autofill = { autofill }, description = desc)

fun boolArg(key: String, desc: String = "", optional: Boolean = false, autofill: (() -> Set<String>)? = null) = CommandArgument(key, ArgType.Boolean, optional = optional, autofill = autofill, description = desc,)

fun boolArg(key: String, desc: String = "", optional: Boolean = false, autofill: Set<String>) = CommandArgument(key, ArgType.Boolean, optional = optional, autofill = { autofill }, description = desc)

fun stringArg(key: String, desc: String = "", optional: Boolean = false, autofill: (() -> Set<String>)? = null) = CommandArgument(key, ArgType.String, optional = optional, autofill = autofill, description = desc,)

fun stringArg(key: String, desc: String = "", optional: Boolean = false, autofill: Set<String>) = CommandArgument(key, ArgType.String, optional = optional, autofill = { autofill }, description = desc)

fun varArgs(key: String, desc: String = ""): Array<CommandArgument> {
    val arg = stringArg(key, desc, optional = true)
    return Array(5) { arg }
}