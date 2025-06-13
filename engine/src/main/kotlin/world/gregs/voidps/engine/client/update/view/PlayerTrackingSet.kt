package world.gregs.voidps.engine.client.update.view

import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Keeps track of players moving in and out of view
 * Each tick [update] moves all [add] indices from [globals] into [locals]
 */
class PlayerTrackingSet {

    private val appearanceHash = IntArray(MAX_PLAYERS)
    private val add = BooleanArray(MAX_PLAYERS)

    val locals = IntArray(MAX_PLAYERS)
    var localCount = 0

    val globals = IntArray(MAX_PLAYERS)
    var globalCount = 0

    fun needsAppearanceUpdate(player: Player): Boolean = appearanceHash[player.index] != player.visuals.appearance.hash

    fun updateAppearance(player: Player) {
        appearanceHash[player.index] = player.visuals.appearance.hash
    }

    fun add(index: Int) {
        add[index] = true
    }

    fun remove(index: Int) {
        add[index] = false
    }

    fun addSelf(self: Player) {
        add[self.index] = true
        locals[localCount++] = self.index
        for (i in 1 until MAX_PLAYERS) {
            if (i == self.index || globalCount >= MAX_PLAYERS) {
                continue
            }
            globals[globalCount++] = i
        }
    }

    fun update() {
        localCount = 0
        globalCount = 0
        for (i in 1 until MAX_PLAYERS) {
            val add = add[i]
            if (add) {
                locals[localCount++] = i
            } else {
                globals[globalCount++] = i
            }
        }
    }
}
