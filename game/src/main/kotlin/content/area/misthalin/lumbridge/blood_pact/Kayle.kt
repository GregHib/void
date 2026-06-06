package content.area.misthalin.lumbridge.blood_pact

import content.entity.combat.dead
import content.entity.combat.killer
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.LookDown
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.instance
import content.quest.instanceOffset
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.type.Direction

class Kayle : Script {
    init {
        npcAfterDeath("kayle_attackable") {
            dead = false
            mode = EmptyMode
            huntMode = ""
            levels.restore(Skill.Constitution)
            val player = killer as? Player
            if (player != null) {
                player["blood_pact_kayle"] = "defeated"
                player["blood_pact"] = "kayle"
                val original = tile.minus(player.instanceOffset())
                player["blood_pact_kayle_tile"] = original.id
                player.refreshQuestJournal()
            }
            transform("kayle_defeated")
        }

        npcOperate("Talk-to", "kayle_defeated") { (target) ->
            npc<Scared>("Are - are you going to kill me?")
            initialOptions(target)
        }
    }

    suspend fun Player.initialOptions(target: NPC) {
        choice {
            option<Neutral>("I have some questions.") {
                npc<Scared>("Y-yes! I'll tell you anything!")
                questionToKayle(target)
            }
            option<Angry>("Yes. Now die!") {
                set("blood_pact_kayle", "killed")
                target.anim("kayle_death")
                delay(2)
                open("fade_out")
                delay(3)
                NPCs.remove(target)
                FloorItems.add(instanceOffset().tile(3877, 5543, 1), "kayles_sling", disappearTicks = 300, owner = this)
                set("blood_pact", "caitlin")
                refreshQuestJournal()
                xeniaAfterChoice()
            }
            option<Neutral>("No. Just give me your stuff and get out of here.") {
                set("blood_pact_kayle", "spared")
                open("fade_out")
                target.anim("kayle_getUp")
                delay(3)
                NPCs.remove(target)
                FloorItems.add(instanceOffset().tile(3877, 5543, 1), "kayles_sling", disappearTicks = 300, owner = this)
                set("blood_pact", "caitlin")
                refreshQuestJournal()
                xeniaAfterChoice()
            }
        }
    }

    suspend fun Player.questionToKayle(target: NPC) {
        choice {
            option<Neutral>("Who are you?") {
                npc<Scared>("I- My name's Kayle. I'm a ranger. Well, I'd been practising the chargebow... I guess I wasn't as good as I'd thought.")
                questionToKayle(target)
            }
            option<Neutral>("Who are the others?") {
                npc<Scared>("Reese is the leader. All this, the blood pact, it was his idea. He doesn't know magic but he's a strong fighter.")
                npc<Scared>("Caitlin is a wizard. She was a student at the Wizards' Tower, but she left. She wanted to study dark magic.")
                questionToKayle(target)
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

    suspend fun Player.xeniaAfterChoice() {
        val instance = instance()
        if (instance != null) {
            NPCs.remove(NPCs.findOrNull(instance.toLevel(1), "xenia_wounded"))
            val xenia = NPCs.add("xenia_wounded", instanceOffset().tile(3877, 5538, 1), Direction.NORTH)
            delay(1)
            open("fade_in")
            xenia.walkTo(instanceOffset().tile(3877, 5541, 1))
            delay(1)
            talkWith(xenia) {
                val kayleStatus = get<String>("blood_pact_kayle")
                when (kayleStatus) {
                    "spared" -> npc<LookDown>("I don't think that cultist will be any more trouble. I'm glad you didn't have to kill him.")
                    "killed" -> npc<LookDown>("It's a pity you had to kill that man...but I'm not questioning your judgment.")
                }
                npc<LookDown>("I think the second cultist was using magic. You should use a ranged weapon to defeat magic-users. Ask me if you need any help.")
            }
        }
    }

    suspend fun Player.whatPlans(target: NPC) {
        choice {
            option<Neutral>("And you just went along with this?") {
                npc<Scared>("The blood pact! We'd made a blood pact, and Reese said that bound me to him. It meant I had to do anything he said. He...he said he could curse me.")
                questionToKayle(target)
            }
            option<Neutral>("Who are you?") {
                npc<Scared>("I- My name's Kayle. I'm a ranger. Well, I'd been practising the chargebow... I guess I wasn't as good as I'd thought.")
                questionToKayle(target)
            }
            option<Neutral>("Who are the others?") {
                npc<Scared>("Reese is the leader. All this, the blood pact, it was his idea. He doesn't know magic but he's a strong fighter.")
                npc<Scared>("Caitlin is a wizard. She was a student at the Wizards' Tower, but she left. She wanted to study dark magic.")
                questionToKayle(target)
            }
            option<Neutral>("Enough questions.") {
                npc<Scared>("Are - are you going to kill me now?")
                initialOptions(target)
            }
        }
    }
}
