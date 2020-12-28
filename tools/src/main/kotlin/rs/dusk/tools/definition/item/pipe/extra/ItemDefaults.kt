package rs.dusk.tools.definition.item.pipe.extra

import rs.dusk.engine.entity.item.ItemKept
import rs.dusk.engine.entity.item.ItemUse
import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

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
        extras.remove("stacksinbank", true)
        extras.remove("weight", 0.0)
        extras.remove("edible", false)
        val destroy = extras["destroy"]
        if(destroy != null && destroy is String && destroy.startsWith("drop", true)) {
            extras.remove("destroy")
        }
        extras.remove("destroy", "")
        extras.remove("destroy", "N/A")
        extras.remove("destroy", "The only way to unbind an item is to destroy it.")
        extras.remove("examine", "It's a null.")
        extras.remove("examine", "")
        extras.remove("astab", 0.0)
        extras.remove("aslash", 0.0)
        extras.remove("acrush", 0.0)
        extras.remove("amagic", 0.0)
        extras.remove("arange", 0.0)
        extras.remove("dstab", 0.0)
        extras.remove("dslash", 0.0)
        extras.remove("dcrush", 0.0)
        extras.remove("dmagic", 0.0)
        extras.remove("drange", 0.0)
        extras.remove("dsummon", 0.0)
        extras.remove("str", 0.0)
        extras.remove("rangestr", 0.0)
        extras.remove("absorbmelee", 0)
        extras.remove("absorbmagic", 0)
        extras.remove("absorbranged", 0)
        extras.remove("magicdamage", 0)
        extras.remove("prayer", 0)
        extras.remove("limit", 0)
        extras.remove("kept", ItemKept.Never)
        extras.remove("use", ItemUse.Surface)
    }

}