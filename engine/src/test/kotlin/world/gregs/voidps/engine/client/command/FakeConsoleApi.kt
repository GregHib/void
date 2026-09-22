package world.gregs.voidps.engine.client.command

/**
 * The windows console api, recorded rather than called.
 */
internal class FakeConsoleApi(
    override val available: Boolean = true,
    private val handles: Map<Int, Long?> = mapOf(INPUT to 1L, OUTPUT to 2L),
    private val modes: MutableMap<Long, Int?> = mutableMapOf(1L to 0x00F7, 2L to 0x0003),
    private val refuse: Set<Long> = emptySet(),
    private var columns: Int? = 120,
    private var page: Int? = 437,
) : ConsoleApi {

    val calls = mutableListOf<String>()

    /**
     * The mode the console was left in.
     */
    fun left(handle: Long): Int = modes[handle] ?: 0

    override fun handle(id: Int): Long? = handles[id]

    override fun mode(handle: Long): Int? = modes[handle]

    override fun mode(handle: Long, mode: Int): Boolean {
        if (refuse.contains(handle)) {
            return false
        }
        modes[handle] = mode
        calls.add("mode $handle $mode")
        return true
    }

    override fun columns(handle: Long): Int? = columns

    override fun codePage(): Int? = page

    override fun codePage(page: Int): Boolean {
        this.page = page
        calls.add("page $page")
        return true
    }

    companion object {
        const val INPUT = -10
        const val OUTPUT = -11
    }
}
