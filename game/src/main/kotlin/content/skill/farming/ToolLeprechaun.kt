package content.skill.farming

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ToolLeprechaun : Script {
    init {
        npcOperate("Talk-to", "tool_leprechaun_alices_farm") {
            npc<Happy>("Ah, 'tis a foine day to be sure! Were yez wantin' me to store yer tools, or maybe ye might be wantin' yer stuff back from me?")
            choice("What would you like to say?") {
                yesPlease()
                option<Talk>("What can you store?") {
                    npc<Happy>("We'll hold onto yer rake, yer seed dibber, yer spade, yer secateurs, yer waterin' can and yer trowel - but mind it's not one of them fancy trowels only archaeologists use!")
                    npc<RollEyes>("We'll take a few buckets off yer hands too, and even yer compost, supercompost an' ultracompost! Also plant cure vials.")
                    npc<Happy>("Aside from that, if ye hands me yer farming produce, I can mebbe change it into banknotes for ye.")
                    npc<Quiz>("So... do ye want to be using the store?")
                    choice("What would you like to say?") {
                        yesPlease()
                        whatDoYouDo()
                        noThanks()
                    }
                }
                whatDoYouDo()
                noThanks()
            }
        }
    }

    private fun ChoiceOption.yesPlease() {
        option("Yes please.") {
            // TODO store interface
        }
    }

    private fun ChoiceOption.noThanks() {
        option<Talk>("No thanks, I'll keep hold of my stuff.") {
            npc<Chuckle>("Ye must be dafter than ye look if ye likes luggin' yer tools everywhere ye goes!")
        }
    }

    private fun ChoiceOption.whatDoYouDo() {
        option("What do you do with the tools you're storing?") {
            player<Quiz>("What do you do with the tools you're storing? They can't possibly all fit in your pockets!")
        }
    }
}