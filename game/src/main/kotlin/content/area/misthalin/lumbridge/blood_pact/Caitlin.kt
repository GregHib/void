package content.area.misthalin.lumbridge.blood_pact

import content.entity.combat.dead
import world.gregs.voidps.engine.Script
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.instance
import content.quest.instanceOffset
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.type.Direction

class Caitlin : Script {
    init {
        npcAfterDeath("caitlin_attackable") {
            dead = false
            mode = EmptyMode
            levels.restore(Skill.Constitution)
            set("blood_pact_caitlin", "defeated")
            transform("caitlin_defeated")
        }

        npcOperate("Talk-to", "caitlin_defeated") { (target) ->
            npc<Scared>("Are - are you going to kill me?")
            initialOptions(target)
        }
    }

    suspend fun Player.initialOptions(target : NPC) {
        choice {
            option<Neutral>("I have some questions.") {
                npc<Scared>("Y-yes! I'll tell you anything!")
                questionTocaitlin(target)
            }
            option<Angry>("Yes. Now die!") {
                set("blood_pact_caitlin", "killed")
                anim("caitlin_death")
                open("fade_out")
                delay(3)
                NPCs.remove(target)
                FloorItems.add(instanceOffset().tile(3877, 5543, 1), "caitlins_sling", disappearTicks = 300, owner = this)
                open("fade_in")
                //xenia : "It's a pity you had to kill that man...but I'm not questioning your judgment."
            }
            option<Neutral>("No. Just give me your stuff and get out of here.") {
                set("blood_pact_caitlin", "spared")
                anim("caitlin_getUp")
                //TODO: find anim and delay removal
                NPCs.remove(target)
                val caitlin = NPCs.add("caitlin_cutscene", instanceOffset().tile(3877, 5543, 1), Direction.NORTH)
                caitlin.walkTo(instanceOffset().tile(3876, 5542, 1))
                caitlin.walkTo(instanceOffset().tile(3876, 5531, 1))
                delay(2)
                open("fade_out")
                delay(3)
                NPCs.remove(caitlin)
                FloorItems.add(instanceOffset().tile(3877, 5543, 1), "caitlins_sling", disappearTicks = 300, owner = this)
                open("fade_in")
                //xenia : "I don't think that cultist will be any more trouble. I'm glad you didn't have to kill him."

            }
        }
    }

    suspend fun Player.questionTocaitlin(target : NPC) {
        choice {
            option<Neutral>("Who are you?") {
                npc<Scared>("I- My name's caitlin. I'm a ranger. Well, I'd been practising the chargebow... I guess I wasn't as good as I'd thought.")
                questionTocaitlin(target)
            }
            option<Neutral>("Who are the others?") {
                npc<Scared>("Reese is the leader. All this, the blood pact, it was his idea. He doesn't know magic but he's a strong fighter.")
                npc<Scared>("Caitlin is a wizard. She was a student at the Wizards' Tower, but she left. She wanted to study dark magic.")
                questionTocaitlin(target)
            }
            option<Neutral>("What were you planning to do down here?") {
                npc<Scared>("I - I don't really know! Honestly!")
                npc<Scared>("Listen...Reese used to be an acolyte at the church here. He discovered something about these catacombs; I don't know what. Something about how they were built, I think.")
                npc<Scared>("Caitlin was a student at the Wizards' Tower. She found something too, in the ruins of the old tower, from back when Zamorakian wizards used it.")
                npc<Scared>("Caitlin and Reese put what they'd found together. They said they'd discovered a ritual they could perform, something that could give them power over life and death.")
                npc<Scared>("We made a blood pact, the three of us. So that we'd be in it together, whatever happened.")
                npc<Scared>("Then we kidnapped Ilona. She was another apprentice from the Wizards' Tower, someone Caitlin had known there.")
                npc<Scared>("Reese and Caitlin are going down there to perform the ritual. I don't - I don't know what it involves.")
                whatPlans(target)
            }
            option<Neutral>("Enough questions.") {
                npc<Scared>("Are - are you going to kill me now?")
                initialOptions(target)
            }
        }
    }

    suspend fun Player.whatPlans(target : NPC) {
        choice {
            option<Neutral>("And you just went along with this?") {
                npc<Scared>("The blood pact! We'd made a blood pact, and Reese said that bound me to him. It meant I had to do anything he said. He...he said he could curse me.")
                questionTocaitlin(target)
            }
            option<Neutral>("Who are you?") {
                npc<Scared>("I- My name's caitlin. I'm a ranger. Well, I'd been practising the chargebow... I guess I wasn't as good as I'd thought.")
                questionTocaitlin(target)
            }
            option<Neutral>("Who are the others?") {
                npc<Scared>("Reese is the leader. All this, the blood pact, it was his idea. He doesn't know magic but he's a strong fighter.")
                npc<Scared>("Caitlin is a wizard. She was a student at the Wizards' Tower, but she left. She wanted to study dark magic.")
                questionTocaitlin(target)
            }
            option<Neutral>("Enough questions.") {
                npc<Scared>("Are - are you going to kill me now?")
                initialOptions(target)
            }
        }
    }
}