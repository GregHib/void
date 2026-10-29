package content.area.fremennik_province.rellekka

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class Longships : Script {

    init {
        npcOperate("Travel-Jatizso", "mord_gunnars_rellekka") {
            if (fremennik()) {
                sail("Jatizso", Tile(2421, 3781), "longship_to_jatizso", 14)
            }
        }

        npcOperate("Travel-Rellekka", "mord_gunnars_jatizo") {
            sail("Rellekka", Tile(2644, 3710), "longship_from_jatizso", 14)
        }

        npcOperate("Talk-to", "mord_gunnars_rellekka") {
            npc<Happy>("Would you like to sail to Jatizso?")
            choice {
                option<Happy>("Yes, please!") {
                    if (fremennik()) {
                        sail("Jatizso", Tile(2421, 3781), "longship_to_jatizso", 14)
                    }
                }
                option<Neutral>("No, thank you.")
            }
        }

        npcOperate("Talk-to", "mord_gunnars_jatizo") {
            npc<Happy>("Would you like to sail back to Rellekka?")
            choice {
                option<Happy>("Yes, please!") {
                    sail("Rellekka", Tile(2644, 3710), "longship_from_jatizso", 14)
                }
                option<Neutral>("No, thank you.")
            }
        }

        npcOperate("Travel-Neitiznot", "maria_gunnars_rellekka") {
            if (fremennik()) {
                sail("Neitiznot", Tile(2310, 3782), "longship_to_neitiznot", 15)
            }
        }

        npcOperate("Travel-Rellekka", "maria_gunnars") {
            sail("Rellekka", Tile(2644, 3710), "longship_from_neitiznot", 15)
        }

        npcOperate("Talk-to", "maria_gunnars_rellekka") {
            if (!questCompleted("fremennik_trials")) {
                npc<Sad>("I do not speak to outerlanders. Maybe you should speak with Rellekka's Chieftain.")
                return@npcOperate
            }
            npc<Happy>("Welcome back, $name. Do you have any questions?")
            choice {
                option<Neutral>("Can you ferry me to Neitiznot?") {
                    npc<Happy>("Let's set sail then.")
                    sail("Neitiznot", Tile(2310, 3782), "longship_to_neitiznot", 15)
                }
                option<Happy>("I just stopped to say 'hello'.") {
                    npc<Happy>("Thanks!")
                    player<Neutral>("I may be back later.")
                }
            }
        }

        npcOperate("Talk-to", "maria_gunnars") {
            npc<Happy>("Hi, $name, you should speak with Burgher Mawnis Burowgar in the main chambers. Do you have any questions?")
            choice {
                option<Neutral>("Can you ferry me to Rellekka?") {
                    npc<Neutral>("Sure.")
                    sail("Rellekka", Tile(2644, 3710), "longship_from_neitiznot", 15)
                }
                option<Neutral>("I have nothing to ask you right now.") {
                    npc<Neutral>("Fare thee well.")
                }
            }
        }

        npcOperate("Travel-Miscellania", "sailor_rellekka") {
            if (fremennik()) {
                sail("Miscellania", Tile(2581, 3845), "longship_to_miscellania", 10)
            }
        }

        npcOperate("Travel-Rellekka", "sailor_miscellania") {
            if (fremennik()) {
                sail("Rellekka", Tile(2629, 3693), "longship_from_miscellania", 8)
            }
        }

        npcOperate("Talk-to", "jarvald_rellekka") {
            jarvald(island = false)
        }

        npcOperate("Talk-to", "jarvald_waterbirth_island") {
            jarvald(island = true)
        }

        npcOperate("Travel-Rellekka", "jarvald_waterbirth_island") {
            jarvald(island = true)
        }
    }

    private fun Player.fremennik(): Boolean {
        if (questCompleted("fremennik_trials")) {
            return true
        }
        message("You must have completed The Fremennik Trials quest.")
        return false
    }

    private suspend fun Player.jarvald(island: Boolean) {
        val member = questCompleted("fremennik_trials")
        if (!member) {
            npc<Quiz>("So do you have the 1000 coins for my service, and are you ready to leave now?")
        }
        choice(if (island) "Leave island?" else null) {
            option("YES") {
                if (!member && !inventory.remove("coins", 1000)) {
                    message("You cannot afford that.")
                    npc<Neutral>("Ah, outerlander... I go beyond my duty to allow you to accompany me, and you attempt to swindle me out of a reasonable fee? This is no way even for an outerlander to behave...")
                    return@option
                }
                if (island) {
                    npc<Neutral>("Then let us away; There will be death to bring here another day!")
                    sail("Rellekka", Tile(2620, 3685), "longship_from_waterbirth_island", 8)
                } else {
                    sail("Waterbirth Island", Tile(2544, 3759), "longship_to_waterbirth_island", 9)
                }
            }
            option("NO") {
                if (island) {
                    npc<Laugh>("Ha Ha Ha! A true huntsman at heart!")
                    npc<Happy>("I myself have killed over a hundred of the daggermouths, and did not think it too many!")
                } else {
                    player<Neutral>("No, actually I have some stuff to do here first.")
                    npc<Neutral>("As you wish. Come and see me when your bloodlust needs sating.")
                }
            }
        }
    }

    private suspend fun Player.sail(name: String, tile: Tile, animation: String, ticks: Int) {
        open("fade_out")
        message("You board the longship...")
        delay(2)
        tele(tile)
        open("longship_journey")
        interfaces.sendAnimation("longship_journey", "ship", animation)
        delay(ticks)
        close("longship_journey")
        open("fade_in")
        message("The ship arrives at $name.")
    }
}
