package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

npcOperate("Talk-to", "jeffery") {
    npc<Unsure>("Keep it quick. What do you want?")
    choice {
        if (player.quest("gunnars_ground") != "unstarted" && player.quest("gunnars_ground") != "started" && player.quest("gunnars_ground") != "love_poem") {
            option<Talk>("Who was that love poem for?") {
                if (player.quest("gunnars_ground") == "completed") {
                    npc<Sad>("It, er, it didn't work out well.")
                    npc<Angry>("I don't want to talk about it! Leave me alone!")
                } else {
                    npc<Talk>("I haven't had a chance to do anything with it yet!")
                }
            }
        }
        if (player.quest("gunnars_ground") == "love_poem") {
            option<Talk>("I'm here about a gold ring.") {
                npc<Unsure>("You want to buy a gold ring? You want to sell a gold ring? You want to ask pointless questions about gold rings?")
                choice {
                    option<Talk>("I was hoping you would trade me a gold ring.") {
                        npc<Unsure>("Trade you? Trade you for what?")
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
                player<Unsure>("How do I become exceptional?")
                npc<Cheerful>("Exceptional people have earned exceptional items; earning Varrock armour would impress me.")
                player<Happy>("Alright!")
                return@option
            }
            npc<Cheerful>("You seem exceptional enough. Go ahead.")
            player<Unsure>("What can I make here, exactly?")
            npc<Cheerful>("Well, depending on your skill as a blacksmith, you can use this furnace to smelt ore into metal bars.")
            player<Cheerful>("Oh, I see. What's so special about this furnace, then?")
            npc<Cheerful>("If you smelt at this furnace while wearing your Varrock armour the enchantment on the armour will give you a small chance of smelting two bars instead of one.")
            player<Unsure>("I see. So, which metal will I be able to obtain more of when smelting with the armour I'm wearing?")
            if (player.equipment.contains("varrock_armour_4")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, rune.")
                player<Cheerful>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Cheerful>("Stay exceptional!")
            } else if (player.equipment.contains("varrock_armour_3")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, adamant.")
                player<Cheerful>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Cheerful>("Stay exceptional!")
            } else if (player.equipment.contains("varrock_armour_2")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, mithril.")
                player<Cheerful>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Cheerful>("Stay exceptional!")
            } else if (player.equipment.contains("varrock_armour_1")) {
                npc<Talk>("While wearing the Varrock armour, you will have a chance of smelting an extra bar of any metal up to, and including, steel.")
                player<Cheerful>("Oh, that's useful. That should save me a fair bit of time. Thanks very much.")
                npc<Cheerful>("Stay exceptional!")
            } else {
                npc<Talk>("Try coming back when you're actually wearing your Varrock armour.")
                player<Surprised>("Oh, right. Yes I see. Okay, thanks.")
            }
        }
        option<Talk>("Er, nothing.")
    }
}

suspend fun TargetNPCContext.lovePoem() {
    npc<Surprised>("A love poem? What?")
    npc<Unsure>("Wait...that dwarf put you up to this, didn't he?")
    choice {
        option<Talk>("Yes, he did.") {
            cheekyLittle()
        }
        option<Talk>("I don't know any dwarf.") {
            npc<Angry>("I recognise his awful handwriting.")
            cheekyLittle()
        }
    }
}

suspend fun TargetNPCContext.cheekyLittle() {
    npc<Angry>("That cheeky little...")
    npc<Angry>("He just can't leave it alone, can he? Fine! I'll trade you for the poem. What is it you want?")
    choice {
        option<Talk>("Just a plain, gold ring.") {
            goldRing()
        }
        option<Talk>("The most valuable diamond ring you have.") {
            npc<Happy>("Well, all I have is this plain, gold ring.")
            player<Upset>("That will have to do.")
            goldRing()
        }
    }
}

suspend fun TargetNPCContext.goldRing() {
    player.inventory.replace("love_poem", "ring_from_jeffery")
    player["gunnars_ground"] = "jeffery_ring"
    player.setAnimation("hand_over_item")
    target.setAnimation("exchange_pocket")
    items("love_poem", "ring_from_jeffery", "Jeffery trades you a gold ring for the poem.")
    npc<Angry>("Now, leave me in peace!")
}