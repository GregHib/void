package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class PartyPete : Script {

    init {
        npcOperate("Talk-to", "party_pete") { (target) ->
            player<Happy>("Hi!")
            npc<Happy>("Hi! I'm Party Pete. Welcome to the Party Room!")
            menu(target)
        }
    }

    private suspend fun Player.menu(target: NPC) {
        choice("Select an option") {
            option<Quiz>("So, what's this room for?") {
                npc<Happy>("This room is for partying the night away!")
                player<Quiz>("How do you have a party?")
                npc<Neutral>("Get a few mates round, get the beers in and have fun! Some players organise parties so keep an eye open!")
                player<Happy>("Woop! Thanks Pete!")
            }
            option<Quiz>("What's the big lever over there for?") {
                npc<Neutral>("Simple. With the lever you can do some fun stuff.")
                player<Quiz>("What kind of stuff?")
                npc<Neutral>("A balloon drop costs 1000 gold. For this, you get 200 balloons dropped across the whole of the party room. You can then have fun popping the balloons!")
                npc<Neutral>("Any items in the Party Drop Chest will be put into balloons as soon as you pull the lever.")
                npc<Neutral>("When the balloons are released, you can burst them to get at the items!")
                npc<Laugh>("For 500 gold, you can summon the Party Room Knights, who will dance for your delight. Their singing isn't a delight, though!")
            }
            option<Quiz>("What's the gold chest for?") {
                npc<Neutral>("Any items in the chest will be dropped inside the balloons when you pull the lever.")
                player<Happy>("Cool! Sounds like a fun way to do a drop party.")
                npc<Neutral>("Exactly! A word of warning, though. Any items that you put into the chest can't be taken out again, and it costs 1000 gold pieces for each drop party.")
            }
            option<Quiz>("I wanna party!") {
                npc<Happy>("I've won the Dance Trophy at the Kandarin Ball three years in a trot!")
                player<Happy>("Show me your moves Pete!")
                delay(2)
                target.anim("partyroom_dance")
            }
            option("More.") {
                more(target)
            }
        }
    }

    private suspend fun Player.more(target: NPC) {
        choice("Select an option") {
            option<Happy>("I love your hair!") {
                npc<Happy>("Isn't it groovy? I liked it so much, I had extras made for my party goers. Would you like to buy one?")
                choice("Would you like to buy one?") {
                    option("Yes.") {
                        openShop("party_petes_emporium")
                    }
                    option("No.")
                }
            }
            option<Quiz>("Why's there a chameleon in here?") {
                npc<Neutral>("Karma's my pet. I got him for Christmas one year. He keeps the Party Room free of flies, and he loves watching me dance. Karma karma karma cha...")
                chameleon()
            }
            option("Back.") {
                menu(target)
            }
        }
    }

    private suspend fun Player.chameleon() {
        choice("Select an option") {
            option<Quiz>("Can you talk to him?") {
                npc<Happy>("Sure, I talk to the little fellow all the time. My Summoning level's not high enough to understand what he says back, but he's still great company.")
                chameleon()
            }
            option("Christmas is over.") {
                player<Confused>("Christmas is over. Why've you still got him hanging around?")
                npc<Happy>("I couldn't chuck the little chappy out! A pet is for life!")
                chameleon()
            }
            option<Quiz>("Aww, that's nice.")
        }
    }
}

// todo make party pete "interact" with karma
