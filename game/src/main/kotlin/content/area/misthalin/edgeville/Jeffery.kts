package content.area.misthalin.edgeville

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import content.entity.player.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player

npcOperate("Talk-to", "jeffery") {
    npc<Quiz>("Keep it quick. What do you want?")
    choice {
        if (player.quest("gunnars_ground") != "unstarted" && player.quest("gunnars_ground") != "started" && player.quest("gunnars_ground") != "love_poem") {
            option<Talk>("Who was that love poem for?") {
                if (player.quest("gunnars_ground") == "completed") {
                    npc<Sad>("It, er, it didn't work out well.")
                    npc<Frustrated>("I don't want to talk about it! Leave me alone!")
                } else {
                    npc<Talk>("I haven't had a chance to do anything with it yet!")
                }
            }
        }
        if (player.quest("gunnars_ground") == "love_poem") {
            option<Talk>("I'm here about a gold ring.") {
                npc<Quiz>("You want to buy a gold ring? You want to sell a gold ring? You want to ask pointless questions about gold rings?")
                choice {
                    option<Talk>("I was hoping you would trade me a gold ring.") {
                        npc<Quiz>("Trade you? Trade you for what?")
                        if (player.holdsItem("love_poem")) {
                            choice {
                                option<Talk>("This splendid love poem.") {
                                    lovePoem()
                                }
                                option<Talk>("Some old love poem or something.") {
                                    lovePoem()
                                }
                            }
                        } else {
                            player<Talk>("Er...I meant to bring a poem as a trade, but I seem to have mislaid it. I'll go and find it.")
                        }
                    }
                    option<Talk>("Actually, forget it.")
                }
            }
        }
        option<Talk>("I want to use the furnace.") {
            if (!player.ownsItem("varrock_armour_4") && !player.ownsItem("varrock_armour_3") && !player.ownsItem("varrock_armour_2") && !player.ownsItem("varrock_armour_1")) {
                npc<Talk>("You want to use my furnace? I only let exceptional people use my furnace. You don't look exceptional to me.")
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
            if (player.equipment.contains("varrock_armour_4")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, rune.")
                player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Happy>("Stay exceptional!")
            } else if (player.equipment.contains("varrock_armour_3")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, adamant.")
                player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Happy>("Stay exceptional!")
            } else if (player.equipment.contains("varrock_armour_2")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, mithril.")
                player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Happy>("Stay exceptional!")
            } else if (player.equipment.contains("varrock_armour_1")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, steel.")
                player<Happy>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Happy>("Stay exceptional!")
            } else {
                npc<Talk>("Try coming back when you're actually wearing your Varrock armour.")
                player<Surprised>("Oh, right. Yes I see. Okay, thanks.")
            }
        }
        option<Talk>("Er, nothing.")
    }
}

suspend fun NPCOption<Player>.lovePoem() {
    npc<Surprised>("A love poem? What?")
    npc<Quiz>("Wait...that dwarf put you up to this, didn't he?")
    choice {
        option<Talk>("Yes, he did.") {
            cheekyLittle()
        }
        option<Talk>("I don't know any dwarf.") {
            npc<Frustrated>("I recognise his awful handwriting.")
            cheekyLittle()
        }
    }
}

suspend fun NPCOption<Player>.cheekyLittle() {
    npc<Frustrated>("That cheeky little...")
    npc<Frustrated>("He just can't leave it alone, can he? Fine! I'll trade you for the poem. What is it you want?")
    choice {
        option<Talk>("Just a plain, gold ring.") {
            goldRing()
        }
        option<Talk>("The most valuable diamond ring you have.") {
            npc<Pleased>("Well, all I have is this plain, gold ring.")
            player<Upset>("That will have to do.")
            goldRing()
        }
    }
}

suspend fun NPCOption<Player>.goldRing() {
    player.inventory.replace("love_poem", "ring_from_jeffery")
    player["gunnars_ground"] = "jeffery_ring"
    player.anim("hand_over_item")
    target.anim("exchange_pocket")
    items("love_poem", "ring_from_jeffery", "Jeffery trades you a gold ring for the poem.")
    npc<Frustrated>("Now, leave me in peace!")
}