package world.gregs.voidps.web.route

/**
 * Limits the number of open proxy connections per remote host and overall.
 */
class ProxyLimits(
    private val maxPerHost: Int,
    private val maxTotal: Int,
) {
    private val hosts = HashMap<String, Int>()
    private var total = 0

    @Synchronized
    fun acquire(host: String): Boolean {
        if (total >= maxTotal || (hosts[host] ?: 0) >= maxPerHost) {
            return false
        }
        total++
        hosts.merge(host, 1, Int::plus)
        return true
    }

    @Synchronized
    fun release(host: String) {
        total--
        hosts.computeIfPresent(host) { _, count -> if (count <= 1) null else count - 1 }
    }

    @Synchronized
    fun count(host: String): Int = hosts[host] ?: 0

    @Synchronized
    fun total(): Int = total
}
