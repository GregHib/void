package world.gregs.voidps.engine.entity.character.player

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.entity.list.PooledMapList
import java.util.*

@Suppress("ArrayInDataClass")
data class Players(
    override val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<Player?>> = Int2ObjectOpenHashMap(
        MAX_PLAYERS
    ),
    override val pool: LinkedList<ObjectLinkedOpenHashSet<Player?>> = LinkedList(),
    override val indexed: Array<Player?> = arrayOfNulls(MAX_PLAYERS)
) : PooledMapList<Player>