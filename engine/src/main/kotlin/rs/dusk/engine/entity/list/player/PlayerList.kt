package rs.dusk.engine.entity.list.player

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import rs.dusk.engine.entity.model.Player
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class PlayerList(
    override val delegate: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<Player>> = Int2ObjectOpenHashMap(),
    override val pool: LinkedList<ObjectLinkedOpenHashSet<Player>> = LinkedList()
) : Players