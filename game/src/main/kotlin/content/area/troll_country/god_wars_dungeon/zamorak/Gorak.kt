package content.area.troll_country.god_wars_dungeon.zamorak

import content.skill.prayer.protectMelee
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC

class Gorak : Script {
    init {
        combatDamage { (source) ->
            if (source is NPC && source.id.startsWith("gorak") && protectMelee()) {
                message("Your protective prayer doesn't seem to work!")
            }
        }
    }
}
