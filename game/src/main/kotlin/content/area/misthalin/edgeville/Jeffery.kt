package content.area.misthalin.edgeville

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class Jeffery : Script {

    init {
        npcOperate("Talk-to", "jeffery_*") { (target) ->
            npc<Quiz>("Keep it quick. What do you want?")
            choice {
                if (quest("gunnars_ground") != "unstarted" && quest("gunnars_ground") != "started" && quest("gunnars_ground") != "love_poem") {
                    option<Neutral>("Who was that love poem for?") {
                        if (quest("gunnars_ground") == "completed") {
                            npc<Disheartened>("It, er, it didn't work out well.")
                            npc<Frustrated>("I don't want to talk about it! Leave me alone!")
                        } else {
                            npc<Neutral>("I haven't had a chance to do anything with it yet!")
                        }
                    }
                }
                if (quest("gunnars_ground") == "love_poem") {
                    option<Neutral>("I'm here about a gold ring.") {
                        npc<Quiz>("You want to buy a gold ring? You want to sell a gold ring? You want to ask pointless questions about gold rings?")
                        choice {
                            option<Neutral>("I was hoping you would trade me a gold ring.") {
                                npc<Quiz>("Trade you? Trade you for what?")
                                if (carriesItem("love_poem")) {
                                    choice {
                                        option<Neutral>("This splendid love poem.") {
                                            lovePoem(target)
                                        }
                                        option<Neutral>("Some old love poem or something.") {
                                            lovePoem(target)
                                        }
                                    }
                                } else {
                                    player<Neutral>("Er...I meant to bring a poem as a trade, but I seem to have mislaid it. I'll go and find it.")
                                }
                            }
                            option<Neutral>("Actually, forget it.")
                        }
                    }
                }
                option<Neutral>("I want to use the furnace.") {
                    if (!ownsItem("varrock_armour_4") && !ownsItem("varrock_armour_3") && !ownsItem("varrock_armour_2") && !ownsItem("varrock_armour_1")) {
                        npc<Neutral>("You want to use my furnace? I only let exceptional people use my furnace. You don't look exceptional to me.")
                        player<Quiz>("How do I become exceptional?")
                        npc<Happy>("Exceptional people have earned exceptional items; earning Varrock armour would impress me.")
                        player<Pleased>("Alright!")
                        return@option
                    }
                    npc<Happy>("You seem exceptional enough. Go ahead.")
                    player<Quiz>("What can I make here, exactly?")
                    npc<Happy>("Well, depending on your skill as a blacksmith, you can use this furnace to smelt ore into metal bars.")
                    player<Happy>("Oh, I see. What's so special about this furnace, then?")
                    npc<Happy>("If you smelt at this furnace while wearing your Varrock armour the enchantment on the armour will give you a small chance of smelting two bars instead of one.")
                    player<Quiz>("I see. So, which metal will I be able to obtain more of when smelting with the armour I'm wearing?")
                    if (equipment.contains("varrock_armour_4")) {
                        npc<Neutral>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, rune.")
                        player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                        npc<Happy>("Stay exceptional!")
                    } else if (equipment.contains("varrock_armour_3")) {
                        npc<Neutral>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, adamant.")
                        player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                        npc<Happy>("Stay exceptional!")
                    } else if (equipment.contains("varrock_armour_2")) {
                        npc<Neutral>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, mithril.")
                        player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                        npc<Happy>("Stay exceptional!")
                    } else if (equipment.contains("varrock_armour_1")) {
                        npc<Neutral>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, steel.")
                        player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                        npc<Happy>("Stay exceptional!")
                    } else {
                        npc<Neutral>("Try coming back when you're actually wearing your Varrock armour.")
                        player<Shock>("Oh, right. Yes I see. Okay, thanks.")
                    }
                }
                option<Neutral>("Er, nothing.")
            }
        }
    }

    suspend fun Player.lovePoem(target: NPC) {
        npc<Shock>("A love poem? What?")
        npc<Quiz>("Wait...that dwarf put you up to this, didn't he?")
        choice {
            option<Neutral>("Yes, he did.") {
                cheekyLittle(target)
            }
            option<Neutral>("I don't know any dwarf.") {
                npc<Frustrated>("I recognise his awful handwriting.")
                cheekyLittle(target)
            }
        }
    }

    suspend fun Player.cheekyLittle(target: NPC) {
        npc<Frustrated>("That cheeky little...")
        npc<Frustrated>("He just can't leave it alone, can he? Fine! I'll trade you for the poem. What is it you want?")
        choice {
            option<Neutral>("Just a plain, gold ring.") {
                goldRing(target)
            }
            option<Neutral>("The most valuable diamond ring you have.") {
                npc<Pleased>("Well, all I have is this plain, gold ring.")
                player<Sad>("That will have to do.")
                goldRing(target)
            }
        }
    }

    suspend fun Player.goldRing(target: NPC) {
        inventory.replace("love_poem", "ring_from_jeffery")
        set("gunnars_ground", "jeffery_ring")
        anim("hand_over_item")
        target.anim("exchange_pocket")
        items("love_poem", "ring_from_jeffery", "Jeffery trades you a gold ring for the poem.")
        npc<Frustrated>("Now, leave me in peace!")
    }
}
