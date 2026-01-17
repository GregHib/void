package content.area.wilderness.abyss

import content.entity.npc.shop.openShop
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random

class MageOfZamorak : Script {

    init {
        playerSpawn {
            sendVariable("enter_the_abyss")
        }

        npcOperate("Teleport", "mage_of_zamorak_wilderness_*") { (target) ->
            teleport(this, target)
        }

        npcOperate("Talk-to", "mage_of_zamorak_wilderness_*") { (target) ->
            if (equipment.items.any { it.id.contains("saradomin", ignoreCase = true) }) {
                npc<Angry>("I don't speak to Saradominist filth.")
                return@npcOperate
            }

            if (questCompleted("rune_mysteries")) {
                when (get("enter_the_abyss", "unstarted")) {
                    "unstarted" -> {
                        npc<Neutral>("If you want to talk, this isn't the place for it. Meet me in Varrock's Chaos Temple, by the rune shop. Unless you're here to buy something?")
                        set("enter_the_abyss", "started")
                    }
                    "started" -> npc<Neutral>("I already told you to meet me in Varrock's Chaos Temple, by the rune shop. Unless you're here to buy something?")
                    else -> npc<Neutral>("This isn't the place to talk. Visit me in Varrock's Chaos Temple if you have something to discuss. Unless you're here to buy something?")
                }
            } else {
                npc<Angry>("This isn't the place to talk. Unless you're here to buy something, you should leave.")
            }

            choice {
                option("Let's see what you're selling.") {
                    openShop("mage_of_zamorak")
                }
                if (questCompleted("enter_the_abyss")) {
                    option<Quiz>("Could you teleport me to the Abyss?") {
                        teleport(this, target)
                    }
                }
                option<Confused>("Alright, I'll go.")
            }
        }

        npcOperate("Talk-to", "mage_of_zamorak_varrock,mage_of_zamorak_normal") {
            if (equipment.items.any { it.id.contains("saradomin", ignoreCase = true) }) {
                npc<Angry>("How dare you wear such disrespectful attire in this holy place? Remove those immediately if you wish to speak to me.")
                return@npcOperate
            }
            if (questCompleted("enter_the_abyss")) {
                npc<Neutral>("Ah, you again. What do you want?")
                choice {
                    aboutAbyss()
                    aboutGroup()
                    option<Neutral>("Nothing. I'm just looking around.") {
                        npc<Neutral>("Very well.")
                    }
                }
                return@npcOperate
            }
            if (!get("enter_abyss_where_runes", false)) {
                npc<Neutral>("Ah, you again. The Wilderness is hardly the appropriate place for a conversation now, is it? What was it you wanted?")
                player<Confused>("Err... I didn't really want anything.")
                npc<Confused>("So why did you approach me?")
                player<Confused>("I was just wondering why you sell runes in the Wilderness?")
                npc<Angry>("Well I can't go doing it in the middle of Varrock, can I? In case you hadn't noticed, I'm a servant of Zamorak. The Saradominists have made sure that people like me are not welcome in these parts.")
                set("enter_abyss_where_runes", true)
                choice {
                    whereRunes()
                    option<Neutral>("Interesting. Thanks for the information.")
                }
            } else if (get("enter_abyss_has_orb", false)) {
                npc<Quiz>("You again. Have you managed to use that scrying orb to obtain the information I need?")
                if (get("enter_abyss_taken_orb", false)) {
                    takenOrb()
                } else if (ownsItem("scrying_orb_full")) {
                    player<Neutral>("Here you go.")
                    if (!inventory.remove("scrying_orb_full")) {
                        return@npcOperate
                    }
                    set("enter_the_abyss", "orb_inspect")
                    message("You hand the orb to the Mage of Zamorak.")
                    item("scrying_orb", 400, "You hand the orb to the Mage of Zamorak.")
                    npc<Neutral>("Right, let's take a look at this orb...")
                    npc<Happy>("Yes, this will do nicely. Once again, the Zamorak Magical Institute has overcome the Order of Wizards!")
                    set("enter_abyss_taken_orb", true)
                    takenOrb()
                } else if (!ownsItem("scrying_orb")) {
                    player<Sad>("I lost it. Could I have another?")
                    npc<Angry>("Fool! Take this, and don't lose it this time!")
                    item("scrying_orb", 400, "The Mage of Zamorak hands you an orb.")
                    inventory.add("scrying_orb")
                } else {
                    player<Neutral>("Not yet.")
                    npc<Neutral>("You must carry it with you and teleport to the Rune Essence Mine from three different locations. Return to me once you have done so.")
                }
            } else if (get("enter_abyss_offer", false)) {
                npc<Quiz>("You again. Have you considered my offer? If you help us access the Rune Essence Mine, we will share our runecrafting secrets with you in return.")
                offer()
            } else if (get("enter_abyss_where_runes", false)) {
                npc<Neutral>("Ah, you again. Do you need something?")
                choice {
                    whereRunes()
                    option<Neutral>("Just looking around.")
                }
            }
        }
    }

    fun ChoiceOption.aboutGroup() {
        option<Quiz>("Can you tell me more about your group?") {
            npc<Idle>("I suppose you have proven yourself trustworthy. We are a group of mages in service to Zamorak. Our group is called the Zamorak Magical Institute, or Z.M.I. for short.")
            npc<Angry>("Few actually know of us. Saradominist groups like the Order of Wizards hold sway over these lands, so we are forced to work in the shadows. However, make no mistake, our power far exceeds theirs.")
            player<Quiz>("You don't seem to like the Order of Wizards very much.")
            npc<Angry>("And why would we? Did you know that the Order of Wizards wasn't always a Saradominist group? Once they allowed mages of all faiths to study with them, but they became greedy.")
            player<Quiz>("What happened?")
            npc<Angry>("They started to desire more control. They banned Zamorakian wizards from certain areas of study, claiming them to be too dangerous. In reality, they just wanted that knowledge for themselves.")
            npc<Angry>("It all went wrong for them, of course. A Zamorakian wizard made a great breakthrough in teleportation magic, but the Saradominists stole his research.")
            npc<Angry>("They used it to perform a ritual, but their lack of understanding caused it to go wrong. The entire Wizards' Tower burnt down. Many were killed, and years of research was destroyed.")
            npc<Angry>("Naturally, they blamed the Zamorakians. Claimed we intentionally burnt the tower down. After the tower was rebuilt, we were banished from the Order of Wizards and forced underground.")
            player<Quiz>("And that's how the Z.M.I. was formed?")
            npc<Neutral>("Exactly.")
            npc<Quiz>("Now, did you want something else?")
            choice {
                aboutAbyss()
                option<Neutral>("No thanks.") {
                    npc<Neutral>("Very well.")
                }
            }
        }
    }

    fun ChoiceOption.aboutAbyss() {
        option<Quiz>("Can you tell me more about the Abyss?") {
            npc<Neutral>("It is a hard place to describe. We often refer to it as another plane, but that isn't quite accurate. If anything, it is more like a plane that sits between all other planes.")
            player<Quiz>("Right... And what does it have to do with runecrafting?")
            npc<Neutral>("In truth, nothing at all. However, it has everything to do with teleportation. You see, time and space work differently in the Abyss.")
            npc<Idle>("For example, you can travel to the Abyss, walk a few steps and then exit, only to find yourself somewhere completely different from where you entered. You might even find yourself on a different plane.")
            npc<Neutral>("You can also enter the Abyss and spend days there, but on your return, only moments have passed.")
            npc<Idle>("This is why the Abyss is so useful. It can be used as an effective hub for teleportation. In fact, all teleportation that we know of uses the Abyss, you just don't realise it.")
            player<Quiz>("Wait... So when I teleport, I'm actually travelling through the Abyss?")
            npc<Neutral>("In some ways, I suppose you are. Whenever you use a teleport spell, that spell is actually travelling through the Abyss and using it to link two places together.")
            player<Quiz>("So did you discover the Abyss?")
            npc<Idle>("No. Knowledge of the Abyss has existed for a long time, hence it being used for teleportation. However, we believe we may be the first to have properly gained access in centuries.")
            player<Quiz>("How did you manage it?")
            npc<Idle>("By complete accident. One of our initiates was performing a routine teleportation experiment when something went wrong. Instead of ending up at their destination, they found themselves in the Abyss.")
            npc<Quiz>("Now, did you want something else?")
            choice {
                aboutGroup()
                option<Neutral>("No thanks.") {
                    npc<Neutral>("Very well.")
                }
            }
        }
    }

    suspend fun Player.takenOrb() {
        npc<Happy>("You have done well. Now, time for us to uphold our end of the bargin.")
        npc<Idle>("The reason we are able to craft so many runes is because we do not visit the runic altars in the traditional way. Instead, we have found a way to teleport to them directly.")
        player<Quiz>("How?")
        npc<Idle>("Via another plane known as the Abyss. It is a complex place that cannot be easily explained, but I will share our research notes with you so you may better understand it.")
        player<Quiz>("So can I use the Abyss?")
        npc<Idle>("Yes. Visit me in the Wilderness whenever you wish to be teleported there. Just be careful, for it is a dangerous place. Still, I'm sure you'll agree that the risk is worth the reward.")
        npc<Neutral>("There are creatures there that will hunt and attack any visitors on sight. The magic we use for teleporting there can also be a bit... unstable.")
        player<Quiz>("What do you mean?")
        npc<Shifty>("Just don't expect to be using any prayers in there.")
        npc<Neutral>("Anyway, you may also have this pouch as well. I'm sure you will find it useful. Now, we're done here.")
        if (!inventory.add("abyssal_book", "small_pouch")) {
            // TODO proper message
            items("abyssal_book", "small_pouch", "The Mage of Zamorak tries to hand you a book and a pouch, but you don't have enough room to take it.")
            return
        }
        items("abyssal_book", "small_pouch", "The Mage of Zamorak hands you a book and a pouch.")
        message("The Mage of Zamorak hands you a book and a pouch.")
        set("enter_the_abyss", "completed")
        exp(Skill.Runecrafting, 1000.0)
    }

    fun ChoiceOption.whereRunes() {
        option<Quiz>("Where do you get your runes from?") {
            npc<Confused>("Well we craft them of course.")
            player<Confused>("We?")
            npc<Angry>("My associates and I. Despite the best attempts of the Saradominists, there's still more of us around than they'd like.")
            player<Quiz>("I can't imagine they like you crafting runes much. Do they not try and stop you?")
            npc<Cackle>("Ha! I'm sure they'd love to, but we have methods of runecrafting that they can only dream of!")
            player<Quiz>("Care to share?")
            npc<Confused>("Why would I? You are not a member of our institute. How do I know you won't just go and share all of our secrets with those Saradominist fools in the Order of Wizards.")
            choice {
                option<Quiz>("Maybe I could make it worth your while?") {
                    npc<Quiz>("How? What do you have to offer?")
                    player<Quiz>("Well what is it you want?")
                    accessLostMine()
                }
                option<Neutral>("But I'm a loyal servant of Zamorak as well!") {
                    npc<Neutral>("Even if you speak the truth, it takes more than just being a follower of Zamorak to gain the secrets of the institute. You would need to offer something in return.")
                    player<Quiz>("Like what?")
                    accessLostMine()
                }
                option<Neutral>("You're right. I'm a faithful follower of Saradomin.") {
                    npc<Angry>("Then you have no place here! Leave, before I make you!")
                }
                option<Neutral>("Actually, I'm not interested.")
            }
        }
    }

    suspend fun Player.accessLostMine() {
        npc<Angry>("Until recently, our runecrafting secrets allowed us to produce runes at a far superior rate compared to the inept Order of Wizards, but something has changed.")
        npc<Angry>("From what we can gather, they've somehow rediscovered how to access the lost Rune Essence Mine.")
        player<Happy>("Ah, well I know all about that. I was actually the one to help them do it!")
        npc<Angry>("You did what? You helped the Order of Wizards? I thought you claimed to be a servant of Zamorak?")
        player<Confused>("Err...")
        choice {
            option<Neutral>("I did it so that I could then steal their secrets.") {
                npc<Confused>("You did? Perhaps I underestimated you.")
                deal()
            }
            option<Happy>("Okay, fine. I don't really serve Zamorak.") {
                npc<Angry>("Then give me one good reason why I shouldn't have you teleported into the depths of a volcano!")
                choice {
                    option<Neutral>("Because I can still help you.") {
                        npc<Neutral>("You would help both sides for your own gain? I suppose as a Zamorakian, I can respect that, even if I don't like it.")
                        deal()
                    }
                    option<Neutral>("Alright, I'll leave. Just don't go teleporting me anywhere.") {
                        npc<Angry>("Be glad that I am merciful! Now go!")
                    }
                }
            }
            option<Confused>("Sorry, I just remembered that I have to take my pet rat for a walk.") {
                npc<Confused>("What?")
                player<Idle>("Yup! Got to go!")
            }
        }
    }

    suspend fun Player.deal() {
        npc<Confused>("Alright, if you help us access the Rune Essence Mine, we will share our runecrafting secrets with you in return.")
        set("enter_abyss_offer", true)
        offer()
    }

    fun teleport(player: Player, target: NPC) {
        if (player.queue.contains(ActionPriority.Normal)) {
            return
        }
        player.closeInterfaces()
        player.queue("teleport", onCancel = null) {
            target.face(player)
            target.gfx("tele_other")
            target.anim("tele_other")
            player.sound("tele_other_cast")
            target.say("Veniens! Sallakar! Rinnesset!")
            player.delay(2)
            player.anim("lunar_teleport")
            player.gfx("tele_other_receive")
            player.sound("teleport_all")
            player.delay(2)
            player["abyss_obstacles"] = random.nextInt(0, 12)
            var tile = AreaDefinitions["abyss_multi_area"].random(player)
            var count = 0
            while ((tile == null || tile in AreaDefinitions["abyss_center"]) && count++ < 100) {
                tile = AreaDefinitions["abyss_multi_area"].random(player)
            }
            player.tele(tile!!)
            player.levels.drain(Skill.Prayer, player.levels.get(Skill.Prayer))
            player.clearAnim()
        }
    }

    suspend fun Player.offer() {
        choice {
            option<Neutral>("Deal.") {
                npc<Neutral>("Good. Now, all I need from you is the spell that will teleport me to the Rune Essence Mine.")
                player<Confused>("Err... I don't actually know the spell.")
                npc<Confused>("What? Then how do you get there.")
                player<Neutral>("Oh, well the people who do know the spell just teleport me there directly.")
                npc<Neutral>("Hmm... I see. That makes this slightly more complex, but no matter. You can still help us.")
                player<Quiz>("How?")
                npc<Neutral>("I'll give you a scrying orb with a standard cypher spell cast upon it. The orb will absorb mystical energies that it is exposed to.")
                npc<Neutral>("If you teleport to the Rune Essence Mine from three different locations, the orb will absorb the energies of the spell and allow us to reverse-engineer the magic behind it.")
                npc<Quiz>("Do you know of three different people who can teleport you there?")
                player<Confused>(if (get("last_npc_teleport_to_rune_essence_mine", "aubury") != "aubury") "I think so?" else "Maybe?")
                npc<Neutral>("Well if not, I'm sure one of those fools in the Order of Wizards can tell you. Now, here's the orb.")
                if (!inventory.add("scrying_orb")) {
                    item("scrying_orb", 400, "The Mage of Zamorak tries to hand you an orb, but you don't have enough room to take it.")
                } else {
                    item("scrying_orb", 400, "The Mage of Zamorak hands you an orb.")
                    set("enter_the_abyss", "scrying")
                    set("enter_abyss_has_orb", true)
                }
            }
            option<Neutral>("No deal.") {
                npc<Angry>("Fine. I will find another way.")
            }
            option<Neutral>("I need to think about it.") {
                npc<Confused>("I will be here once you have decided.")
            }
        }
    }
}
