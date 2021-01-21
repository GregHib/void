package world.gregs.void.engine.entity.character.player

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.void.engine.entity.list.MAX_PLAYERS
import world.gregs.void.engine.entity.list.PooledMapList
import java.util.*

/**
 * @author GregHib <greg@gregs.world>
 * @since March 30, 2020
 */
@Suppress("ArrayInDataClass")
data class Players(
    override val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<Player?>> = Int2ObjectOpenHashMap(
        MAX_PLAYERS
    ),
    override val pool: LinkedList<ObjectLinkedOpenHashSet<Player?>> = LinkedList(),
    override val indexed: Array<Player?> = arrayOfNulls(MAX_PLAYERS)
) : PooledMapList<Player>