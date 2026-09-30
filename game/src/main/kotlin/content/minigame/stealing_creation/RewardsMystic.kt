package content.minigame.stealing_creation

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class RewardsMystic : Script {

    init {
        npcOperate("Talk-to", "rewards_mystic") {
            menu()
        }

        npcOperate("Exchange", "rewards_mystic") {
            open("stealing_creation_rewards")
        }
    }

    private suspend fun Player.menu() {
        npc<Quiz>("If you've worked hard towards your cause, perhaps you'd be interested in some rewards?")
        choice("What would you like to say?") {
            option<Neutral>("Show me the rewards, please.") {
                open("stealing_creation_rewards")
            }
            option<Quiz>("Tell me about the rewards.") {
                npc<Happy>("By helping your team gather sacred clay, you earn points that you can spend on rewards here. There are two types of reward: tools and equipment.")
                npc<Happy>("All the rewards are made from the same sacred clay that your team works so hard to harvest, which gives them some rather special properties.")
                information()
            }
            option<Neutral>("Never mind.") {
            }
        }
    }

    private suspend fun Player.information() {
        choice("What would you like to say?") {
            option<Quiz>("Tell me more about tool rewards.") {
                npc<Happy>("These hallowed tools are made from sacred clay, and can be transformed into a pickaxe, woodcutting hatchet, harpoon, butterfly net, fletching knife, hammer or needle.")
                npc<Happy>("The morphic tool allows you to decide which tool it will transform into. As you use the tool, you'll get extra experience in that skill.")
                npc<Happy>("The volatile tool is made of more powerful clay, but is more unstable, so although you get more bonus experience, you have no control over which tool it becomes.")
                npc<Happy>("Eventually, either type of tool will run out of sacred power and stop working. You can always come back here and buy more charges for it with points from the primordial realm.")
                information()
            }
            option<Quiz>("Tell me more about equipment rewards.") {
                npc<Happy>("Ah, now these rewards truly reveal the power of the sacred clay. The morphic armour and weapon can adapt to your fighting style.")
                npc<Happy>("When you operate a piece of the morphic armour and choose a new combat style, all currently equipped morphic armour and weapons will change to that style of combat.")
                npc<Happy>("While you are using the equipment, you will gain extra experience in whichever combat skills you are training.")
                npc<Happy>("Eventually, the armour will run out of sacred energy, but you can always come back and earn more charges for it.")
                information()
            }
            option<Neutral>("Show me the rewards, please.") {
                open("stealing_creation_rewards")
            }
            option<Neutral>("Thanks.") {
            }
        }
    }
}
