package content.area.kharidian_desert.jaldraocht_pyramid

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound

class JaldraochtPyramid : Script {

    init {
        objTeleportTakeOff("Enter", "jaldraocht_pyramid_tunnel") { _, _ ->
            if (!questCompleted("desert_treasure")) {
                message("This passage does not seem to lead anywhere...")
                return@objTeleportTakeOff Teleport.CANCEL
            }
            Teleport.CONTINUE
        }

        objectOperate("Pray-at", "ancient_altar") {
            if (!questCompleted("desert_treasure")) {
                message("You must have completed Desert Treasure to use this altar.")
                return@objectOperate
            }
            if (!has(Skill.Magic, 50)) {
                message("You need a Magic level of at least 50 in order to do this.")
                return@objectOperate
            }
            anim("altar_pray")
            sound("prayer_recharge")
            levels.drain(Skill.Prayer, levels.get(Skill.Prayer))
            delay(3)
            if (interfaces.contains("ancient_spellbook")) {
                open("modern_spellbook")
                message("You feel a strange drain upon your memory...")
            } else {
                open("ancient_spellbook")
                message("You feel a strange wisdom fill your mind...")
            }
        }
    }
}
