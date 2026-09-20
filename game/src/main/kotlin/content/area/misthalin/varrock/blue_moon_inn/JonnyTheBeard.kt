package content.area.misthalin.varrock.blue_moon_inn

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

// Source: https://runescape.wiki/w/Transcript:Jonny_the_beard?oldid=36108723
class JonnyTheBeard : Script {
    init {
        npcOperate("Talk-to", "jonny_the_beard") {
            message("Jonny the beard is not interested in talking.")
            return@npcOperate
        }
    }
}