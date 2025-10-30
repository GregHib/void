package content.area.troll_country.god_wars_dungeon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class SaradominRock : Script {

    val handler: suspend TargetInteraction<Player, GameObject>.() -> Unit = objectOperate@{
        tieRope(player, target.def.stringId)
    }

    init {
        enterArea("godwars_dungeon_multi_area") {
            player.sendVariable("godwars_saradomin_rope_top")
            player.sendVariable("godwars_saradomin_rope_bottom")
        }

        objectOperate("Tie-rope", "godwars_saradomin_rock_top", "godwars_saradomin_rock_bottom") {
            tieRope(player, def.stringId)
        }

        itemOnObjectOperate("rope", "godwars_saradomin_rock_top", handler = handler)

        itemOnObjectOperate("rope", "godwars_saradomin_rock_bottom", handler = handler)
    }

    fun tieRope(player: Player, id: String) {
        if (!player.has(Skill.Agility, 70, message = true)) {
            return
        }
        if (!player.inventory.remove("rope")) {
            player.message("You aren't carrying a rope with you.")
            return
        }
        player.anim("climb_up")
        player[id.replace("rock", "rope")] = true
    }
}
