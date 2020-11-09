package rs.dusk.tools.definition.item.pipe.extra

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier
import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.ItemExtras

class ItemNoted(private val decoder: ItemDecoder) : Pipeline.Modifier<ItemExtras> {

    override fun modify(content: ItemExtras): ItemExtras {
        val (builder, extras) = content
        val (id, n, _, _, _, _, _, _, uid) = builder
        val def = decoder.getOrNull(id) ?: return content

        if (def.noted || def.lent || def.singleNote) {
            extras.clear()
        }
        val name = if (uid.isEmpty()) toIdentifier(n) else uid
        builder.uid = when {
            def.noted -> "${name}_noted"
            def.lent -> "${name}_lent"
            def.singleNote -> "${name}_note"
            else -> builder.uid
        }
        return content
    }
}