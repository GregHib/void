package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

/**
 * A slice of a larger result set. Services take a [PageRequest] and return one of these; the
 * numbers in [Pagination] are everything the site's pagination footer prints.
 */
@Serializable
data class Page<T>(
    val pagination: Pagination,
    val items: List<T>,
) {
    companion object {
        fun <T> of(request: PageRequest, total: Long, items: List<T>): Page<T> = Page(Pagination.of(request, total), items)

        fun <T> empty(request: PageRequest): Page<T> = Page(Pagination.of(request, 0), emptyList())
    }
}

/** What the caller asked for. [page] is zero-based. */
data class PageRequest(
    val page: Int = 0,
    val pageSize: Int = DEFAULT_SIZE,
) {
    val offset: Int
        get() = page * pageSize

    companion object {
        const val DEFAULT_SIZE = 25
        const val MAX_SIZE = 100
    }
}

/**
 * Derived counts, resolved once on the server so the client never recomputes them. Build with
 * [of] rather than the constructor — the derived fields are constructor properties only because
 * they have to appear in the JSON.
 */
@Serializable
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val totalPages: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
) {
    companion object {
        fun of(request: PageRequest, total: Long): Pagination {
            val pages = if (total == 0L) 0 else ((total + request.pageSize - 1) / request.pageSize).toInt()
            return Pagination(
                page = request.page,
                pageSize = request.pageSize,
                total = total,
                totalPages = pages,
                hasPrevious = request.page > 0,
                hasNext = request.page < pages - 1,
            )
        }
    }
}

/** The `{ "items": [...] }` envelope the spec uses for unpaged collections. */
@Serializable
data class Items<T>(val items: List<T>)
