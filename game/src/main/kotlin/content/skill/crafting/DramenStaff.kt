package content.skill.crafting

import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class DramenStaff : Script {
    init {
        crafted(Skill.Crafting) { def ->
            if (quest("lost_city") == "spirit_killed" && def.add.any { it.id == "dramen_staff" }) {
                set("lost_city", "crafted_staff")
            }
        }
    }
}