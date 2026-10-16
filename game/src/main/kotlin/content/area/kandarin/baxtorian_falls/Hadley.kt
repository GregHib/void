package content.area.kandarin.baxtorian_falls

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.inventory

class Hadley : Script {
    init {
        npcOperate("Talk-to", "hadley_baxtorian_falls") {
            player<Neutral>("Hello there.")
            if (inventory.contains("book_on_baxtorian")) {
                npc<Neutral>("I hope you're enjoying your stay, there should be lots of useful information in that book: places to go, people to see.")
            } else {
                npc<Neutral>("Are you on holiday? If so you've come to the right place. I'm Hadley the tourist guide, anything you need to know just ask me we have some of the most unspoilt wildlife and scenery in RuneScape.")
                npc<Neutral>("People come from miles around to fish in the clear lakes or to wander the beautiful hillsides.")
                player<Neutral>("It is quite pretty.")
                npc<Neutral>("Surely pretty is an understatement kind ${if (male) "Sir" else "Lady"}. Beautiful, amazing or possibly life-changing would be more suitable wording. Have you seen the Baxtorian waterfall? Named after the elf king who was buried beneath.")
            }
            choice {
                elfKing()
                worthVisit()
                if (quest("waterfall_quest") != "unstarted") {
                    treasure()
                }
                thanks()
            }
        }
    }

    private fun ChoiceOption.elfKing() {
        option<Neutral>("Can you tell me what happened to the elf king?") {
            npc<Sad>("Baxtorian, I guess he died a long, long time ago; it's quite sad really. After defending his kingdom against the invading dark forces from the west,")
            npc<Neutral>("Baxtorian returned to find his wife, Glarial, had been captured by the enemy. This destroyed Baxtorian, after years of searching he became a recluse. In the secret home he had made for Glarial under the waterfall, he")
            npc<Neutral>("never came out and to this day no one has managed to get in.")
            player<Neutral>("What happened to him?")
            npc<Neutral>("No one knows.")
            choice {
                worthVisit()
                nature()
                thanks()
            }
        }
    }

    private fun ChoiceOption.treasure() {
        option<Neutral>("Is there treasure under the waterfall?") {
            npc<Neutral>("Ha ha... Another treasure hunter. Well if there is no one's been able to get to it. They've been searching that river for decades, all to no avail.")
            choice {
                elfKing()
                worthVisit()
                thanks()
            }
        }
    }

    private fun ChoiceOption.nature() {
        option<Neutral>("I don't like nature, it gives me a rash!") {
            npc<Neutral>("That's just silly talk")
        }
    }

    private fun ChoiceOption.worthVisit() {
        option<Neutral>("Where else is worth visiting around here?") {
            npc<Neutral>("Well, there's a wide variety of wildlife, although unfortunately most of it's quite dangerous. Please don't feed the goblins.")
            player<Neutral>("OK")
            npc<Neutral>("There is a lovely spot for a picnic on the hill to the north east, there's a monument to the deceased elven queen Glarial. It really is quite pretty.")
//          TODO osrs only?
//            player<Idle>("Who was Glarial?")
//            npc<Idle>("Baxtorian's wife, the only other person who could also enter the waterfall apart from him. She was queen when this land was inhabited by elven kind.")
//            npc<Idle>("Glarial was kidnapped while Baxtorian was away, but they eventually recovered her body and brought her home to rest.")
//            player<Idle>("That's sad.")
//            npc<Idle>("True, I believe we have a book on Baxtorian and Glarial upstairs if you want to learn more.")
            choice {
                elfKing()
                treasure()
                thanks()
            }
        }
    }

    private fun ChoiceOption.thanks() {
        option<Neutral>("Thanks then, goodbye.") {
            npc<Neutral>("Enjoy your visit.")
        }
    }
}
