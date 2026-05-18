package content.area.morytania.mort_myre_swamp

import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.member.myreque.nature_spirit
import content.quest.member.myreque.sendNatureSpiritReward
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class Filliman : Script {

    var npcTile: Tile = Tile(3439, 9742)

    init {

        npcOperate("Talk-to", "filliman_tarlock_ghost,filliman_tarlock_spirit") { (target) ->
            npcTile = target.tile
            val hasAmulet = equipment.contains("ghostspeak_amulet")
            when {
                nature_spirit < 15 -> earlyEncounter(hasAmulet)
                !hasAmulet -> {
                    npc<Neutral>("Woooo wooo wooo wooo")
                    statement("You cannot understand the ghost.")
                }
                nature_spirit in 15..19 -> {
                    player<Neutral>("Hello?")
                    npc<Shock>("Oh, I understand you! At last, someone who doesn't just mumble. I understand what you're saying!")
                    convinceHesAGhost()
                }
                nature_spirit == 20 -> {
                    player<Neutral>("Hello again!")
                    npc<Sad>(
                        "Oh, hello there, do you still think I am dead? It's hard to see how I could " +
                                "be dead when I'm still in the world, I can see everything quite clearly. " +
                                "And nothing of what you say reflects the truth."
                    )
                    player<Neutral>("Yes, I do think you're dead and I'll prove it somehow.")
                    convinceHesAGhost()
                }
                nature_spirit == 25 -> {
                    player<Neutral>("Hello again..")
                    npc<Sad>(
                        "Oh, hello... Sorry, you've caught me at a bad time, it's just that I've " +
                                "had a sign you see and I need to find my journal."
                    )
                    player<Neutral>("Where did you put it?")
                    npc<Sad>(
                        "Well, if I knew that, I wouldn't still be looking for it. However, I do " +
                                "remember something about a knot? Perhaps I was meant to tie a knot or something?"
                    )
                }
                nature_spirit == 30 -> readJournalRecap()
                nature_spirit == 35 -> {
                    npc<Sad>("Hello there, have you been blessed yet?")
                    player<Neutral>("No, not yet.")
                    npc<Sad>("Well, hurry up!")
                    bloomScrollChoiceLoop()
                }
                nature_spirit == 40 -> hasFungusAtStage40()
                nature_spirit in 45..50 -> {
                    if (inventory.contains("mort_myre_fungus")) {
                        showedFungusScene()
                    } else {
                        npc<Neutral>("Did you manage to get something from nature?")
                        player<Neutral>("Not yet.")
                        bloomScrollChoiceLoop()
                    }
                }
                nature_spirit == 55 -> {
                    npc<Neutral>(
                        "Hello again! I don't suppose you've found out what the other components " +
                                "of the Nature spell are have you?"
                    )
                    componentsHelpLoop()
                }
                nature_spirit == 60 -> {
                    npc<Neutral>("Please come down into the grotto, we have much to discuss.")
                }
                nature_spirit == 65 -> {
                    npc<Neutral>(
                        "Well, hello there again, I was just enjoying the grotto. Many thanks for " +
                                "your help, I couldn't have become a Spirit of nature without you."
                    )
                    transformInGrotto()
                }
                nature_spirit == 70 -> {
                    npc<Neutral>("Have you brought me the silver sickle?")
                    if (inventory.contains("silver_sickle")) {
                        player<Neutral>("Yes, here it is. What are you going to do with it?")
                        npc<Neutral>(
                            "My friend, I will bless it for you and you will then be able to " +
                                    "accomplish great things. Now then, I must cast the enchantment. You can " +
                                    "bless a new sickle by dipping it in the holy water of the grotto."
                        )
                        blessSickleScene()
                    } else {
                        player<Neutral>("No sorry, not yet!")
                        npc<Neutral>("Well, come to me when you have it.")
                        postSickleHelpLoop()
                    }
                }
                nature_spirit in 75..104 -> {
                    npc<Neutral>("Hello again my friend, have you defeated three Ghasts as I asked you?")
                    player<Neutral>("Not yet.")
                    npc<Neutral>("Well, when you do, please come to me and I'll reward you!")
                    ghastHuntHelpLoop()
                }
                nature_spirit == 105 -> {
                    npc<Neutral>("Hello again my friend, have you defeated three Ghasts as I asked you?")
                    player<Neutral>("Yes, I've killed all three and their spirits have been released!")
                    npc<Neutral>(
                        "Many thanks my friend, you have completed your quest! I can now change " +
                                "this place into a holy sanctuary! And forever will it now be an Altar of Nature!"
                    )
                    sendNatureSpiritReward()
                }
                nature_spirit >= 110 -> {
                    npc<Happy>("Welcome to my Altar to Nature! Farewell my friend and keep those Ghasts at bay!")
                    val npc = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_spirit")
                    if (npc != null) {
                        NPCs.remove(npc)
                    }
                }
            }
        }

        itemOnNPCOperate("mirror", "filliman_tarlock_ghost") { interaction ->
            val target = interaction.target
            npcTile = target.tile
            talkWith(target)
            if (!equipment.contains("ghostspeak_amulet")) {
                npc<Neutral>("Woooo wooo wooo wooo")
                statement("You cannot understand the ghost.")
                return@itemOnNPCOperate
            }
            when (nature_spirit) {
                20 -> mirrorScene()
                in 25..109 -> {
                    npc<Happy>("Oh, keep hold of that, I may need it later.")
                }
                else -> statement("The spirit doesn't seem interested in this right now.")
            }
        }

        itemOnNPCOperate("journal_nature_spirit", "filliman_tarlock_ghost") { interaction ->
            val target = interaction.target
            npcTile = target.tile
            talkWith(target)
            if (!equipment.contains("ghostspeak_amulet")) {
                npc<Neutral>("Woooo wooo wooo wooo")
                statement("You cannot understand the ghost.")
                return@itemOnNPCOperate
            }
            when (nature_spirit) {
                25 -> journalScene()
                in 30..109 -> {
                    npc<Happy>("Oh, keep hold of that, I may need it later.")
                }
                else -> statement("The spirit doesn't seem interested in this right now.")
            }
        }
    }

    private suspend fun Player.earlyEncounter(hasAmulet: Boolean) {
        if (!hasAmulet) {
            statement("A shifting apparition appears in front of you.")
            npc<Confused>("Cannot wake up... Where am I?")
            player<Quiz>("Huh? What's this?")
            npc<Confused>("What did I write down now? Put it in the knot hole.")
            if (nature_spirit < 10) {
                nature_spirit = 10
            }
            npc<Drunk>("Ahhrs Oooohh arhhhhAHhhh.")
            player<Shock>("Huh! Now you're just not making any sense at all! I just cannot understand you!")
            return
        }
        player<Quiz>("Hello?")
        npc<Shock>("Oh, I understand you! At last, someone who doesn't just mumble. I understand what you're saying!")
        convinceHesAGhost()
    }

    private suspend fun Player.convinceHesAGhost() {
        if (nature_spirit < 20) {
            nature_spirit = 20
        }
        choice {
            option("I'm wearing an amulet of ghost speak!") { wearingAmulet() }
            option("How long have you been a ghost?") { howLongGhost() }
            option("What's it like being a ghost?") { whatLikeBeingGhost() }
            option<Neutral>("Ok, thanks.")
        }
    }

    private suspend fun Player.howLongGhost() {
        player<Neutral>("How long have you been a ghost?")
        npc<Sad>("What?! Don't be preposterous! I'm not a ghost! How could you say something like that?")
        player<Neutral>("But it's true, you're a ghost... well, at least that is to say, you're sort of not alive anymore.")
        npc<Sad>(
            "Don't be silly, I can see you, I can see that tree. If I were dead, I wouldn't be able " +
                    "to see anything. What you say just doesn't reflect the truth. You'll have to try harder to pull one over on me!"
        )
        convinceHesAGhost()
    }

    private suspend fun Player.whatLikeBeingGhost() {
        player<Neutral>("What's it like being a ghost?")
        npc<Sad>("Oh, it's quite... Oh... Trying to catch me out were you! Anyone can clearly see that I am not a ghost!")
        player<Neutral>(
            "But you are a ghost, look at yourself! I can see straight through you! You're as dead " +
                    "as this swamp! Err... No offence or anything..."
        )
        npc<Sad>(
            "No I won't take offence because I'm not dead and I'm afraid you'll have to come up " +
                    "with some pretty conclusive proof before I believe it. What a strange dream this is."
        )
        convinceHesAGhost()
    }

    private suspend fun Player.wearingAmulet() {
        player<Neutral>("I'm wearing an amulet of ghost speak!")
        npc<Sad>("Why you poor fellow, have you passed away and you want to send a message back to a loved one?")
        player<Neutral>("Err.. Not exactly...")
        npc<Sad>(
            "You have come to haunt my dreams until I pass on your message to a dearly loved one. " +
                    "I understand. Pray, tell me who would you like me to pass a message on to?"
        )
        player<Neutral>("Ermm, you don't understand... It's just that..")
        npc<Sad>("Yes!")
        player<Neutral>("Well, please don't be upset or anything... But you're the ghost!")
        npc<Sad>("Don't be silly now! That in no way reflects the truth!")
        convinceHesAGhost()
    }

    private suspend fun Player.mirrorScene() {
        item(item = "mirror", text = "You use the mirror on the spirit of the dead Filliman Tarlock.")
        player<Neutral>(
            "Here take a look at this, perhaps you can see that you're utterly transparent now!"
        )
        item(item = "mirror", text = "The spirit of Filliman reaches forward and takes the mirror.")
        npc<Quiz>(
            "Well, that is the most peculiar thing I've ever experienced. This mirror must somehow " +
                    "be dysfunctional. Strange how well it reflects the stagnant swamp behind me, but there " +
                    "is nothing of my own visage apparent."
        )
        player<Neutral>(
            "That's because you're dead! Dead as a door nail.. Deader in fact... You bear a " +
                    "remarkable resemblance to wormbait! Err.. No offence..."
        )
        npc<Sad>(
            "I think you might be right my friend, though I still feel very much alive. It is " +
                    "strange how I still come to be here and yet I've not turned into a Ghast."
        )
        npc<Sad>(
            "It must be a sign... Yes a sign... I must try to find out what it means. Now, where " +
                    "did I put my journal?"
        )
        nature_spirit = 25
    }

    private suspend fun Player.journalScene() {
        item(item = "journal_nature_spirit", text = "You give the journal to Filliman.")
        player<Neutral>("Here, I found this, maybe you can use it?")
        npc<Happy>("My journal! That should help me collect my thoughts.")
        item(
            item = "journal_nature_spirit",
            text = "~ The spirit starts leafing through the journal. ~ <br>~ He seems quite distant as " +
                    "he regards the pages. ~ <br>~ After some time the druid faces you again. ~"
        )
        npc<Sad>(
            "It's all coming back to me now. It looks like I came to a violent and bitter end but " +
                    "that's not important now. I just have to figure out what I am going to do now?"
        )
        nature_spirit = 30
        inventory.remove("journal_nature_spirit")
        planChoiceLoop()
    }

    private suspend fun Player.readJournalRecap() {
        npc<Sad>(
            "Thanks for the journal, I've been reading it. It looks like I came to a violent and " +
                    "bitter end but that's not really important I just have to figure out what I am going to do now?"
        )
        planChoiceLoop()
    }

    private suspend fun Player.planChoiceLoop() {
        choice {
            option("Being dead, what options do you think you have?") {
                player<Happy>(
                    "Being dead, what options do you think you have? I'm not trying to be rude " +
                            "or anything, but it's not like you have many options is it? I mean, it's " +
                            "either up or down for you isn't it?"
                )
                npc<Sad>(
                    "Hmm, well you're a poetic one aren't you. Your material world logic stands " +
                            "you in good stead... if you're standing in the material world..."
                )
                planChoiceLoop()
            }
            option<Neutral>("So, what's your plan?") {
                npc<Sad>(
                    "In my former incarnation I was Filliman Tarlock, a great druid of some " +
                            "power. I spent many years in this place, which was once a forest and I " +
                            "would wish to protect it as a nature spirit."
                )
                planChoiceLoop()
            }
            option<Neutral>("Well, good luck with that.") {
                npc<Sad>("Won't you help me to become a nature spirit? I could really use your help!")
                planChoiceLoop()
            }
            option<Neutral>("How can I help?") {
                askHowToHelp()
            }
            option<Neutral>("Ok, thanks.")
        }
    }

    private suspend fun Player.askHowToHelp() {
        npc<Sad>(
            "Will you help me to become a nature spirit? The directions for becoming one are a " +
                    "bit vague, I need three things but I know how to get one of them. Perhaps you can " +
                    "help collect the rest?"
        )
        player<Neutral>("I might be interested, what's involved?")
        npc<Sad>(
            "Well, the book says, that I need, and I quote:- 'Something with faith', 'something " +
                    "from nature' and 'something of the 'spirit-to-become' freely given'. Hmm, I know how " +
                    "to get something from nature."
        )
        player<Neutral>("Well, that does seem a bit vague.")
        npc<Sad>(
            "Hmm, it does and I could understand if you didn't want to help. However, if you " +
                    "could perhaps at least get the item from nature, that would be a start. Perhaps we " +
                    "can figure out the rest as we go along."
        )
        item(
            item = "druidic_spell",
            text = "The druid produces a small sheet of papyrus with some writing on it."
        )
        addOrDrop("druidic_spell")
        nature_spirit = 35
        npc<Sad>(
            "This spell needs to be cast in the swamp after you have been blessed. I'm afraid " +
                    "you'll need to go to the temple to the North and ask a member of the clergy to bless you."
        )
        player<Neutral>("Blessed, what does that do?")
        npc<Sad>(
            "It is required if you're to cast this druid spell. Once you've cast the spell, you " +
                    "should find something from nature. Bring it back to me and then we'll try to figure " +
                    "out the other things we need."
        )
    }

    private suspend fun Player.bloomScrollChoiceLoop() {
        choice {
            option<Neutral>("Could I have another bloom scroll please?") {
                giveAnotherBloomScroll()
            }
            option<Neutral>("Ok, thanks.")
        }
    }

    private suspend fun Player.giveAnotherBloomScroll() {
        if (inventory.contains("druidic_spell")) {
            npc<Angry>("You've already got one! You don't need two!")
            return
        }
        npc<Neutral>("Sure, but please look after this one.")
        addOrDrop("druidic_spell")
        item(item = "druidic_spell", text = "The spirit of Filliman Tarlock gives you another bloom spell.")
    }

    private suspend fun Player.hasFungusAtStage40() {
        if (!inventory.contains("mort_myre_fungus") && !get("ns_brown_correct", false)) {
            player<Neutral>("Hello, I've been blessed but I don't know what to do now.")
            npc<Neutral>(
                "Well, you need to bring 'something from nature', 'something with faith' and " +
                        "'something of the spirit-to-become freely given-"
            )
            player<Neutral>("Yeah, but what does that mean?")
            npc<Neutral>(
                "Hmm, it is a conundrum, however, if you use that Bloom spell I gave you, you " +
                        "should be able to get something from nature. Once you have that, we may be able " +
                        "to puzzle the rest out."
            )
            bloomScrollChoiceLoop()
            return
        }
        npc<Neutral>("Did you manage to get something from nature?")
        showedFungusScene()
    }

    private suspend fun Player.showedFungusScene() {
        item(item = "mort_myre_fungus", text = "You show the fungus to Filliman.")
        player<Neutral>("I picked a fungus that grew when I cast the bloom spell.")
        npc<Neutral>(
            "Wonderful, the mushroom represents 'something from nature'. Now we need to work out " +
                    "what the other components of the spell are!"
        )
        if (nature_spirit <= 50) {
            nature_spirit = 55
        }
        componentsHelpLoop()
    }

    private suspend fun Player.componentsHelpLoop() {
        choice {
            option("What are the things that are needed?") {
                player<Neutral>("What are the things that are needed again?")
                npc<Neutral>(
                    "The three things are: 'Something with faith', 'something from nature' " +
                            "and 'something of the spirit-to-become freely given'."
                )
                player<Neutral>("Ok, and 'something from nature' is the mushroom from the bloom spell you gave me?")
                npc<Neutral>(
                    "Yes, that's correct, that seems right to me. The other things we need " +
                            "are 'something with faith' and 'something of the spirit-to-become freely given'."
                )
                player<Neutral>("Do you have any idea what those things are?")
                npc<Neutral>("I'm sorry my friend, but I do not.")
                componentsHelpLoop()
            }
            option<Neutral>("What should I do when I have those things?") {
                npc<Neutral>(
                    "Ah yes, I looked this up. It says... 'to arrange upon three rocks around " +
                            "the spirit-to-become...'. Then I must cast a spell. As you can see, I've " +
                            "already placed the rocks. I must have planned to do this before I died!"
                )
                player<Neutral>("Can we just place the components on any rock?")
                npc<Neutral>(
                    "Well, the only thing the journal says is that 'something with faith stand " +
                            "south of the spirit-to-become', but I'm so confused now I don't really " +
                            "know what that means. Oh, if only I had all my faculties!"
                )
                componentsHelpLoop()
            }
            option<Neutral>("I think I've solved the puzzle!") {
                npc<Neutral>(
                    "Oh really.. Have you placed all the items on the stones? Ok, well, " +
                            "let's try! <navy>~ The druid attempts to cast a spell. ~"
                )
                if (solvedPuzzle()) {
                    correctSpellScene()
                } else {
                    failSpellScene()
                }
            }
            option<Quiz>("Could I have another bloom scroll please?") {
                giveAnotherBloomScroll()
            }
            option<Neutral>("Ok, thanks.")
        }
    }

    private fun Player.solvedPuzzle(): Boolean =
        get("ns_brown_correct", false) && get("ns_grey_correct", false) &&
                tile.x == 3440 && tile.y == 3335

    private suspend fun Player.failSpellScene() {
        val ghost = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_ghost")
        ghost?.anim("human_casting")
        sound("spirit_transform_start")
        delay(2)
        npc<Confused>(
            "Hmm, something still doesn't seem right. I think we need something more before we can continue."
        )
    }

    private suspend fun Player.correctSpellScene() {
        val ghost = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_ghost") ?: return
        ghost.anim("human_casting", delay = 30)
        sound("spirit_transform_start", delay = 30)
        delay(1)
        ghost.anim("human_casting")
        delay(1)
        sound("spirit_transform_start", delay = 30)
        val fillimanTile = Tile(3440, 3336, 0)
        for (origin in listOf(Tile(3438, 3336, 0), Tile(3440, 3334, 0), Tile(3442, 3336, 0))) {
            origin.shoot(
                id = "druid_shooting_star",
                tile = fillimanTile,
                delay = 30,
                flightTime = 100,
                height = 0,
                endHeight = 48,
                curve = 180,
                offset = 0,
            )
        }
        delay(2)
        nature_spirit = 60
        for (step in 0..2) {
            areaGfx(
                id = "druidicspirit_effect",
                tile = fillimanTile,
                delay = 40 - step * 5,
                height = 128 - step * 64,
            )
        }
        sound("bloom_pears", delay = 30)
        npc<Happy>(
            "Aha, everything seems to be in place! You can come through now into the grotto for the " +
                    "final section of my transformation."
        )
    }

    private suspend fun Player.transformInGrotto() {
        npc<Neutral>(
            "I must complete the transformation now. Just stand there and watch the show, " +
                    "apparently it's quite good!"
        )

        delay(1)
        val ghost = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_ghost") ?: NPCs.add("filliman_tarlock_ghost", npcTile)

        ghost.anim("human_casting", delay = 5)
        sound("spirit_transform", delay = 30)
        delay(4)
        val targetTile = ghost.tile
        val projectileBursts = listOf(
            Tile(3438, 9742, targetTile.level) to 56,
            Tile(3439, 9736, targetTile.level) to 106,
            Tile(3439, 9738, targetTile.level) to 86,
            Tile(3442, 9735, targetTile.level) to 116,
            Tile(3444, 9742, targetTile.level) to 96,
            Tile(3444, 9738, targetTile.level) to 96,
            Tile(3444, 9736, targetTile.level) to 106,
        )
        for ((origin, flightTime) in projectileBursts) {
            origin.shoot(
                id = "druid_shooting_star",
                tile = targetTile,
                delay = 30,
                flightTime = flightTime,
                height = 0,
                endHeight = 32,
                curve = 180,
                offset = 0,
            )
        }
        delay(1)
        ghost.transform("filliman_tarlock_spirit")
        ghost.face(this)
        for (step in 0..2) {
            areaGfx(
                id = "druidicspirit_effect",
                tile = targetTile,
                delay = 40 - step * 5,
                height = 128 - step * 64,
            )
        }
        delay(1)
        nature_spirit = 70
        talkWith(ghost)
        delay(4)

        npc<Neutral>(
            "Hmmm, good, the transformation is complete. Now, my friend, in return for your " +
                    "assistance, I will help you to kill the Ghasts. First bring to me a silver sickle " +
                    "so that I can bless it for you."
        )
        player<Neutral>("A silver sickle? What's that?")
        npc<Neutral>(
            "The sickle is the symbol and weapon of the Druid, you need to construct one of silver " +
                    "so that I can bless it, with its powers you will be able to defeat the Ghasts of Mort Myre."
        )
        postSickleHelpLoop()
    }

    private suspend fun Player.postSickleHelpLoop() {
        choice {
            option<Neutral>("Where would I get a silver sickle?") {
                npc<Neutral>(
                    "You could make one yourself if you're artisan enough. I've heard of a " +
                            "distant sandy place where you can buy the mould that you require, it's " +
                            "similar in many respects to the creating of a holy symbol."
                )
                postSickleHelpLoop()
            }
            option<Neutral>("What will you do to the silver sickle?") {
                npc<Neutral>(
                    "Why, I will give it my blessings so that the very swamp in which you " +
                            "stand will blossom and bloom!"
                )
                postSickleHelpLoop()
            }
            option<Neutral>("How can a blessed sickle help me to defeat the Ghasts?") {
                npc<Neutral>(
                    "My blessings will entice nature to bloom in Mort Myre! And then with " +
                            "nature's harvest you can fill a druids' pouch and release the Ghasts " +
                            "from their torment."
                )
                postSickleHelpLoop()
            }
            option<Neutral>("Ok, thanks.")
        }
    }

    private suspend fun Player.blessSickleScene() {
        val spirit = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_spirit") ?: NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_ghost") ?: return
        spirit.anim(id = "human_casting", delay = 30)
        gfx(
            id = "druidicspirit_bloom_player_spotanim",
            height = 200,
            delay = 120
        )
        sound(id = "prayer_recharge", delay = 30)
        val projectileOrigins = listOf(
            Tile(3441, 9741, spirit.tile.level),
            Tile(3442, 9741, spirit.tile.level),
            Tile(3441, 9740, spirit.tile.level),
            Tile(3442, 9740, spirit.tile.level),
        )
        for (origin in projectileOrigins) {
            origin.shoot(
                id = "druid_shooting_star",
                target = this,
                delay = 30,
                flightTime = 120,
                height = 0,
                endHeight = 42,
                curve = 180,
                offset = 0,
            )
        }
        delay(1)
        anim(id = "druidicspirit_human_bloom", delay = 30)
        delay(1)
        nature_spirit = 75
        inventory.remove("silver_sickle")
        addOrDrop("silver_sickle_b")
        delay(3)
        item(
            item = "silver_sickle_b",
            text = "Your sickle has been blessed! <navy>~ If you lose the blessed sickle, you can " +
                    "bless a new sickle by dipping it in the grotto waters. ~"
        )
        npc<Neutral>(
            "Now you can go forth and make the swamp bloom. Collect nature's bounty to fill a " +
                    "druids pouch. So armed will the Ghasts be bound to you until you flee or they are defeated."
        )
        npc<Neutral>(
            "Before I can make this grotto into an Altar of Nature, I need to be sure that the " +
                    "Ghasts will be kept at bay. Go forth into Mort Myre and slay three Ghasts. You'll " +
                    "be releasing their souls from Mort Myre."
        )
        addOrDrop("druid_pouch")
        item(item = "druid_pouch", text = "The nature spirit gives you an empty pouch.")
        npc<Neutral>(
            "You'll need this in order to collect together nature's bounty. When it contains items, " +
                    "it will bind the Ghast to you until you flee or it is defeated."
        )
    }

    private suspend fun Player.ghastHuntHelpLoop() {
        choice {
            option<Neutral>("How do I get to attack the Ghasts?") {
                npc<Neutral>(
                    "Go forth and with the sickle make the swamp bloom. Collect nature's " +
                            "bounty to fill a druid's pouch. So armed will the Ghasts be bound to " +
                            "you until you flee or they are defeated."
                )
            }
            option<Neutral>("What's this pouch for?") {
                npc<Neutral>(
                    "It is for collecting natures bounty, once it contains the blossomed " +
                            "items of the swamp, it will make the Ghasts appear and you can then attack them."
                )
            }
            option<Neutral>("What can I do with this sickle?") {
                npc<Neutral>(
                    "You may use it wisely within the area of Mort Myre to affect nature's " +
                            "balance and bring forth a bounty of nature's harvest. Once collected " +
                            "into the druid pouch, will the Ghast be apparent."
                )
            }
            option<Neutral>("I've lost my sickle.") {
                npc<Neutral>(
                    "If you should lose the blessed sickle, simply bring another to my altar " +
                            "of nature and refresh it in the grotto waters."
                )
            }
            option<Neutral>("Ok, thanks.")
        }
    }
}
