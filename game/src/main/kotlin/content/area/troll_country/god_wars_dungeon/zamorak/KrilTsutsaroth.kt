package content.area.troll_country.god_wars_dungeon.zamorak

import content.skill.prayer.protectMelee
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class KrilTsutsaroth : Script {

    val npcs: NPCs by inject()

    var kreeyath: NPC? = null
    var karlak: NPC? = null
    var gritch: NPC? = null

    init {
        npcSpawn("kril_tsutsaroth") {
            if (kreeyath == null) {
                kreeyath = npcs.add("balfrug_kreeyath", Tile(2921, 5319, 2))
            }
            if (karlak == null) {
                karlak = npcs.add("tstanon_karlak", Tile(2932, 5328, 2))
            }
            if (gritch == null) {
                gritch = npcs.add("zakln_gritch", Tile(2919, 5327, 2))
            }
        }

        npcCondition("protect_melee_zamorak") { target ->
            target is Player && target.protectMelee() && !target.hasClock("gwd_last_slam")
        }

        npcAttack("kril_tsutsaroth", "melee_slam", ::slamCooldown)
        npcAttack("kril_tsutsaroth", "melee_slam_poison", ::slamCooldown)

        npcDespawn("balfrug_kreeyath") {
            kreeyath = null
        }

        npcDespawn("tstanon_karlak") {
            karlak = null
        }

        npcDespawn("zakln_gritch") {
            gritch = null
        }

    }

    private fun slamCooldown(npc: NPC, target: Character) {
        target.start("gwd_last_slam", random.nextInt(5) + 6)
    }
}
