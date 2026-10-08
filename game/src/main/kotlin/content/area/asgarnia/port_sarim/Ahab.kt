package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male

class Ahab : Script {

    init {
        npcOperate("Talk-to", "ahab_port_sarim") {
            npc<Drunk>("Arrr, matey!")
            menu()
        }

        takeable("ahabs_beer") { _, _ ->
            npc<Angry>("ahab_port_sarim", "Oi matey, leave my beer alone!")
            player<Shifty>("Sorry!")
            null
        }

        onFloorItemApproach("modern_spellbook:telekinetic_grab", "ahabs_beer") {
            npc<Angry>("ahab_port_sarim", "Oi matey, no casting spells on my beer!")
        }
    }

    private suspend fun Player.menu() {
        choice("Select an Option") {
            if (!questCompleted("pirates_treasure")) {
                option<Neutral>("I'm looking for Redbeard Frank.") {
                    npc<Happy>("Redbeard Frank ye say? He be outside. Says he likes the feel of the wind on his cheeks.")
                    player<Neutral>("Thanks.")
                }
            }
            option<Neutral>("Arrr!") {
                npc<Drunk>("Arrr, matey!")
                menu()
            }
            option<Quiz>("Are you going to sit there all day?") {
                askAboutShip()
            }
            option("Do you want to trade?") {
                player<Quiz>("Do you have anything for trade?")
                npc<Neutral>("Nothin' at the moment, but then again the Customs Agents are on the warpath right now.")
            }
        }
    }

    private suspend fun Player.askAboutShip() {
        npc<Angry>("Aye, I am. I canna walk, ye see.")
        player<Quiz>("What's stopping you from walking?")
        npc<Sad>("Arrr, I 'ave only the one leg! I lost its twin when my last ship went down.")
        player<Quiz>("But I can see both your legs!")
        npc<Sad>("Nay, young ${if (male) "laddie" else "lassie"}, this be a false leg. For years I had me a sturdy wooden peg-leg, but now I wear this dainty little feller.")
        npc<Neutral>("Yon peg-leg kept getting stuck in the floorboards.")
        player<Unamused>("Right...")
        npc<Confused>("Perhaps a ${if (male) "bright young laddie" else "bonnie young lassie"} like yerself would like to help me? I be needing another ship to go a-hunting my enemy.")
        if (questCompleted("dragon_slayer")) {
            player<Neutral>("Well, I do have a ship that I'm not using. It's the Lady Lumbridge.")
            npc<Happy>("Arrr! That ship be known to me, and a fine lass she is.")
            player<Neutral>("I suppose she might be...")
            npc<Neutral>("So would ye be kind enough to let me take her out to sea?")
            player<Quiz>("I had to pay 2000 gp for that ship. Have you got that much?")
        } else {
            player<Quiz>("Hmmm. And can you afford another ship?")
        }
        npc<Neutral>("Nay, I have nary a penny to my name. All my worldly goods went down with me old ship.")
        player<Quiz>("So you're actually asking me to give you a free ship.")
        npc<Happy>("Arrr! Would ye be so kind?")
        player<Angry>("No I jolly well wouldn't!")
        npc<Sad>("Arrr.")
    }
}
