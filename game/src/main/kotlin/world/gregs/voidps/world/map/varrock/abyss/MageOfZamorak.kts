package world.gregs.voidps.world.map.varrock.abyss

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop
import world.gregs.voidps.world.interact.entity.sound.playSound

val areas: AreaDefinitions by inject()

val abyss = areas["abyss_multi_area"]
val abyssCenter = areas["abyss_center"]

playerSpawn { player ->
    player.sendVariable("enter_the_abyss")
}

npcOperate("Talk-to", "mage_of_zamorak_wilderness") {
    if (player.equipment.items.any { it.id.contains("saradomin", ignoreCase = true) }) {
        npc<Angry>("I don't speak to Saradominist filth.")
        return@npcOperate
    }

    if (player.questComplete("rune_mysteries")) {
        when (player["enter_the_abyss", "unstarted"]) {
            "unstarted" -> {
                npc<Talk>("If you want to talk, this isn't the place for it. Meet me in Varrock's Chaos Temple, by the rune shop. Unless you're here to buy something?")
                player["enter_the_abyss"] = "started"
            }
            "completed" -> npc<Talk>("") // TODO
            "started" -> npc<Talk>("I already told you to meet me in Varrock's Chaos Temple, by the rune shop. Unless you're here to buy something?")
            else -> npc<Talk>("This isn't the place to talk. Visit me in Varrock's Chaos Temple if you have something to discuss. Unless you're here to buy something?")
        }
    } else {
        npc<Angry>("This isn't the place to talk. Unless you're here to buy something, you should leave.")
    }

    choice {
        option("Let's see what you're selling.") {
            player.openShop("mage_of_zamorak")
        }
        option<Uncertain>("Alright, I'll go.")
    }
}
npcOperate("Talk-to", "mage_of_zamorak_varrock") {
    if (player.equipment.items.any { it.id.contains("saradomin", ignoreCase = true) }) {
        npc<Angry>("How dare you wear such disrespectful attire in this holy place? Remove those immediately if you wish to speak to me.")
        return@npcOperate
    }
    if (!player.containsVarbit("enter_the_abyss_data", "where_runes")) {
        npc<Talk>("Ah, you again. The Wilderness is hardly the appropriate place for a conversation now, is it? What was it you wanted?")
        player<Uncertain>("Err... I didn't really want anything.")
        npc<Uncertain>("So why did you approach me?")
        player<Uncertain>("I was just wondering why you sell runes in the Wilderness?")
        npc<Angry>("Well I can't go doing it in the middle of Varrock, can I? In case you hadn't noticed, I'm a servant of Zamorak. The Saradominists have made sure that people like me are not welcome in these parts.")
        player["enter_abyss_where_runes"] = true
        choice {
            option<Quiz>("Where do you get your runes from?") {
                whereRunes()
            }
            option<Talk>("Interesting. Thanks for the information.")
        }
    } else if (player["enter_abyss_has_orb", false]) {
        npc<Quiz>("You again. Have you managed to use that scrying orb to obtain the information I need?")
        if (!player.ownsItem("scrying_orb")) {
            player<Upset>("I lost it. Could I have another?")
            npc<Angry>("Fool! Take this, and don't lose it this time!")
            item("scrying_orb", 400, "The Mage of Zamorak hands you an orb.")
            player.inventory.add("scrying_orb")
        } else {
            player<Talk>("Not yet.")
            npc<Talk>("You must carry it with you and teleport to the Rune Essence Mine from three different locations. Return to me once you have done so.")
        }
    } else if (player["enter_abyss_offer", false]) {
        npc<Quiz>("You again. Have you considered my offer? If you help us access the Rune Essence Mine, we will share our runecrafting secrets with you in return.")
    } else if (player["enter_abyss_where_runes", false]) {
        npc<Talk>("Ah, you again. Do you need something?")
        choice {
            option<Quiz>("Where do you get your runes from?") {
                whereRunes()
            }
            option<Talk>("Just looking around.")
        }
    }
}
/*

varbit 13728 = 1 // knows locations of teleporters
Aubury
[7106] 2024-05-02 12:30:01 Varbit (varpId: 491, oldValue: 0) Varbit(id = 2315, value = 1)
[7106] 2024-05-02 12:30:01 Varbit (varpId: 638, oldValue: 0) Varbit(id = 2313, value = 4)
[7106] 2024-05-02 12:30:01 Varp (oldValue: 166141760)        Varp(id: 23, value = 166141776)


Lumb guy
[7329] 2024-05-02 12:32:15 Varbit (varpId: 491, oldValue: 0) Varbit(id = 2314, value = 1)
[7329] 2024-05-02 12:32:15 Varbit (varpId: 638, oldValue: 4) Varbit(id = 2313, value = 6)


Brimstail
[7754] 2024-05-02 12:36:30 Varbit (varpId: 491, oldValue: 0)      Varbit(id = 2317, value = 1)
[7754] 2024-05-02 12:36:30 Varbit (varpId: 638, oldValue: 6)      Varbit(id = 2313, value = 1)
[7754] 2024-05-02 12:36:30 Local                                  Message(type = GAMEMESSAGE, text = "Your scrying orb has absorbed enough teleportation information.")

 */


suspend fun NPCOption.whereRunes() {
    npc<Uncertain>("Well we craft them of course.")
    player<Uncertain>("We?")
    npc<Angry>("My associates and I. Despite the best attempts of the Saradominists, there's still more of us around than they'd like.")
    player<Quiz>("I can't imagine they like you crafting runes much. Do they not try and stop you?")
    npc<Laugh>("Ha! I'm sure they'd love to, but we have methods of runecrafting that they can only dream of!")
    player<Quiz>("Care to share?")
    npc<Uncertain>("Why would I? You are not a member of our institute. How do I know you won't just go and share all of our secrets with those Saradominist fools in the Order of Wizards.")
    choice {
        option<Quiz>("Maybe I could make it worth your while?") {

        }
        option<Talk>("But I'm a loyal servant of Zamorak as well!") {
            npc<Talk>("Even if you speak the truth, it takes more than just being a follower of Zamorak to gain the secrets of the institute. You would need to offer something in return.")
            player<Quiz>("Like what?")
            npc<Angry>("Until recently, our runecrafting secrets allowed us to produce runes at a far superior rate compared to the inept Order of Wizards, but something has changed.")
            npc<Angry>("From what we can gather, they've somehow rediscovered how to access the lost Rune Essence Mine.")
            player<Happy>("Ah, well I know all about that. I was actually the one to help them do it!")
            npc<Angry>("You did what? You helped the Order of Wizards? I thought you claimed to be a servant of Zamorak?")
            player<Uncertain>("Err...")
            choice {
                option<Talk>("I did it so that I could then steal their secrets.") {
                    npc<Uncertain>("You did? Perhaps I underestimated you.")
                    npc<Uncertain>("Alright, if you help us access the Rune Essence Mine, we will share our runecrafting secrets with you in return.")
                    player["enter_abyss_offer"] = true
                    choice {
                        option<Talk>("Deal.") {
                            npc<Talk>("Good. Now, all I need from you is the spell that will teleport me to the Rune Essence Mine.")
                            npc<Uncertain>("Err... I don't actually know the spell.")
                            npc<Uncertain>("What? Then how do you get there.")
                            player<Talk>("Oh, well the people who do know the spell just teleport me there directly.")
                            npc<Talk>("Hmm... I see. That makes this slightly more complex, but no matter. You can still help us.")
                            player<Quiz>("How?")
                            npc<Talk>("I'll give you a scrying orb with a standard cypher spell cast upon it. The orb will absorb mystical energies that it is exposed to.")
                            npc<Talk>("If you teleport to the Rune Essence Mine from three different locations, the orb will absorb the energies of the spell and allow us to reverse-engineer the magic behind it.")
                            npc<Quiz>("Do you know of three different people who can teleport you there?")
                            player<Uncertain>("Maybe?")
                            npc<Talk>("Well if not, I'm sure one of those fools in the Order of Wizards can tell you. Now, here's the orb.")
                            item("scrying_orb", 400, "The Mage of Zamorak hands you an orb.")
                            player["enter_the_abyss"] = "stage1"
                            player["enter_abyss_has_orb"] = true
                            player.inventory.add("scrying_orb")
                            // TODO what if inv full
                        }
                        option<Talk>("No deal.") {
                            npc<Angry>("Fine. I will find another way.")
                        }
                        option<Talk>("I need to think about it.") {
                            npc<Uncertain>("I will be here once you have decided.")
                        }
                    }
                }
                option<Happy>("Okay, fine. I don't really serve Zamorak.") {

                }
                option<Uncertain>("Sorry, I just remembered that I have to take my pet rat for a walk.") {
                    npc<Uncertain>("What?")
                    player<Neutral>("Yup! Got to go!")
                }
            }
        }
        option<Talk>("You're right. I'm a faithful follower of Saradomin.") {
            npc<Angry>("Then you have no place here! Leave, before I make you!")
        }
        option<Talk>("Actually, I'm not interested.")
    }
}

npcOperate("Teleport", "mage_of_zamorak_wilderness") {
    if (player.queue.contains(ActionPriority.Normal)) {
        return@npcOperate
    }
    player.closeInterfaces()
    player.queue("teleport", onCancel = null) {
        target.face(player)
        target.setGraphic("tele_other")
        target.setAnimation("tele_other")
        player.playSound("tele_other_cast")
        target.forceChat = "Veniens! Sallakar! Rinnesset!"
        pause(2)
        player.setAnimation("lunar_teleport")
        player.setGraphic("tele_other_receive")
        player.playSound("teleport_all")
        pause(2)
        player["abyss_obstacles"] = random.nextInt(0, 12)
        var tile = abyss.random(player)
        var count = 0
        while ((tile == null || tile in abyssCenter) && count++ < 100) {
            tile = abyss.random(player)
        }
        player.tele(tile!!)
        player.clearAnimation()
    }
}
