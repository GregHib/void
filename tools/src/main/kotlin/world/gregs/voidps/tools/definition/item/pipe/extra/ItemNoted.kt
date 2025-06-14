package world.gregs.voidps.tools.definition.item.pipe.extra

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras

class ItemNoted(private val decoder: Array<ItemDefinition>) : Pipeline.Modifier<Extras> {

    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, n, _, _, _, _, _, _, uid) = builder
        val def = decoder.getOrNull(id) ?: return content

        if (def.noted || def.lent) {
            extras.clear()
        }
        val name = uid.ifEmpty { toIdentifier(n) }
        builder.uid = when {
            def.noted -> "${name}_noted"
            def.lent -> "${name}_lent"
            else -> builder.uid
        }
        return content
    }
}
