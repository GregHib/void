package world.gregs.voidps.tools.definition.item.pipe.extra

import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.entity.item.ItemUse
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras

/**
 * Removes default values to save space
 */
class ItemDefaults : Pipeline.Modifier<MutableMap<Int, Extras>> {

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (_, builder) ->
            processExtras(builder.second)
        }
        return content
    }

    private fun processExtras(extras: MutableMap<String, Any>) {
        extras.remove("tradeable", true)
        extras.remove("bankable", true)
        extras.remove("bank_stacks", true)
        extras.remove("weight", 0.0)
        extras.remove("edible", false)
        val destroy = extras["destroy"]
        if (destroy != null && destroy is String && destroy.startsWith("drop", true)) {
            extras.remove("destroy")
        }
        extras.remove("destroy", "")
        extras.remove("destroy", "N/A")
        extras.remove("destroy", "The only way to unbind an item is to destroy it.")
        extras.remove("examine", "It's a null.")
        extras.remove("examine", "")
        extras.remove("stab", 0.0)
        extras.remove("slash", 0.0)
        extras.remove("crush", 0.0)
        extras.remove("magic", 0.0)
        extras.remove("range", 0.0)
        extras.remove("stab_def", 0.0)
        extras.remove("slash_def", 0.0)
        extras.remove("crush_def", 0.0)
        extras.remove("magic_def", 0.0)
        extras.remove("range_def", 0.0)
        extras.remove("summoning_def", 0.0)
        extras.remove("str", 0.0)
        extras.remove("range_str", 0.0)
        extras.remove("absorb_melee", 0)
        extras.remove("absorb_magic", 0)
        extras.remove("absorb_range", 0)
        extras.remove("magic_damage", 0)
        extras.remove("prayer", 0)
        extras.remove("limit", 0)
        extras.remove("kept", ItemKept.Never)
        extras.remove("use", ItemUse.Surface)
    }
}
