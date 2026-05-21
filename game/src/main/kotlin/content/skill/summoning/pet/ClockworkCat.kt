package content.skill.summoning.pet

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class ClockworkCat : Script {

    init {
        npcOperate("Shoo away", "pet_clockwork_cat_baby") { interact ->
            if (pet?.index != interact.target.index) {
                message("This isn't your pet.")
                return@npcOperate
            }
            choice("Are you sure you want to shoo away the clockwork cat?") {
                option<Quiz>("Yes I am.") {
                    if (pet?.index != interact.target.index) return@option
                    say("Shoo!")
                    interact.target.say("Whir...")
                    dismissPet()
                    message("The clockwork cat winds down and stops.")
                }
                option<Sad>("No I'm not.") {
                    message("You choose not to shoo away the clockwork cat.")
                }
            }
        }
    }
}
