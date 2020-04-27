package rs.dusk.engine.entity.list.player

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.model.Player
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
@Suppress("ArrayInDataClass")
data class PlayerList(
    override val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<Player>> = Int2ObjectOpenHashMap(MAX_PLAYERS),
    override val pool: LinkedList<ObjectLinkedOpenHashSet<Player>> = LinkedList(),
    override val indexed: Array<Player?> = arrayOfNulls(MAX_PLAYERS)
) : Players