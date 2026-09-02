package content.area.misthalin.lumbridge.swamp.chams_of_tears

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.closeTabs
import content.quest.openTabs
import content.quest.quest
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class Juna : Script {
    init {
        adminCommand("test") {
            clearCamera()
            moveCamera(Tile(3233, 9505, 2), height = 700, speed = 5, acceleration = 5)
            turnCamera(Tile(3227, 9493, 2), height = 200, speed = 5, acceleration = 5)
        }

        objectOperate("Talk-to", "juna_tears,juna") { (target) ->
            talkWith(junaNpc())
            if (equipped(EquipSlot.Weapon).id == "stone_bowl") {
                npc<Neutral>("I will not permit you to be in the cave for long. Collect as many tears as you can.")
                choice {
                    option<Neutral>("Okay.")
                    option<Neutral>("I want to get out now.") {
                        exit()
                    }
                }
                return@objectOperate
            }
            if (quest("tears_of_guthix") == "stone_bowl") {
                npc<Happy>("Before you can collect the Tears of Guthix you must make a bowl out of the stone in the cave on the south of the chasm.")
                if (carriesItem("stone_bowl")) {
                    player<Neutral>("I have a bowl.")
                    npc<Neutral>("I will keep your bowl for you, so that you may collect the tears many times in the future.")
                    npc<Neutral>("Now... tell me another story, and I will let you collect the tears for the first time.")
                    questComplete()
                }
                return@objectOperate
            }
            npc<Neutral>("Tell me... a story...")
            choice {
                okay(target)
                option<Quiz>("A story?") {
                    npc<Bored>("I have been waiting here three thousand years, guarding the Tears of Guthix. I serve my master faithfully, but I am bored.")
                    npc<Neutral>("An adventurer such as yourself must have many tales to tell. If you can entertain me, I will let you into the cave for a time.")
                    npc<Neutral>("The more I enjoy your story, the more time I will give you in the cave.")
                    npc<Happy>("Then you can drink of the power of balance, which will make you stronger in whatever area you are weakest.")
                    choice {
                        okay(target)
                        option<Quiz>("What are the Tears of Guthix?") {
                            tearsStory()
                        }
                        option<Neutral>("Not now.")
                    }
                }
                option<Neutral>("You tell me a story.") {
                    val stories = get("juna_stories", 0)
                    choice {
                        option("Tell me about the Tears of Guthix") {
                            tearsStory()
                            if (stories == 0) {
                                set("juna_stories", 1)
                            }
                        }
                        if (stories <= 0) {
                            return@choice
                        }
                        option<Quiz>(if (stories == 1) "Tell me a new story." else "Tell me about the light-creatures") {
                            lightCreatureStory()
                            if (stories == 1) {
                                set("juna_stories", 2)
                            }
                        }
                        if (stories <= 1) {
                            return@choice
                        }
                        option<Quiz>(if (stories == 2) "Tell me a new story." else "Tell me about the Cave Goblins") {
                            goblinStory()
                            if (stories == 2) {
                                set("juna_stories", 3)
                            }
                        }
                    }
                }
                if (Settings["world.additional.messages", false]) {
                    option<Neutral>(if (get("tears_reminder", false)) "I don't want any messages reminding me to return here." else "I'd like to receive messages prompting me to return here.") {
                        if (toggle("tears_reminder")) {
                            npc<Neutral>("Very well, when you are eligible to drink from the Tears, you shall be reminded daily.")
                        } else {
                            npc<Neutral>("Very well, it will be up to you to remember when you can return. It is good that you are willing to take responsibility for yourself in this way.")
                        }
                    }
                }
                option<Neutral>("Not now.")
            }
        }

        objectOperate("Tell-story", "juna") { (target) ->
            if (equipped(EquipSlot.Weapon).id == "stone_bowl") {
                npc<Neutral>("If you wish to tell me more stories, come out here first.")
                return@objectOperate
            }
            start(target)
        }

        timerStart("tears_of_guthix_timer") {
            start("tears_of_guthix_ticks", get("quest_points", 0))
            set("tears_of_guthix_points", 0)
            set("tears_of_guthix_time", 10)
            get("quest_points", 0) / 10
        }

        timerTick("tears_of_guthix_timer") {
            if (dec("tears_of_guthix_time") > 0) {
                Timer.CONTINUE
            } else if (!hasClock("tears_of_guthix_ticks")) {
                Timer.CANCEL
            } else {
                remaining("tears_of_guthix_ticks")
            }
        }

        timerStop("tears_of_guthix_timer") {
            suspension = null
            strongQueue("tears_of_guthix_exit") {
                message("Your time in the caves is up.")
                walkToDelay(Tile(3254, 9517, 2))
                exit()
            }
        }

        playerDespawn {
            if (equipped(EquipSlot.Weapon).id != "stone_bowl") {
                return@playerDespawn
            }
            equipment.remove(EquipSlot.Weapon.index, "stone_bowl")
            tele(3251, 9516, 2)
            reward()
        }

        playerSpawn {
            if (!Settings["world.additional.messages", false] || !get("tears_reminder", false)) {
                return@playerSpawn
            }
            if (!questCompleted("tears_of_guthix")) {
                return@playerSpawn
            }
            if (hasClock("tears_of_guthix_cooldown")) {
                return@playerSpawn
            }
            if (hasClock("tears_reminder_cooldown")) {
                return@playerSpawn
            }
            message("<col=ef1020>You are eligible to drink from the Tears of Guthix.")
            start("tears_reminder_cooldown", TimeUnit.DAYS.toSeconds(1).toInt(), epochSeconds())
        }
    }

    private fun ChoiceOption.okay(target: GameObject) {
        option<Quiz>("Okay...") {
            if (get("quest_points", 0) < 44) {
                player<Confused>("Well... Um...")
                statement("You try to tell a story, but Juna does not seem impressed.")
                npc<Bored>("Hmm. Maybe you should come back when you can tell a good story.")
                statement("You need at least 43 Quest Points to start this quest.") // TODO proper message
                return@option
            }
            start(target)
        }
    }

    private suspend fun Player.lightCreatureStory() {
        npc<Neutral>("I will tell you the story of the light-creatures.")
        npc<Happy>("Myriad and beautiful were the creatures and civilizations of the early ages of the world. Gielinor was a work of art, shaped lovingly over the millennia by the creative mind of Guthix.")
        npc<Bored>("Only the sturdiest races survived the Godwars, and even then only by abandoning their high culture and gearing their societies towards war. Of the more delicate races there is now no trace, and almost no memory.")
        npc<Happy>("One such race had bodies as fragile as snowflakes, yet they built crystal cities that stood for a thousand years.")
        npc<Happy>("The wind would whisper through the spires and fill them with sweet harmonies, and the rising sun would shine through the precious gems that studded the towers and create inter plays of light as if rainbows were dancing.")
        npc<Happy>("Indeed, so marvellous was this light-show at its height that the patterns of light themselves became alive, and great flocks of luminous creatures rode along the gem- cast beams, each drawn to its own colour.")
        npc<Neutral>("The creatures you see floating in this chasm are the last sorry remnants of that age. I do not know how they made their way here and survived to this time, but I am grateful for their company.")
    }

    private suspend fun Player.goblinStory() {
        npc<Neutral>("Not long after the start of my vigil a party of goblins happened on my cave. These were the ugly brutes of the gods' armies, but their armour bore a faded patch where the symbol of a god had been removed.")
        npc<Bored>("They looked around hesitantly, squinting in the light of crude torches, but as soon as they saw me they raised their spears and charged.")
        npc<Happy>("No one may access the Tears of Guthix by force. I sent them tumbling into the chasm.")
        npc<Happy>("Two hundred years later another goblin found me. This one was unarmed, and although she was very wary I succeeded in engaging her in conversation.")
        npc<Happy>("Since then I have had many more visits from the descendants of those lost warriors, and over the centuries I have seen them change.")
        npc<Neutral>("They have become timid rather than aggressive, and I have seen the light of intelligence grow in their bulging eyes.")
        npc<Neutral>("Their stories are repetitive and grim, of scraping a living out of the harsh rock and through ingenuity and toil shaping it into a home.")
        npc<Bored>("I have followed the progress of their race, but their individual stories hold little interest for me.")
    }

    private suspend fun Player.tearsStory() {
        npc<Bored>("The Third Age of the world was a time of great conflict, of destruction never seen before or since, when all the gods save Guthix warred for control.")
        npc<Neutral>("The colossal Wyrms, of whom today's dragons are a pale reflection, turned all the sky to fire, while on the ground armies of foot soldiers, goblins and trolls and humans, filled the valleys and plains with blood.")
        npc<Neutral>("In time the noise of the conflict woke Guthix from His deep slumber, and He rose and stood in the centre of the battlefield so that the splendour of His wrath filled the world, and He called for the conflict to cease!")
        npc<Happy>("Silence fell, for the gods knew that none could challenge the power of the mighty Guthix -- for His power is that of nature itself, to which all other things are subject, in the end.")
        npc<Neutral>("Guthix reclaimed that which had been stolen from Him, and went back underground to return to His sleep and continue to draw the world's power into Himself.")
        npc<Neutral>("But on His way into the depths of the earth He sat and rested in this cave; and, thinking of the battle-scarred desert that now stretched from one side of His world to the other, He wept.")
        npc<Neutral>("And so great was His sorrow, and so great was His life- giving power, that the rocks themselves began to weep with Him.")
        npc<Quiz>("Later, Guthix noticed that the rocks continued to weep, and that their tears were infused with a small part of His power.")
        npc<Neutral>("So He set me, His servant, to guard the cave, and He entrusted to me the task of judging who was and was not worthy to access the tears.")
    }

    private fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "tears_of_guthix")
        inventory.remove("stone_bowl")
        set("tears_of_guthix", "completed")
        jingle("quest_complete_1")
        exp(Skill.Crafting, 1000.0)
        inc("quest_points")
        message("Congratulations, you've completed a quest: <navy>tears of guthix")
        refreshQuestJournal()
        questComplete(
            "tears of guthix",
            "1 Quest Point",
            "1000 Crafting XP",
            "Access to the Tears of Guthix",
            "cave",
            item = "stone_bowl",
        )
    }

    private suspend fun Player.start(target: GameObject) {
        if (questCompleted("tears_of_guthix") && (equipped(EquipSlot.Weapon).isNotEmpty() || equipped(EquipSlot.Shield).isNotEmpty())) {
            npc<Neutral>("Perhaps you should empty your hands before you begin.")
            return
        }
        talkWith(junaNpc())
        statement("You tell Juna some stories of your adventures.")
        randomQuestStory()
        if (hasClock("tears_of_guthix_cooldown")) {
            val remaining = remaining("tears_of_guthix_cooldown", epochSeconds()).toLong()
            val days = TimeUnit.SECONDS.toDays(remaining).toInt()
            val time = if (days == 0) {
                val hours = TimeUnit.SECONDS.toHours(remaining).toInt()
                "$hours ${"hour".plural(hours)}"
            } else {
                "$days ${"day".plural(days)}"
            }
            npc<Neutral>("Your stories have entertained me. But I will not permit any adventurer to access the tears more than once a week. Come back in $time.")
            return
        }
        npc<Happy>("Your stories have entertained me. I will let you into the cave for a short time.")
        if (!questCompleted("tears_of_guthix")) {
            npc<Neutral>("But first you will need to make a bowl in which to collect the tears.")
            moveCamera(Tile(3233, 9505, 2), height = 700, speed = 5, acceleration = 5)
            turnCamera(Tile(3227, 9493, 2), height = 200, speed = 5, acceleration = 5)
            npc<Neutral>("There is a cave on the south side of the chasm that is similarly infused with the power of Guthix. The stone in that cave is the only substance that can catch the Tears of Guthix.")
            clearCamera()
            set("tears_of_guthix", "stone_bowl")
            npc<Neutral>("Mine some stone from that cave, make it into a bowl, and bring it to me, and then I will let you catch the Tears.")
            return
        }
        npc<Neutral>("Collect as much as you can from the blue streams. If you let in water from the green streams, it will take away from the blue. For Guthix is god of balance, and balance lies in the juxtaposition of opposites.")
        target.anim("tear_of_guthix_snake_open_close")
        sound("juna_hiss")
        equipment.transaction {
            set(EquipSlot.Weapon.index, Item("stone_bowl"))
        }
        renderEmote("water_bowl")
        walkOverDelay(Tile(3251, 9516, 2))
        tab(Tab.WornEquipment)
        closeTabs(Tab.Options)
        open("tears_of_guthix_water_bowl")
        walkOverDelay(Tile(3253, 9516, 2))
        walkOverDelay(Tile(3253, 9517, 2))
        walkOverDelay(Tile(3254, 9517, 2))
        softTimers.start("tears_of_guthix_timer")
    }

    private suspend fun Player.exit() {
        walkOverDelay(Tile(3253, 9517, 2))
        walkOverDelay(Tile(3253, 9516, 2))
        equipment.remove(EquipSlot.Weapon.index, "stone_bowl")
        clearRenderEmote()
        close("tears_of_guthix_water_bowl")
        openTabs(Tab.Options)
        val target = GameObjects.find(Tile(3252, 9516, 2), "juna_base")
        target.anim("tear_of_guthix_snake_open_close")
        walkOverDelay(Tile(3251, 9516, 2))
        reward()
    }

    private fun Player.reward() {
        var lowest = Skill.Attack
        var lowestXp = Int.MAX_VALUE
        var message = ""
        for (row in Tables.get("tears_of_guthix_messages").rows()) {
            val skill = Skill.all[Skill.map[row.rowId] ?: continue]
            if (skill == Skill.Herblore && !questCompleted("druidic_ritual")) {
                continue
            }
            if (skill == Skill.Runecrafting && !questCompleted("rune_mysteries")) {
                continue
            }
            val xp = experience.direct(skill)
            if (xp < lowestXp) {
                lowest = skill
                lowestXp = xp
                message = row.string("message")
            }
        }
        val points = get("tears_of_guthix_points", 0)
        if (points <= 0) {
            return
        }
        val rate = (10 + ((1.0 / 10.0) * floor((lowestXp / 10.0) / 27)).toInt()).coerceAtMost(60).toDouble()
        exp(lowest, rate * points)
        message(message)
        start("tears_of_guthix_cooldown", TimeUnit.DAYS.toSeconds(7).toInt(), epochSeconds())
    }

    private fun junaNpc(): NPC = NPCs.find(Tile(3252, 9517, 1), "juna")
}
