package content.area.troll_country.god_wars_dungeon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.ItemObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class SaradominRock : Script {

    init {
        entered("godwars_dungeon_multi_area") {
            sendVariable("godwars_saradomin_rope_top")
            sendVariable("godwars_saradomin_rope_bottom")
        }

        objectOperate("Tie-rope", "godwars_saradomin_rock_top,godwars_saradomin_rock_bottom") { (target) ->
            tieRope(this, target.def(this).stringId)
        }

        itemOnObjectOperate("rope", "godwars_saradomin_rock_top", block = ::tieRope)
        itemOnObjectOperate("rope", "godwars_saradomin_rock_bottom", block = ::tieRope)
    }

    fun tieRope(player: Player, interact: ItemObjectInteract) {
        tieRope(player, interact.target.def.stringId)
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
