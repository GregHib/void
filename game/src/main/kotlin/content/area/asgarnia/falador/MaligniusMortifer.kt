package content.area.asgarnia.falador

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class MaligniusMortifer : Script {

    private companion object {
        const val SECATEURS_PRICE = 40_000
    }

    private enum class NecromancyChoice {
        SUMMONING,
        MELZAR,
        FUNGI,
    }

    init {
        npcOperate("Talk-to", "malignius_mortifer") { (target) ->
            npc<Neutral>("So, $name, your curiosity leads you to speak to me?")
            mainMenu(target)
        }
        itemOnNPCOperate(npc = "malignius_mortifer") {
            npc<Angry>("Oi - stop waving your items at me!")
        }
        itemOption("Open", "bone_seeds") {
            val malignius = NPCs.at(tile.regionLevel).firstOrNull { it.id == "malignius_mortifer" }
            if (malignius != null && tile.distanceTo(malignius.tile) <= 5) {
                talkWith(malignius)
                npc<Angry>("You should take that pot a bit further away from me before you open it!")
                return@itemOption
            }
        }
    }

    private suspend fun Player.mainMenu(target: NPC) {
        if (hasSwanSongDialogue()) {
            swanSongMenu(target)
            return
        }
        defaultMenu(target)
    }

    private suspend fun Player.defaultMenu(target: NPC) {
        choice("What would you like to say?") {
            option<Quiz>("Who are you and what are you doing here?") {
                whoAreYou(target)
            }
            option<Quiz>("Can you teach me something about magic?") {
                teachMagic()
            }
            option<Quiz>("Where can I get clothes like those?") {
                clothes(target)
            }
            option<Confused>("Actually, I don't want to talk to you.") {
                leave()
            }
        }
    }

    private suspend fun Player.swanSongMenu(target: NPC) {
        choice("What would you like to say?") {
            option<Quiz>("About those enchanted secateurs again...") {
                enchantedSecateurs()
            }
            if (!carriesItem("brown_apron")) {
                option<Quiz>("Could you spare me a brown apron?") {
                    brownApron()
                }
            }
            option<Quiz>("Can I have some more bone seeds?") {
                boneSeeds()
            }
            option<Neutral>("... MORE ...") {
                swanSongMoreMenu(target)
            }
        }
    }

    private suspend fun Player.swanSongMoreMenu(target: NPC) {
        choice("What would you like to say?") {
            option<Quiz>("Who are you and what are you doing here?") {
                whoAreYou(target)
            }
            option<Quiz>("Can you teach me something about magic?") {
                teachMagic()
            }
            option<Quiz>("About those skeletal magi in the Colony...") {
                skeletalMagi()
            }
            option<Quiz>("Where can I get clothes like those?") {
                clothes(target)
            }
            option<Confused>("Actually, I don't want to talk to you.") {
                leave()
            }
        }
    }

    private suspend fun Player.brownApron() {
        player<Quiz>("Could you spare me a brown apron?")
        npc<Neutral>("Oh, if you must.")
        addOrDrop("brown_apron")
        item("brown_apron", "Malignius Mortifer hands you a brown apron.")
    }

    private suspend fun Player.leave() {
        npc<Angry>("Bah! Then go away!")
    }

    private suspend fun Player.whoAreYou(target: NPC) {
        npc<Bored>("I am the great Malignius Mortifer, wielder of strange and terrible powers. These lowly followers of mine are dedicated students of the magical arts. Their business is to follow me and learn all they can.")
        player<Laugh>("They don't look very tough.")
        npc<Shifty>("You may believe that, but even if you strike one down, another will rise up within minutes.")
        player<Bored>("Yeah, right.")
        npc<Neutral>("Each of my followers is a master of his chosen element. His life becomes bound to that element in a way you could not hope to understand.")
        player<Quiz>("And what do you do?")
        npc<Shifty>("I am mastering a branch of magic that few dare to attempt: Necromancy! The fools in the Guild of Wizards shun anyone who practices this art, but there are a few in this land who know the rudiments.")
        if (questCompleted("zogre_flesh_eaters")) {
            npc<Neutral>("Grayzag, Invrigar, Sithik... Even Melzar studied the methods of necromancy, until an accident affected his mind. Now his spells tend to result in...")
        } else {
            npc<Neutral>("Grayzag and Invrigar... Even Melzar studied the methods of necromancy, until an accident affected his mind. Now his spells tend to result in...")
        }
        npc<Sad>("... well, let us simply say that he does NOT raise armies of undead minions.")
        necromancyMenu(target)
    }

    private suspend fun Player.necromancyMenu(target: NPC, previousChoice: NecromancyChoice? = null) {
        choice {
            if (previousChoice != NecromancyChoice.SUMMONING) {
                option("Is this like Summoning?") {
                    player<Quiz>("Is this like Summoning? Can you teach me?")
                    npc<Shifty>("The obscure art of necromancy has nothing to do with Summoning.")
                    npc<Neutral>("Through necromancy, I may resurrect the bodies of those who have fallen, and bend them to my will, and even command them to tell me hidden knowledge.")
                    npc<Neutral>("I shall not pass on my secrets lightly to any passer-by!")
                    player<Shifty>("Even a mighty hero like me?")
                    when (alignment()) {
                        "guthix" -> npc<Angry>("Deluded lover of Guthix, you will get no secrets from me!")
                        "saradomin" -> npc<Angry>("Saradominist Fool! You will get no secrets from me!")
                        "zamorak" -> npc<Angry>("Although I note your respects for our Lord Zamorak, I am afraid the subtle secrets of necromancy must remain beyond your grasp.")
                        else -> npc<Neutral>("There are ways through which you may summon creatures to your side, but my own work is of a different sort.")
                    }
                    necromancyMenu(target, NecromancyChoice.SUMMONING)
                }
            }
            if (previousChoice != NecromancyChoice.MELZAR) {
                option<Quiz>("What happened to Melzar?") {
                    melzar()
                    necromancyMenu(target, NecromancyChoice.MELZAR)
                }
            }
            if (previousChoice != NecromancyChoice.FUNGI) {
                option<Quiz>("Why do you keep making fungi appear?") {
                    fungi()
                    necromancyMenu(target, NecromancyChoice.FUNGI)
                }
            }
            option<Neutral>("Thanks, I'll be off now.") {
                npc<Shifty>("I am sure your curiosity will eventually draw you to speak to me again, $name.")
            }
        }
    }

    private suspend fun Player.melzar() {
        npc<Neutral>("The forces required to wield power over the dead are very difficult to control.")
        npc<Neutral>("Melzar's concentration slipped, and his spell rebounded upon him. Although he did not die, his mind broke.")
        npc<Neutral>("Now he dwells amidst the beasts that fill his strange home, and when he attempts to do magic he gets... amusing results.")
    }

    private suspend fun Player.fungi() {
        npc<Shifty>("Necromancy is... still not fully understood. So few people dare to study it. Although my spells usually raise whichever undead creature I desire, they sometimes misfire; as Melzar has learnt!")
    }

    private suspend fun Player.enchantedSecateurs() {
        player<Quiz>("About those enchanted secateurs again...")
        npc<Neutral>("What now, $name, can't you just leave me alone to practice my art?")
        choice {
            option<Quiz>("Can I get another set of magic secateurs?") {
                npc<Neutral>("Well I guess it's your lucky day! Since your last visit I've been thinking, I never use my enchanted secateurs anymore, I could sell them to you, if you like.")
                player<Neutral>("That's great!")
                player<Quiz>("How much would they cost?")
                npc<Neutral>("I could let you have them for a mere 40,000 gp.")
                enchantedSecateursPrice()
            }
            option<Neutral>("Sorry to have bothered you.")
            npc<Neutral>("That's ok, I shouldn't have been so abrupt with you.")
        }
    }

    private suspend fun Player.enchantedSecateursPrice() {
        choice {
            option<Angry>("40,000! That's outrageous!") {
                npc<Neutral>("Well, that's my price. If you change your mind, you can come back and talk to me. Good day.")
            }
            option<Sad>("That's too expensive for me I'm afraid.") {
                npc<Neutral>("Well, that's my price. If you change your mind, you can come back and talk to me. Good day.")
            }
            option<Neutral>("Ok, great.") {
                if (inventory.isFull()) {
                    npc<Angry>("You don't have room to hold them. Stop wasting my time.")
                    return@option
                }
                if (!inventory.contains("coins", SECATEURS_PRICE)) {
                    npc<Angry>("You don't have 40,000 gp with you! Do you take me for a fool?")
                    player<Neutral>("Sorry, I didn't realise. I'll have to visit a bank.")
                    npc<Neutral>("Fine, I might see you later then.")
                    return@option
                }
                if (!inventory.remove("coins", SECATEURS_PRICE)) {
                    return@option
                }
                addOrDrop("magic_secateurs")
                item("magic_secateurs", "Malignius Mortifer hands you a pair of magic secateurs.")
                npc<Neutral>("A pleasure doing business with you, $name.")
            }
        }
    }

    private fun Player.hasSwanSongDialogue(): Boolean = questCompleted("swan_song")

    private suspend fun Player.boneSeeds() {
        val hasAirtightPot = inventory.contains("airtight_pot")
        val hasPotWithLid = inventory.contains("empty_pot") && inventory.contains("pot_lid")
        player<Quiz>("Can I have some more bone seeds?")
        if (!hasAirtightPot && !hasPotWithLid) {
            npc<Neutral>("You fetch me a pot with a lid first.")
            return
        }
        player<Quiz>("Fortunately I do have another pot with a lid.")
        if (hasAirtightPot) {
            if (!inventory.remove("airtight_pot")) {
                return
            }
        } else {
            if (!inventory.remove("empty_pot")) {
                return
            }
            if (!inventory.remove("pot_lid")) {
                return
            }
        }
        npc<Neutral>("Yes, yes, have fun with them!")
        addOrDrop("bone_seeds")
        item("bone_seeds", "Malignius Mortifer hands you some bone seeds.")
    }

    private suspend fun Player.skeletalMagi() {
        player<Quiz>("About those skeletal magi in the Colony - why don't they disappear now the Colony's safe?")
        npc<Laugh>("HAHAHA! I never specified how long they'd stay there!")
        player<Angry>("So when will they go away? They're in the dormitories, and that's a real nuisance for the colonists!")
        npc<Laugh>("HAHAHA!")
        player<Angry>("So you're not going to get rid of them?")
        npc<Laugh>("HAHAHA!")
        player<Neutral>("Oh, forget I asked.")
    }

    private suspend fun Player.teachMagic() {
        if (!questCompleted("rune_mysteries")) {
            npc<Unamused>("I see no glimmer of understanding in your eyes; my time would be wasted trying to teach you of the subtle arts.")
            npc<Neutral>("If you are truly interested in magic, perhaps you should go and speak to the Duke of Lumbridge, who lives east of this place. Perchance he will have a mission for you that will teach you something.")
            return
        }
        npc<Neutral>("Ah, you are an inquisitive fellow. I shall speak of the great Wizards' Tower, destroyed by fire many years ago...")
        npc<Neutral>("Many say it was the greatest building in the history of Gielinor, a magnificent monument to human ingenuity.")
        npc<Neutral>("Yet when humans are offered great power, they so often buy it at the cost of their principles.")
        npc<Neutral>("Wizards who claimed allegiance to Saradomin began to insist that magic be restricted to the few they deemed 'worthy' of such powers.")
        npc<Neutral>("Before long, those who did not share their fatuous obsession with Saradomin were excluded from the Tower completely. This state of affairs could not continue.")
        npc<Neutral>("One dark night, a brave band of magi loyal to the great Lord Zamorak stormed the Tower. Despite heavy losses, they fought their way up the levels until they reached the Great Library.")
        npc<Neutral>("Alas, they were trapped and could not steal the precious records. But before they could be defeated, the bravest of them took it upon himself to blast the writings into nothingness, destroying the library.")
        npc<Neutral>("The ensuing fire raced through the tower, and none escaped its fury. Some say that Lord Zamorak himself fanned the flames to ensure the destruction of his enemies!")
        if (questCompleted("enter_the_abyss")) {
            npc<Neutral>("Much of that knowledge was lost, and some has never yet been rediscovered. Still, your efforts on behalf of my colleagues have not been wasted! Once again, the forces of Zamorak can create unlimited runestones!")
            npc<Neutral>("May the world cower in its boots!")
            player<Neutral>("I see...")
            npc<Neutral>("I bid you farewell for now.")
            return
        }
        npc<Neutral>("Much of that knowledge was lost, and some has never yet been rediscovered. Still, the power of magic remains in the world, and we who dedicate our lives to its study may yet perform great works...")
        player<Neutral>("Thanks, I'll be going now.")
        npc<Neutral>("As you wish.")
    }

    private suspend fun Player.clothes(target: NPC) {
        npc<Angry>("Bah! Our garments are an outward sign of our dominance of the magical arts. You cannot simply buy them in a shop!")
        player<Quiz>("What happens if I kill you and take them?")
        npc<Laugh>("Try it and see!")
        player<Quiz>("How about if you teach me enough about magic so I can wear those clothes too?")
        npc<Unamused>("How about if I turn you into a mushroom to make you stop bothering me?")
        target.anim("human_casting")
        delay(2)
        gfx("flames_of_zamorak_impact")
        transform("fungi")
        player<Neutral>("MMMmmmph!", clickToContinue = false)
        delay(10)
        close("dialogue_chat_np1")
        clearTransform()
    }

    private fun Player.alignment(): String? = when {
        equipment.items.any { it.id.contains("guthix", ignoreCase = true) } -> "guthix"
        equipment.items.any { it.id.contains("saradomin", ignoreCase = true) } -> "saradomin"
        equipment.items.any { it.id.contains("zamorak", ignoreCase = true) } -> "zamorak"
        else -> null
    }
}
