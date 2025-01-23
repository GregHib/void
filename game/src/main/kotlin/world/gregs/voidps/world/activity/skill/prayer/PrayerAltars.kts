package world.gregs.voidps.world.activity.skill.prayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate

objectOperate("Pray", "prayer_altar_*") {
    pray()
}

objectOperate("Pray-at", "prayer_altar_*") {
    pray()
}

fun ObjectOption<Player>.pray() {
    if (player.levels.getOffset(Skill.Prayer) >= 0) {
        player.message("You already have full Prayer points.")
    } else {
        player.levels.set(Skill.Prayer, player.levels.getMax(Skill.Prayer))
        player.setAnimation("altar_pray")
        player.message("You recharge your Prayer points.")
        player["prayer_point_power_task"] = true
    }
}

objectOperate("Check", "prayer_altar_chaos_varrock") {
    player.message("An altar to the evil god Zamorak.")
}