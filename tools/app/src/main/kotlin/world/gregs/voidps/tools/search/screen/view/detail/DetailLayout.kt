package world.gregs.voidps.tools.search.screen.view.detail

sealed interface DetailLayout {
    object Inline : DetailLayout
    object Block : DetailLayout
}

fun layoutFor(raw: Any?): DetailLayout = when (raw) {
    null -> DetailLayout.Inline
    is Boolean -> DetailLayout.Inline
    is Number -> DetailLayout.Inline
    is String if raw.length <= 60 -> DetailLayout.Inline
    is Array<*> if raw.size <= 1 -> {
        val first = raw.firstOrNull()
        if (first is Array<*> || first is ByteArray || first is ShortArray || first is IntArray) {
            DetailLayout.Block
        } else {
            DetailLayout.Inline
        }
    }
    is Map<*, *> -> DetailLayout.Block
    is IntArray -> DetailLayout.Block
    is ShortArray -> DetailLayout.Block
    is ByteArray -> DetailLayout.Block
    is Array<*> -> DetailLayout.Block
    else -> DetailLayout.Inline
}