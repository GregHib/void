package content.skill.construction

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

/**
 * Skill hall head trophies which can be talked to and the quest hall's mounted amulet of glory which teleports without charges
 * https://oldschool.runescape.wiki/w/Skill_hall
 * https://oldschool.runescape.wiki/w/Quest_hall
 */
class HouseHalls : Script {
    init {
        objectOperate("Talk-to", "crawling_hand_trophy") {
            player<Neutral>("Hello there little hand.") // TODO proper message
            npc<Neutral>("mounted_crawling_hand", if (inOwnHouse()) "Don't you 'little hand' me, you're the one who stuffed me." else "Your friend gave me quite the handshake.") // TODO proper message
        }

        objectOperate("Talk-to", "cockatrice_head_trophy") {
            player<Neutral>("Lovely plumage.") // TODO proper message
            npc<Neutral>("mounted_cockatrice_head", "Look me in the eye and say that.") // TODO proper message
        }

        objectOperate("Talk-to", "basilisk_head_trophy") {
            player<Neutral>("Do you miss the dungeon?") // TODO proper message
            npc<Neutral>("mounted_basilisk_head", "Not as much as I miss my body.") // TODO proper message
        }

        objectOperate("Talk-to", "kurask_head_trophy") {
            player<Neutral>("How's the view from up there?") // TODO proper message
            npc<Neutral>("mounted_kurask_head", "You'd need a leaf-bladed sword to find out.") // TODO proper message
        }

        objectOperate("Talk-to", "abyssal_head_trophy") {
            player<Neutral>("Comfortable?") // TODO proper message
            npc<Neutral>("mounted_abyssal_demon_head", "Hardly. The Abyss was much cosier than this wall.") // TODO proper message
        }

        objectOperate("Talk-to", "kbd_heads_trophy") {
            player<Neutral>("Hello, all three of you.") // TODO proper message
            npc<Neutral>("mounted_kbd_left_head", "Is it speaking to us?") // TODO proper message
            npc<Neutral>("mounted_kbd_middle_head", "Ignore it, it's the one that put us up here.") // TODO proper message
            npc<Neutral>("mounted_kbd_right_head", "I could still breathe fire on it...") // TODO proper message
        }

        objectOperate("Talk-to", "kq_head_trophy") {
            player<Neutral>("Hello, your majesty.") // TODO proper message
            npc<Neutral>("mounted_kq_head", "Bow before your queen, soft creature.") // TODO proper message
        }

        objectOperate("Rub", "amulet_of_glory_mounted") {
            rubGlory()
        }
    }

    private suspend fun Player.rubGlory() {
        choice("Where would you like to teleport to?") {
            option("Edgeville") {
                gloryTeleport("edgeville_teleport")
            }
            option("Karamja") {
                gloryTeleport("karamja_teleport")
            }
            option("Draynor Village") {
                gloryTeleport("draynor_village_teleport")
            }
            option("Al Kharid") {
                gloryTeleport("al_kharid_teleport")
            }
            option("Nowhere")
        }
    }

    private fun Player.gloryTeleport(area: String) {
        message("You rub the amulet...", ChatType.Filter)
        Teleport.teleport(this, area, "jewellery")
    }
}
