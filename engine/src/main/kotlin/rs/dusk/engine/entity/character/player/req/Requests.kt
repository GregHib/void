package rs.dusk.engine.entity.character.player.req

import rs.dusk.engine.entity.character.player.Player

class Requests(private val player: Player) {
    private val requests = mutableMapOf<Player, MutableMap<String, (Player, Player) -> Unit>>()

    fun add(player: Player, type: String, request: (requester: Player, acceptor: Player) -> Unit): Boolean {
        if (player.requests.has(this.player, type)) {
            val originalRequest = player.requests.getOrNull(this.player, type) ?: request
            originalRequest.invoke(this.player, player)
            player.requests.remove(this.player, type)
            return false
        }
        val map = get(player)
        map[type] = request
        return true
    }

    fun has(player: Player): Boolean {
        return requests.containsKey(player)
    }

    fun has(player: Player, type: String): Boolean {
        return has(player) && requests[player]!!.containsKey(type)
    }

    fun getOrNull(player: Player): Map<String, (Player, Player) -> Unit>? {
        return requests[player]
    }

    private fun get(player: Player): MutableMap<String, (Player, Player) -> Unit> {
        return requests.getOrPut(player) { mutableMapOf() }
    }

    fun getOrNull(player: Player, type: String): ((Player, Player) -> Unit)? {
        return getOrNull(player)?.get(type)
    }

    fun remove(player: Player, type: String): Boolean {
        if (!has(player, type)) {
            return false
        }
        return get(player).remove(type) != null
    }

    fun removeAll(player: Player): Boolean {
        if (!has(player)) {
            return false
        }
        val removed = get(player).isNotEmpty()
        requests.remove(player)
        return removed
    }
}