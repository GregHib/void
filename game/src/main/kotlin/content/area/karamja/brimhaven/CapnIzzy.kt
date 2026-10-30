package content.area.karamja.brimhaven

import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class CapnIzzy : Script {
    init {
        npcOperate("Talk-to", "capn_izzy_no_beard_brimhaven") {
            player<Happy>("Ahoy Cap'n!")
            npc<Happy>("Ahoy there!")
            npc<Neutral>("parrot_brimhaven", "Avast ye scurvy swabs!")
            player<Confused>("Huh?")
            npc<Happy>("Don't mind me parrot, he's Cracked Jenny's Tea Cup!")
            menu()
        }

        npcOperate("Pay", "capn_izzy_no_beard_brimhaven") {
            payEntrance()
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Neutral>("What is this place?") {
                npc<Neutral>("This, me hearty, is the entrance to the Brimhaven Agility Arena!")
                npc<Neutral>("I were diggin for buried treasure when I found it! Amazed I was! It was a sight to behold!")
                npc<Neutral>("It were the biggest thing I'd ever seen! It must've been at least a league from side to side!")
                npc<Neutral>("It made me list, I were that excited!")
                npc<Neutral>("I'd found a huge cave with all these platforms. I reckon it be an ancient civilisation that made it. I had to be mighty careful as there was these traps everywhere! Dangerous it was!")
                npc<Neutral>("Entrance is only $ENTRANCE_FEE coins!")
                menu()
            }
            option<Neutral>("What do I do in the arena?") {
                npc<Neutral>("Well, me hearty, it's simple. Ye can cross between two platforms by using the traps or obstacles strung across 'em. Try and make your way to the pillar that is indicated by the flashing arrow.")
                npc<Neutral>("Ye receive tickets for tagging more than one pillar in a row. So ye won't get a ticket from the first pillar but ye will for every platform ye tag in a row after that.")
                npc<Neutral>("If ye miss a platform ye will miss out on the next ticket so try and get every platform you can! When ye be done, take the tickets to Jackie over there and she'll exchange them for more stuff!")
                player<Neutral>("Thanks!")
                menu()
            }
            option<Neutral>("I'd like to use the Agility Arena, please.") {
                npc<Neutral>("Aye, entrance be $ENTRANCE_FEE coins.")
                npc<Neutral>("parrot_brimhaven", "Pieces of eight!")
                npc<Amazed>("A word of warning me hearty! There are dangerous traps down there!")
                choice {
                    option<Neutral>("Ok, here's $ENTRANCE_FEE coins.") {
                        payEntrance()
                    }
                    option<Neutral>("Never mind.")
                }
            }
            option<Quiz>("What is that cape you're wearing?") {
                skillcapeMasterDialogue(Skill.Agility, "as agile as possible")
            }
            option<Neutral>("See you later.")
        }
    }

    private suspend fun Player.payEntrance() {
        if (get("brimhaven_arena_paid", false)) {
            npc<Neutral>("capn_izzy_no_beard_brimhaven", "Avast there, ye've already paid!")
            return
        }
        if (!inventory.remove("coins", ENTRANCE_FEE)) {
            message("You don't have the $ENTRANCE_FEE coin entrance fee.")
            return
        }
        set("brimhaven_arena_paid", true)
        message("You give Cap'n Izzy the $ENTRANCE_FEE coin entrance fee.")
        item("coins_8", "You give Cap'n Izzy the $ENTRANCE_FEE coin entrance fee.")
    }

    companion object {
        private const val ENTRANCE_FEE = 200
    }
}
