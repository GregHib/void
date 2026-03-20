package content.area.morytania.mos_le_harmless

import content.entity.player.dialogue.type.choice
import content.skill.firemaking.Light
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class MosLeHarmlessCave : Script {
    init {
        objectOperate("Enter", "cave_entrance_mos_le_harmless") {
            choice("Do you still want to proceed?") {
                option("Yes, I think I am fully prepared.") {
                    open("fade_in")
                    tele(3748, 9373)
                }
                option("No, I think I need to go get food, weapons and a light source.")
            }
        }

        npcCondition("witchwood_icon") { target -> target is Player && target.equipped(EquipSlot.Amulet).id == "witchwood_icon" }
        npcCondition("no_witchwood_icon") { target -> target is Player && target.equipped(EquipSlot.Amulet).id != "witchwood_icon" }

        entered("mos_le_harmless_cave") {
            if (Light.hasLightSource(this)) {
                open("level_one_darkness")
            } else {
                open("level_three_darkness")
                timers.start("insect_swarm")
            }
        }

        exited("mos_le_harmless_cave") {
            close("level_one_darkness")
            close("level_three_darkness")
        }
    }
}
