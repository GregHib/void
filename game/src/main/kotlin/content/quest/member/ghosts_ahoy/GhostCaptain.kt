package content.quest.member.ghosts_ahoy

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class GhostCaptain : Script {
    init {
        npcOperate("Talk-To", "ahoy_ghost_captain_*") { (target) ->
            if (!checkGhostspeak()) return@npcOperate
            if (equipment.contains("bedsheet") || equipment.contains("bedsheet_ectoplasm")) {
                if (equipment.contains("bedsheet_ectoplasm")) {
                    npc<Neutral>("Is that your bedsheet covered in filthy slime?")
                    player<Neutral>("I can say in all honesty that it is not.")
                    npc<Neutral>("Well, whoever it belongs to, it's not coming on this ship!")
                } else {
                    npc<Neutral>(
                        "Would you like to visit Dragontooth Island? If so, I recommend you " +
                            "take off that sheet first. The winds are very strong out at sea.",
                    )
                }
                return@npcOperate
            }
            if (target.id == "ahoy_ghost_captain_1") {
                npc<Neutral>(
                    "Would you like to visit Dragontooth Island? It is a most pleasant island " +
                        "in the sea between Morytania and the lands of the east. It'll cost " +
                        "you 25 ectotokens for the return trip.",
                )
                askToSail(toDragontooth = true)
            } else {
                offerReturn()
            }
        }

        npcOperate("Travel", "ahoy_ghost_captain_1") {
            if (!checkGhostspeak()) return@npcOperate
            sailIfAble(toDragontooth = true)
        }

        npcOperate("Travel", "ahoy_ghost_captain_2") {
            if (!checkGhostspeak()) return@npcOperate
            sail(toDragontooth = false)
        }
    }

    private suspend fun Player.askToSail(toDragontooth: Boolean) {
        if (!inventory.contains("ecto_token", 25) && toDragontooth) {
            player<Neutral>("Not now, thanks.")
            return
        }
        choice {
            option<Happy>("Okay, here's 25 ectotokens. Let's go.") {
                sailIfAble(toDragontooth)
            }
            option<Bored>("Not now, thanks.")
        }
    }

    private suspend fun Player.offerReturn() {
        choice {
            option<Bored>("Can you take me back to Phasmatys, now?") {
                npc<Neutral>("Yes, climb in.")
                sail(toDragontooth = false)
            }
            option<Quiz>("There isn't much on this island, is there?") {
                npc<Neutral>("Beautiful isn't it?")
                player<Neutral>("But it cost me a lot of ectotokens to get here!")
                npc<Neutral>("Worth every token, if you ask me.")
                offerReturn()
            }
            option<Confused>("Actually, I don't want anything.")
        }
    }

    private suspend fun Player.sailIfAble(toDragontooth: Boolean) {
        if (toDragontooth && !inventory.contains("ecto_token", 25)) {
            npc<Confused>("A return trip to Dragontooth Island costs 25 ectotokens.")
            return
        }
        sail(toDragontooth)
    }

    private suspend fun Player.sail(toDragontooth: Boolean) {
        if (toDragontooth) {
            inventory.remove("ecto_token", 25)
        }
        val destination = if (toDragontooth) Tile(3792, 3559, 0) else Tile(3702, 3487, 0)
        open("fade_out")
        interfaces.sendText(
            "fade_out",
            "text",
            "After a long boat trip you ${if (toDragontooth) {
                "arrive at Dragontooth Island"
            } else {
                "return to Port Phasmatys"
            }}.",
        )
        delay(5)
        tele(destination)
        open("fade_in")
        delay(3)
    }
}
