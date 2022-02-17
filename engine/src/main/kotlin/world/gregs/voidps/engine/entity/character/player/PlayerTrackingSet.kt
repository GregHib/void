package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.LOCAL_PLAYER_CAP
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.character.ViewState
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.utility.get

/**
 * Keeps track of players moving in and out of view
 * Each tick [start] clears the view of all players except self then
 * [ViewportUpdating] re-adds all players still within view and queues new
 * additions to be added the following tick.
 */
class PlayerTrackingSet(
    val tickAddMax: Int,
    override val localMax: Int,
    override val radius: Int = VIEW_RADIUS - 1
) : CharacterTrackingSet<Player>, Iterable<Player> {

    override val locals = IntArray(LOCAL_PLAYER_CAP)
    override val state = ViewState(MAX_PLAYERS)
    override var lastIndex = 0
    override var addCount = 0
    override var total: Int = 0

    fun remove(index: Int) = state.removing(index)

    fun local(index: Int) = state.local(index) || state.removing(index)

    fun add(index: Int) = state.adding(index)

    fun addSelf(self: Player) {
        if (!state.local(self.index)) {
            locals[lastIndex++] = self.index
            state.setLocal(self.index)
        }
        total++
    }

    /**
     * If an entity is being removed, return it to the local list
     * Otherwise queue it if there is room on the addition list
     */
    override fun track(entity: Player, self: Player?) {
        if (state.removing(entity.index)) {
            state.setLocal(entity.index)
            total++
        } else if (self == null || entity != self) {
            if (addCount < tickAddMax) {
                state.setAdding(entity.index)
                addCount++
                total++
            }
        }
    }

    override fun iterator(): Iterator<Player> {
        val players: Players = get()
        return object : Iterator<Player> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < lastIndex
            }

            override fun next(): Player {
                return players.indexed(locals[index++])!!
            }
        }
    }
}
