package content.area.asgarnia.dwarven_mines

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class Hura : Script {

    init {
        npcOperate("Talk-to", "hura") { (target) ->
            npc<Happy>("'Ello, $name.")
            player<Quiz>("Hello, what's that you've got there?")
            npc<Quiz>("A crossbow, are you interested?")
            player<Quiz>("Maybe, are they any good?")
            npc<Laugh>("Are they any good?! They're dwarven engineering at its best!")
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Quiz>("How do I make one for myself?") {
                npc<Neutral>("Well, firstly, you'll need to chop yourself some wood, then use a knife on the wood to whittle out a nice crossbow stock like these here.")
                player<Quiz>("Wood fletched into stock... check.")
                npc<Neutral>("Then get yourself some metal and a hammer and smith yourself some limbs for the bow, mind that you use the right metals and woods though as some wood is too light to use with some metal and vice versa.")
                player<Quiz>("Which goes with which?")
                npc<Neutral>("Wood and Bronze as they're basic materials. Oak and Blurite, Willow and Iron, Steel and Teak, Mithril and Maple, Adamantite and Mahogany and finally Runite and Yew.")
                player<Happy>("Ok, so I have my stock and a pair of limbs... what now?")
                npc<Happy>("Simply take a hammer and smack the limbs firmly onto the stock. You'll then need a string, only they're not the same as normal bows. You'll need to dry some large animal's meat to get sinew, then spin that on a spinning")
                npc<Happy>("wheel, it's the only thing we've found to be strong enough for a crossbow.")
                choice {
                    option<Quiz>("What about magic logs?") {
                        npc<Neutral>("Well... I don't rightly know... us dwarves don't work with magic, we prefer gold and rock. Much more stable. I guess you could ask the humans at the Rangers' Guild to see if they can do something, but I don't want")
                        npc<Neutral>("anything to do with it!")
                        player<Happy>("Thanks for telling me. Bye!")
                        npc<Happy>("Take care, straight shooting.")
                    }
                    option<Happy>("Thanks for telling me. Bye!") {
                        npc<Happy>("Take care, straight shooting.")
                    }
                }
            }
            option<Quiz>("What about ammo?") {
                npc<Neutral>("You can smith yourself lots of different bolts, don't forget to flight them with feathers like you do arrows though. There's also the option of tipping any untipped bolt with gems then")
                npc<Neutral>("enchanting them with runes. This can have some pretty powerful effects.")
                player<Quiz>("Oh my poor bank, how will I store all those?!")
                npc<Neutral>("Find Hirko in Keldagrim, he also sells crossbow parts and I'm sure he has something you can use to store bolts in.")
                player<Happy>("Thanks for the info.")
            }
            option<Happy>("Thanks for telling me. Bye!") {
                npc<Happy>("Take care, straight shooting.")
            }
        }
    }
}
