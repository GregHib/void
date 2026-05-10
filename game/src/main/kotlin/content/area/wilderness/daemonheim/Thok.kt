package content.area.wilderness.daemonheim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Thok : Script {
    init {
        npcOperate("Talk-to", "thok_master_of_dungeoneering") {
            npc<Angry>("What do you want with Thok?")
            choice {
                whatIsThisPlace()
                whatCanIDo()
                whatIsThatCape()
            }
        }
    }

    private fun ChoiceOption.whatIsThatCape() {
        option<Quiz>("What is that cape beside you?") {
            npc<Angry>("Ah, this Dungeoneering skillcape. It mark bearer as true master of Demon Halls.")
            npc<Sad>("Though Thok has been far into Daemonheim, no-one believe him. So, Thok must guard skillcape, and offer it to brave warriors who find bottom of this place.")
            choice {
                whatIsThisPlace()
                whatCanIDo()
                daemonheim()
            }
        }
    }

    private fun ChoiceOption.daemonheim() {
        option<Quiz>("Daemonheim?") {
            npc<Angry>("It mean Demon Halls. Name not quite right, as there worse things than demons in Daemonheim. It less a hall and more a pit, too. Some say it has no end, but Thok know better.")
            choice {
                whatIsThisPlace()
                whatCanIDo()
                whatIsThatCape()
            }
        }
    }

    private fun ChoiceOption.whatCanIDo() {
        option<Quiz>("What can I do here?") {
            npc<Angry>("Up path is Daemonheim. It hold more treasure than Thok can put in his pockets, which is lucky for you, but it guarded by countless monsters, which is unlucky for you. First-timers should talk to tutor before going into castle.")
            choice {
                whatIsThisPlace()
                whatIsThatCape()
            }
        }
    }

    private fun ChoiceOption.whatIsThisPlace() {
        option<Quiz>("What is this place?") {
            npc<Angry>("This is camp. This where warriors rest and prepare for onslaught.")
            npc<Angry>("Thok wait here to reward those who survive Daemonheim, who show might as great as Thok's.")
            choice {
                daemonheim()
                whatCanIDo()
                whatIsThatCape()
            }
        }
    }
}