package rs.dusk.engine.model.entity.character.npc

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import rs.dusk.engine.model.entity.list.MAX_NPCS
import rs.dusk.engine.model.entity.list.PooledMapList
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class NPCs(
    override val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<NPC?>> = Int2ObjectOpenHashMap(MAX_NPCS),
    override val pool: LinkedList<ObjectLinkedOpenHashSet<NPC?>> = LinkedList(),
    override val indexed: Array<NPC?> = arrayOfNulls(MAX_NPCS)
) : PooledMapList<NPC>