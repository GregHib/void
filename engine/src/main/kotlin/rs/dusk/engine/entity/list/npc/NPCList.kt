package rs.dusk.engine.entity.list.npc

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import rs.dusk.engine.entity.list.MAX_NPCS
import rs.dusk.engine.entity.model.NPC
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
@Suppress("ArrayInDataClass")
data class NPCList(
    override val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<NPC?>> = Int2ObjectOpenHashMap(MAX_NPCS),
    override val pool: LinkedList<ObjectLinkedOpenHashSet<NPC?>> = LinkedList(),
    override val indexed: Array<NPC?> = arrayOfNulls(MAX_NPCS)
) : NPCs