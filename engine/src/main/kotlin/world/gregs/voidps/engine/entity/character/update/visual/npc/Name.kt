package world.gregs.voidps.engine.entity.character.update.visual.npc

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class Name(var name: String = "") : Visual

const val NAME_MASK = 0x40000

fun NPC.flagName() = visuals.flag(NAME_MASK)

fun NPC.getName() = visuals.getOrPut(NAME_MASK) { Name() }

var NPC.name: String
    get() = getName().name
    set(value) {
        getName().name = value
        flagName()
    }