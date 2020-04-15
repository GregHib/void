package rs.dusk.engine.entity.list.player

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerList : Players {
    override val delegate: SetMultimap<Tile, Player> = HashMultimap.create()
}