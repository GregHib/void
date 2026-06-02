package content.area.kandarin.yanille

import content.entity.npc.shop.buy
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class BartenderDragonInn : Script {

    init {
        npcOperate("Talk-to", "bartender_dragon_inn") { (target) ->
            npc<Quiz>("What can I get you?")
            player<Quiz>("What's on the menu?")
            npc<Neutral>("Dragon Bitter and Greenman's Ale, oh and some cheap beer.")
            choice {
                option<Neutral>("I'll try the Dragon Bitter.") {
                    npc<Neutral>("Ok, that'll be two coins.")
                    if (buy("dragon_bitter", 2)) {
                        message("You buy a pint of Dragon Bitter.")
                    }
                }
                option<Neutral>("Can I have some Greenman's Ale?") {
                    npc<Neutral>("Ok, that'll be ten coins.")
                    if (buy("greenmans_ale", 10)) {
                        player<Neutral>("Ok, here you go.")
                        message("You buy a pint of Greenman's Ale.")
                    }
                }
                option<Neutral>("One cheap beer please!") {
                    npc<Neutral>("That'll be 2 gold coins please!")
                    if (buy("beer", 2)) {
                        item("beer", "You buy a pint of cheap beer.")
                        npc<Neutral>("Have a super day!")
                    }
                }
                option<Neutral>("I'll give it a miss I think.") {
                    npc<Neutral>("Come back when you're a little thirstier.")
                }
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_dragon_inn") { (target) ->
            if (containsVarbit("barcrawl_signatures", "fire_brandy")) {
                return@itemOnNPCOperate
            }
            barCrawl(target)
        }

        // ===== Tankard =====
        itemOnNPCOperate("dragon_inn_tankard", "bartender_dragon_inn") {
            item(item = "dragon_inn_tankard", text = "You show the tankard to the Inn Keeper.")
            if (get("thzfe_showntankard", false)) {
                tankardRepeat()
            } else {
                tankardFirstTime()
            }
        }

        // ===== Bad portrait =====
        itemOnNPCOperate("zogre_sithik_portrait_bad", "bartender_dragon_inn") {
            item(item = "zogre_sithik_portrait_bad", text = "You show the sketch to the Inn keeper.")
            npc<Happy>(
                "Who's that? I mean, I guess it's a picture of a person isn't it? Sorry...you've " +
                    "got me? And before you ask, you're not putting it up on my wall!",
            )
            player<Shifty>("It's a portrait of Sithik Ints...don't you recognise him?")
            npc<Shifty>(
                "I'm sorry, I really am, but I just don't see it...can you make a better picture?",
            )
            player<Sad>("I'll try...")
        }

        // ===== Good portrait =====
        itemOnNPCOperate("zogre_sithik_portrait_good", "bartender_dragon_inn") { (target) ->
            item(item = "zogre_sithik_portrait_good", text = "You show the portrait to the Inn keeper.")
            if (get("thzfe_innkeepermugshown", false)) {
                npc<Neutral>(
                    "Yeah, I recognise that Geezer, he was talking to one of my customers the " +
                        "other day.",
                )
            } else {
                signPortrait(target)
            }
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        effects = {
            levels.drain(Skill.Attack, 6)
            levels.drain(Skill.Defence, 6)
        },
    )

    // ===== First-time tankard reveal =====

    private suspend fun Player.tankardFirstTime() {
        player<Neutral>(
            "Hello there, I found this tankard in an ogre tomb cavern. It has the emblem of " +
                "this Inn on it and I wondered if you knew anything about it?",
        )
        set("thzfe_showntankard", true)
        npc<Neutral>(
            "Oh yes, this is Brentle's mug...I'm surprised he left it just lying around down " +
                "some cave. He's quite protective of it.",
        )
        player<Neutral>("Brentle you say? So you knew him then?")
        npc<Neutral>(
            "Yeah, this belongs to 'Brentle Vahn', he's quite a common customer, though I've " +
                "not seen him in a while.",
        )
        npc<Shifty>(
            "He was talking to some shifty looking wizard the other day. I don't know his " +
                "name, but I'd recognise him if I saw him.",
        )
        player<Neutral>(
            "Hmm, I'm sorry to tell you this, but Brentle Vahn is dead - I believe he was " +
                "murdered.",
        )
        npc<Shock>("Noooo! I'm shocked...")
        npc<Neutral>(
            "...but not surprised. He was a good customer...but I knew he would sell his " +
                "sword arm and do many a dark deed if paid enough.",
        )
        npc<Sad>(
            "If you need help bringing the culprit to justice, you let me know.",
        )
    }

    // ===== Repeat tankard showing =====

    private suspend fun Player.tankardRepeat() {
        player<Happy>(
            "Hello again. Can you tell me what you know about this tankard again please?",
        )
        npc<Neutral>(
            "Oh yes, Brentle's tankard. Yeah, you've shown me this already. It belonged to " +
                "Brentle Vahn, he was quite a common customer, though I've not seen him in " +
                "a while.",
        )
        npc<Shifty>(
            "He was talking to some shifty looking wizard the other day. I don't know his " +
                "name, but I'd recognise him if I saw him.",
        )
    }

    // ===== Good portrait + sign-it sequence =====

    private suspend fun Player.signPortrait(npc: NPC) {
        npc<Neutral>(
            "Yeah, that's the guy who was talking to Brentle Vahn the other day! Look at " +
                "those eyes, never a more shifty looking pair will you ever see!",
        )
        player<Neutral>(
            "Hmm, you've just identified the man who I think sent Brentle Vahn to his death.",
        )
        player<Neutral>(
            "I'm trying to bring him to justice with the Wizards' Guild grand secretary. Do " +
                "you think you could sign this portrait to say that he was talking to Brentle " +
                "Vahn.",
        )
        npc<Neutral>("I can and I will!")
        npc.anim("human_mapping")
        sound("zogre_writing")
        inventory.remove("zogre_sithik_portrait_good")
        addOrDrop("zogre_sithik_portrait_signed")
        set("thzfe_innkeeperportraitshown", true)
        item(item = "zogre_sithik_portrait_signed", text = "The Dragon Inn bartender signs the portrait.")
        player<Happy>("Many thanks for your help, it's really very good of you.")
        npc<Neutral>("Not at all, just doing my part.")
    }
}
