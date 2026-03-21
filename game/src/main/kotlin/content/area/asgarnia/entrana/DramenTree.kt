package content.area.asgarnia.entrana

import content.entity.combat.killer
import content.quest.quest
import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class DramenTree : Script {
    init {
        objectOperate("Chop down", "dramen_tree") { (target) ->
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                message("You do not have a hatchet which you have the woodcutting level to use.")
                return@objectOperate
            }
            if (!has(Skill.Woodcutting, 36, true)) {
                return@objectOperate
            }
            when (quest("lost_city")) {
                "unstarted", "started" -> {
                    message("The tree seems to have a ominous aura to it. You do not feel like chopping it down.")
                    return@objectOperate
                }
                "find_staff", "tree_spirit" -> {
                    set("lost_city", "tree_spirit")
                    val spirit = NPCs.findOrNull(tile.regionLevel, "tree_spirit_lost_city")
                        ?: NPCs.add("tree_spirit_lost_city", Tile(2859, 9734))
                    spirit.say("You must defeat me before touching the tree!")
                    spirit.interactPlayer(this, "Attack")
                    return@objectOperate
                }
                else -> {
                    anim("${hatchet.id}_chop")
                    delay(4)
                    inventory.add("dramen_branch")
                    message("You cut a branch from the Dramen tree.")
                }
            }
        }

        npcDeath("tree_spirit_lost_city") {
            val killer = killer as? Player ?: return@npcDeath
            if (killer.quest("lost_city") == "tree_spirit") {
                killer["lost_city"] = "spirit_killed"
            }
        }
    }
}
