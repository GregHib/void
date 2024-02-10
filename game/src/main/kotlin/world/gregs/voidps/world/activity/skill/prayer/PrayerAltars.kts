package world.gregs.voidps.world.activity.skill.prayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate

objectOperate("Pray", "prayer_altar_*") {
    if (player.levels.getOffset(Skill.Prayer) >= 0) {
        player.message("You already have full Prayer points.")
    } else {
        player.levels.set(Skill.Prayer, player.levels.getMax(Skill.Prayer))
        player.setAnimation("altar_pray")
        player.message("You recharge your Prayer points.")
    }
}