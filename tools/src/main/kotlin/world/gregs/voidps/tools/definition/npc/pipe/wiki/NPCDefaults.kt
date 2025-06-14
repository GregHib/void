package world.gregs.voidps.tools.definition.npc.pipe.wiki

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras

/**
 * Removes default values to save space
 */
class NPCDefaults : Pipeline.Modifier<MutableMap<Int, Extras>> {
    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (_, builder) ->
            processExtras(builder.second)
        }
        return content
    }

    private fun processExtras(extras: MutableMap<String, Any>) {
        extras.remove("members", false)
        extras.remove("aggressive", false)
        extras.remove("immune", false)
        extras.remove("diseased", false)
        extras.remove("immune_stun", false)
        extras.remove("immune_deflect", false)
        extras.remove("immune_drain", false)

        extras.remove("weakness", "none")
        extras.remove("xpbonus", 0.0)
        extras.remove("style", "none")
        extras.remove("examine", "It's a null.")
        extras.remove("examine", "")

        extras.remove("slaylvl", 0)
        extras.remove("hitpoints", 0)
        extras.remove("max", 0)
        extras.remove("poison", 0)
        extras.remove("combat") // TODO what about dung npcs?

        extras.remove("att", 0)
        extras.remove("str", 0)
        extras.remove("def", 0)
        extras.remove("mage", 0)
        extras.remove("range", 0)
        extras.remove("attbns", 0)
        extras.remove("strbns", 0)
        extras.remove("amagic", 0)
        extras.remove("mbns", 0)
        extras.remove("arange", 0)
        extras.remove("rngbns", 0)
        extras.remove("dstab", 0)
        extras.remove("dslash", 0)
        extras.remove("dcrush", 0)
        extras.remove("dmagic", 0)
        extras.remove("drange", 0)
    }
}
