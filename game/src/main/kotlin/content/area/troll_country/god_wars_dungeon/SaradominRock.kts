package content.area.troll_country.god_wars_dungeon

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

enterArea("godwars_dungeon_multi_area") {
    player.sendVariable("godwars_saradomin_rope_top")
    player.sendVariable("godwars_saradomin_rope_bottom")
}

objectOperate("Tie-rope", "godwars_saradomin_rock_top", "godwars_saradomin_rock_bottom") {
    if (!player.inventory.remove("rope")) {
        player.message("You aren't carrying a rope with you.")
        return@objectOperate
    }
    player.anim("climb_up")
    player[target.id.replace("rock", "rope")] = true
}
