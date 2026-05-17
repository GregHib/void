package world.gregs.voidps.tools.search.screen.view.table.filter

import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.tools.search.displayValue
import world.gregs.voidps.tools.search.screen.view.detail.ParamLookup

fun matchesFilter(rawValue: Any?, filter: FieldFilter): Boolean {
    if (filter.value.isBlank()) return true
    val query = filter.value.trim()
    return when (filter.mode) {
        MatchMode.CONTAINS -> displayValue(rawValue).contains(query, ignoreCase = true)
        MatchMode.EXACT -> displayValue(rawValue).equals(query, ignoreCase = true)
        MatchMode.GREATER_THAN -> query.toLongOrNull()?.let { n ->
            when (rawValue) {
                is Number -> rawValue.toLong() > n
                is IntArray -> rawValue.any { it > n }
                is ShortArray -> rawValue.any { it > n }
                is ByteArray -> rawValue.any { it > n }
                else -> false
            }
        } ?: false
        MatchMode.LESS_THAN -> query.toLongOrNull()?.let { n ->
            when (rawValue) {
                is Number -> rawValue.toLong() < n
                is IntArray -> rawValue.any { it < n }
                is ShortArray -> rawValue.any { it < n }
                is ByteArray -> rawValue.any { it < n }
                else -> false
            }
        } ?: false
        MatchMode.HAS_VALUE -> when (rawValue) {
            is IntArray -> rawValue.any { it.toString().contains(query, ignoreCase = true) }
            is ShortArray -> rawValue.any { it.toString().contains(query, ignoreCase = true) }
            is ByteArray -> rawValue.any { it.toString().contains(query, ignoreCase = true) }
            is Array<*> -> rawValue.any { it?.toString()?.contains(query, ignoreCase = true) == true }
            is Map<*, *> -> rawValue.keys.any { it.toString().contains(query, ignoreCase = true) } ||
                    rawValue.values.any { it.toString().contains(query, ignoreCase = true) }
            else -> displayValue(rawValue).contains(query, ignoreCase = true)
        }
        MatchMode.PARAM_KEY -> {
            if (rawValue !is Map<*, *>) return false
            val queryId = Params.idOrNull(query)?.takeIf { it != -1 } ?: query.toIntOrNull()
            rawValue.keys.any { k ->
                k.toString() == queryId?.toString() ||
                        (k is Int && ParamLookup.of(k)?.contains(query, ignoreCase = true) == true)
            }
        }
        MatchMode.PARAM_VALUE -> {
            if (rawValue !is Map<*, *>) return false
            rawValue.values.any { it.toString().contains(query, ignoreCase = true) }
        }
        MatchMode.NOT_NULL -> rawValue != null && rawValue.toString() != "null" && rawValue.toString() != "-1"
        MatchMode.NOT_EMPTY -> when (rawValue) {
            null -> false
            is String -> rawValue.isNotBlank()
            is Array<*> -> rawValue.isNotEmpty()
            is IntArray -> rawValue.isNotEmpty()
            is ShortArray -> rawValue.isNotEmpty()
            is ByteArray -> rawValue.isNotEmpty()
            is Map<*, *> -> rawValue.isNotEmpty()
            else -> rawValue.toString().let { it != "null" && it != "-1" && it.isNotBlank() }
        }
    }
}