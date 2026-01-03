package content.area.kandarin.ardougne

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.proj.shoot
import content.quest.questCompleted
import content.skill.runecrafting.EssenceMine
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.queue.softQueue

class WizardCromperty : Script {

    init {
        npcOperate("Talk-to", "wizard_cromperty_*") { (target) ->
            npc<Neutral>("Hello there. My name is Cromperty. I am a wizard, and an inventor.")
            npc<Neutral>("You must be ElderCadence. My good friend Sedridor has told me about you. As both wizard and inventor, he has aided me in my great invention!")
            player<Neutral>("Hello there.")
            choice {
                option<Neutral>("Two jobs? That's got to be tough.") {
                    npc<Happy>("Not when you combine them it isn't! I invent MAGIC things!")
                    choice {
                        whatHaveYouInvented(target)
                        option<Confused>("Well, I shall leave you to your inventing.") {
                            npc<Neutral>("Thanks for dropping by! Stop again anytime!")
                        }
                    }
                }
                whatHaveYouInvented(target)
                if (questCompleted("rune_mysteries")) {
                    option<Quiz>("Can you teleport me to the Rune Essence Mine?") {
                        EssenceMine.teleport(target, this)
                    }
                }
            }
        }

        npcOperate("Teleport", "wizard_cromperty_*") { (target) ->
            if (questCompleted("rune_mysteries")) {
                EssenceMine.teleport(target, this)
            } else {
                message("You need to have completed the Rune Mysteries Quest to use this feature.")
            }
        }
    }

    fun ChoiceOption.whatHaveYouInvented(target: NPC) {
        option<Quiz>("So what have you invented?") {
            npc<Happy>("Ah! My latest invention is my patent pending teleportation block! It emits a low level magical signal, that will allow me to locate it anywhere in the world, and teleport anything")
            npc<Happy>("directly to it! I hope to revolutionise the entire teleportation system! Don't you think I'm great? Uh, I mean it's great?")
            choice {
                option<Quiz>("So where is the other block?") {
                    npc<Confused>("Well... Hmm. I would guess somewhere between here and the Wizards' Tower in Misthalin. All I know is that it hasn't got there yet as the wizards there would have contacted me.")
                    npc<Sad>("I'm using the GPDT for delivery. They assured me it would be delivered promptly.")
                    choice {
                        teleportMe(target)
                        option<Quiz>("Who are the GPDT?") {
                            npc<Neutral>("The Gielinor Parcel Delivery Team. They come very highly recommended. Their motto is: 'We aim to deliver your stuff at some point after you have paid us!'")
                        }
                    }
                }
                teleportMe(target)
                option<Neutral>("Well done, that's very clever.") {
                    npc<Happy>("Yes it is isn't it? Forgive me for feeling a little smug, this is a major breakthrough in the field of teleportation!")
                }
            }
        }
    }

    fun ChoiceOption.teleportMe(target: NPC) {
        option<Quiz>("Can I be teleported please?") {
            npc<Happy>("By all means! I'm afraid I can't give you any specifics as to where you will come out however. Presumably wherever the other block is located.")
            choice {
                option<Neutral>("Yes, that sounds good. Teleport me!") {
                    npc<Happy>("Okey dokey! Ready?")
                    gfx("curse_impact")
                    target.gfx("curse_cast")
                    target.say("Dipsolum sententa sententi!")
                    target.shoot("curse", tile, offset = 64)
                    softQueue("cromperty_teleport", 2) {
                        player.tele(2649, 3271)
                    }
                }
                option<Shock>("That sounds dangerous. Leave me here.") {
                    npc<Neutral>("As you wish.")
                }
            }
        }
    }
}
