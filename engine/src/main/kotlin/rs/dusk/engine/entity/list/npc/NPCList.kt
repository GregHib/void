package rs.dusk.engine.entity.list.npc

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import rs.dusk.engine.entity.model.NPC
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class NPCList(
    override val delegate: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<NPC>> = Int2ObjectOpenHashMap(),
    override val pool: LinkedList<ObjectLinkedOpenHashSet<NPC>> = LinkedList()
) : NPCs