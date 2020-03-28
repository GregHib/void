package org.redrune.engine.entity.list.player

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import org.redrune.engine.entity.model.Player
import org.redrune.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerList : Players {
    override val delegate: SetMultimap<Tile, Player> = HashMultimap.create()
}