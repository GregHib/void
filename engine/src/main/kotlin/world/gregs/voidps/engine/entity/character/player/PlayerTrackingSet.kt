package world.gregs.voidps.engine.entity.character.player

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.LOCAL_PLAYER_CAP
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.PLAYER_TICK_CAP
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.utility.get

class PlayerTrackingSet(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS - 1,
    val add: ObjectArrayFIFOQueue<Player> = ObjectArrayFIFOQueue(PLAYER_TICK_CAP),
    val remove: ObjectArrayFIFOQueue<Player> = ObjectArrayFIFOQueue(LOCAL_PLAYER_CAP)
) : CharacterTrackingSet<Player> {
    override val current: MutableSet<Player>
        get() {
            val players: Players = get()
            return (0 until localIndex).mapNotNull { players.indexed(locals[it]) }.toMutableSet()
        }

    val locals = IntArray(LOCAL_PLAYER_CAP)
    var localIndex = 0
    var addCount = 0
    val state = IntArray(MAX_PLAYERS)

    fun remove(index: Int) = state[index] == REMOVING

    fun local(index: Int) = state[index] == LOCAL || state[index] == REMOVING

    fun add(index: Int) = state[index] == ADDING

    override var total: Int = 0

    override fun start(self: Player?) {
        for (i in 0 until localIndex) {
            val index = locals[i]
//        for (p in current) {
            if (index != self?.index) {
                state[index] = REMOVING
//                remove.enqueue(p)
            }
        }
        total = 0
        if (self != null) {
            addSelf(self)
        }
    }

    override fun update() {
//        while (!remove.isEmpty) {
//            val it = remove.dequeue()
//            if (state[it.index] == REMOVING) {
////                state[it.index] = GLOBAL
//                current.remove(it)
//            }
//        }
//        while (!add.isEmpty) {
//            val it = add.dequeue()
//            if (state[it.index] == ADDING) {
////                state[it.index] = LOCAL
//                current.add(it)
//            }
//        }
        addCount = 0
        localIndex = 0
        for (i in 1 until MAX_PLAYERS) {
            if (state[i] == REMOVING) {
                state[i] = GLOBAL
            } else if (state[i] == ADDING) {
                state[i] = LOCAL
                locals[localIndex++] = i
            } else if (state[i] == LOCAL) {
                locals[localIndex++] = i
            }
        }
//        println("Compare ${current.size} ${localIndex} ${locals.take(localIndex)} $current")
        total = localIndex//current.size
    }

    override fun addSelf(self: Player) {
//        current.add(self)
        if (state[self.index] != LOCAL) {
            locals[localIndex++] = self.index
        }
        state[self.index] = LOCAL
        total++
    }

    override fun track(entity: Player, self: Player?) {
        if (state[entity.index] == REMOVING) {
            state[entity.index] = LOCAL
            total++
        } else if (self == null || entity != self) {
            if (addCount < tickMax) {
                state[entity.index] = ADDING
//                add.enqueue(entity)
                addCount++
                total++
            }
        }
    }

    companion object {
        private const val GLOBAL = 0
        private const val LOCAL = 1
        private const val ADDING = 2
        private const val REMOVING = 3
    }
}
