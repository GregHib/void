package content.area.misthalin.lumbridge.blood_pact

import content.entity.combat.dead
import content.entity.combat.killer
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.LookDown
import content.entity.player.dialogue.Neutral
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

class Caitlin : Script {
    init {
        npcAfterDeath("caitlin_attackable") {
            dead = false
            mode = EmptyMode
            huntMode = ""
            levels.restore(Skill.Constitution)
            val player = killer as? Player
            if (player != null) {
                player["blood_pact_caitlin"] = "defeated"
                val original = tile.minus(player.instanceOffset())
                player["blood_pact_caitlin_tile"] = original.id
                player.refreshQuestJournal()
            }
            transform("caitlin_defeated")
        }

        npcOperate("Talk-to", "caitlin_defeated") { (target) ->
            npc<Angry>("What are you waiting for? Finish me!")
            initialOptions(target)
        }
    }

    suspend fun Player.initialOptions(target: NPC) {
        choice {
            option<Neutral>("I have some questions.") {
                npc<Angry>("What?")
                questionToCaitlin(target)
            }
            option<Angry>("Time for you to die!") {
                set("blood_pact_caitlin", "killed")
                target.anim("caitlin_death")
                delay(2)
                open("fade_out")
                delay(3)
                NPCs.remove(target)
                FloorItems.add(instanceOffset().tile(3864, 5538, 1), "caitlins_staff", disappearTicks = 300, owner = this)
                set("blood_pact", "reese")
                refreshQuestJournal()
                xeniaAfterChoice()
            }
            option<Neutral>("I'm not killing you. Just give me your stuff and get out of here.") {
                set("blood_pact_caitlin", "spared")
                open("fade_out")
                target.anim("caitlin_getUp")
                delay(3)
                NPCs.remove(target)
                FloorItems.add(instanceOffset().tile(3864, 5538, 1), "caitlins_staff", disappearTicks = 300, owner = this)
                set("blood_pact", "reese")
                refreshQuestJournal()
                xeniaAfterChoice()
            }
        }
    }

    suspend fun Player.questionToCaitlin(target: NPC) {
        choice {
            option<Neutral>("Who are you?") {
                npc<Angry>("I am the wizard Caitlin.")
                questionToCaitlin(target)
            }
            option<Neutral>("Who are the others?") {
                npc<Angry>("Reese used to be an acolyte at Lumbridge Church. He and I came up with this whole idea.")
                when (get<String>("blood_pact_kayle")) {
                    "spared" -> npc<Angry>("Kayle's just some idiot Reese roped into helping us. I heard you let him go. It's more than he deserved. He's useless.")
                    "killed" -> npc<Angry>("Kayle was just some idiot Reese roped into helping us. I heard you killed him and I can't say I mind. If I'd had my way we'd have used him as the sacrifice.")
                }
                questionToCaitlin(target)
            }
            option<Neutral>("What were you planning to do down here?") {
                npc<Angry>("Idiot hero! You don't even know what this place is, do you?")
                npc<Angry>("This is the tomb of Dragith Nurn!")
                npc<Angry>("Dragith Nurn was a necromancer. He lived in Lumbridge decades ago.")
                npc<Angry>("He kept his necromancy secret. Everyone thought he was just a wealthy nobleman and wizard. He paid for these catacombs to be built, and he's interred here in a special tomb.")
                npc<Angry>("Reese was an acolyte here at the church. He learned that Dragith Nurn was buried here.")
                npc<Angry>("I was a student at the Wizards' Tower. In the library, I discovered a note left by Dragith Nurn.")
                npc<Angry>("The body of a necromancer contains powerful magic. We learned we could perform a ritual on his tomb to unlock the secrets of his work.")
                npc<Angry>("We would have gained mastery over life and death!")
                questionToCaitlin(target)
            }
            option<Neutral>("Enough questions.") {
                npc<Angry>("All right. Now finish me!")
                initialOptions(target)
            }
        }
    }

    suspend fun Player.xeniaAfterChoice() {
        val instance = instance()
        if (instance != null) {
            NPCs.remove(NPCs.findOrNull(instance.toLevel(1), "xenia_wounded"))
            // Spawn two tiles south of Caitlin's initial spawn (3864, 5538, 1)
            val xenia = NPCs.add("xenia_wounded", instanceOffset().tile(3864, 5536, 1), Direction.NORTH)
            delay(1)
            open("fade_in")
            xenia.walkTo(instanceOffset().tile(3864, 5540, 1))
            delay(1)
            talkWith(xenia) {
                when (get<String>("blood_pact_caitlin")) {
                    "spared" -> npc<LookDown>("The second cultist came past me on her way out. I don't think she'll be any more trouble. I'm glad you didn't have to kill her.")
                    "killed" -> npc<LookDown>("I heard you killed the second cultist. It's a pity it had to come to that...but I'm not questioning your judgment.")
                }
                npc<LookDown>("I think the third cultist is a swordsman. Magic is the best thing to use against melee fighters. Speak to me if you need any help.")
            }
        }
    }
}
