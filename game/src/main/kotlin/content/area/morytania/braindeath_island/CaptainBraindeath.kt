package content.area.morytania.braindeath_island

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.questComplete
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class CaptainBraindeath : Script {
    init {
        npcOperate("Talk-to", "captain_braindeath") { (target) ->
            when (questStage("rum_deal")) {
                2 -> introCutsceneIntro()
                3 -> handoutBlindweedSeed()
                5 -> deliverBlindweed()
                7 -> nextStagnantWater()
                10 -> nextSluglings()
                12 -> slugShockSnap()
                14 -> finalIngredient()
                16 -> spiderInHopper()
                17 -> spokeToCaptainNo()
                18 -> spokeToCaptainYes()
                19 -> offerRum()
                else -> defaultGreeting()
            }
        }
    }

    // ===== Progress 2: Wake-up + intro cutscene =====

    private suspend fun Player.introCutsceneIntro() {
        npc<Quiz>("Are ye alright, ${ladOrLass()}?")
        player<Drunk>("Ohhhh... My head...")
        player<Neutral>("It feels like someone has smacked me one with a bottle...")
        npc<Sad>("Arr... Those devils gave ye a nasty knock when ye came to aid us.")
        npc<Happy>("But now yer here we'll run those evil brain-eatin' dogs off the island fer good!")
        player<Quiz>("What? What is going on here?")
        player<Sad>("I can't seem to remember anything beyond chatting to a man at the docks.")
        npc<Shifty>("Arr. Well, ${ladOrLass()}, that would be Pete, one of my men.")
        npc<Neutral>("He's been out lookin' fer heroes like yerself to aid us in our peril.")
        npc<Shifty>("When ye arrived ye took a nasty knock to the head, so ye probably don't remember agreein' to help us out. But I swear to ye that ye did.")
        player<Quiz>("Okay... I'll buy that. It sounds like something I would do.")
        player<Neutral>("So where am I, and what is going on?")
        npc<Neutral>("Yer on Braindeath Island!")
        npc<Neutral>("Where it lies is a secret, because ye are standin' in the brewery of Cap'n Braindeath, purveyor of the most vitriolic alcoholic beverages in the world!")
        player<Neutral>("Wow!")
        npc<Happy>("I am the notorious alchemist Cap'n Braindeath, and this whole operation be my idea!")
        npc<Neutral>("With my crew of sturdy, upright pirate brewers, we sail the seven seas, distributing cheap 'alcohol' to all and sundry.")
        npc<Neutral>("Well, fer a price, at any rate.")
        player<Neutral>("Oooh!")
        npc<Sad>("These be dark times, though, ${ladOrLass()}. See, a week ago we awoke to find ourselves beseiged.")
        npc<Neutral>("The lads and I have held them off so far, but 'tis only a matter of time before they sweep through the buildin' and put us all to the sword.")
        player<Quiz>("Who?")
        npc<Angry>("Them!")
        statement("The Captain points out of the window...", clickToContinue = false)
        playProtestCutscene()
        player<Neutral>("...protesting?")
        npc<Angry>("Arr, ${ladOrLass()}! That they are!")
        npc<Neutral>("Day and night they seek to break our will with their chantin', and their singin' and their passive resistance!")
        npc<Neutral>("Seems they lost their fightin' spirit after the first few days. Now most of them just protest all the time.")
        player<Quiz>("So, what do you want me to do?")
        npc<Neutral>("Well, me and the lads got our heads together and decided that if we can get their Cap'n drunk enough, perhaps they'll stop protestin'.")
        npc<Neutral>("If that happens, we'll slip out the back and set up shop somewhere else.")
        player<Neutral>("Well, how can I help?")
        npc<Neutral>("Well, first of all, we need someone to go out the front and grow us some Blindweed.")
        npc<Neutral>("'Tis one of the ingredients of our 'rum'.")
        npc<Neutral>("The only problem is that those rottin' fiends have torn up and destroyed all but one of the Blindweed Patches.")
        if (hasSpace()) {
            addOrDrop("blindweed_seed")
            set("rum_deal", "given_seeds")
            npc<Neutral>("Here, ${ladOrLass()}. I'll give ye the seed you'll need fer growin' the herb. Help yerself to the gardenin' equipment in the basement.")
            blindweedWarnings()
        } else {
            set("rum_deal", "met_braindeath")
            npc<Neutral>("Well, I have some Blindweed seeds fer ye. When ye have some free space fer them, come and talk to me.")
        }
    }

    private suspend fun Player.blindweedWarnings() {
        npc<Neutral>("I'll warn ye again that those devils are sat right on top of the patch.")
        npc<Neutral>("Try hecklin' 'em from a distance. Those Swabs may talk a good fight, but if ye can put a scare in 'em they'll keep out of yer way!")
    }

    // ===== Progress 3: Came back with space, hand out the seed =====

    private suspend fun Player.handoutBlindweedSeed() {
        if (hasSpace()) {
            addOrDrop("blindweed_seed")
            set("rum_deal", "given_seeds")
            npc<Happy>("Finally, ${ladOrLass()}! Here, take this seed and grow us some Blindweed.")
            npc<Neutral>("Ye can help yerself to the gardenin' equipment in the basement, just be quick about it!")
            blindweedWarnings()
        } else {
            defaultGreeting()
        }
    }

    // ===== Progress 5: Player either has Blindweed or lost it =====

    private suspend fun Player.deliverBlindweed() {
        if (inventory.contains("blindweed")) {
            player<Happy>("Here! I have your Blindweed!")
            npc<Neutral>("Splendid, ${ladOrLass()}! Go shove it in the Intake Hopper upstairs.")
            set("rum_deal", "show_blindweed")
            npc<Neutral>("We'll beat them zombies yet! Arr!")
        } else {
            player<Sad>("Captain... I grew some blindweed but then I lost it...")
            npc<Neutral>("There there, ${ladOrLass()}. 'Tis all right.")
            if (hasSpace()) {
                npc<Neutral>("Here, I've been savin' some fer an emergency.")
                set("rum_deal", "show_blindweed")
                npc<Neutral>("Go shove that in the Intake Hopper, and try not ter lose this lot!")
                addOrDrop("blindweed")
            } else {
                npc<Neutral>("When ye've quite finished blubbin', come see me and I'll give ye some Blindweed.")
                npc<Neutral>("Clear a little space from yer pack first though, ${ladOrLass()}, or ye'll be hard pressed to hold it.")
            }
        }
    }

    // ===== Progress 7: Stagnant water step =====

    private suspend fun Player.nextStagnantWater() {
        player<Quiz>("Well, that takes care of the Blindweed. What now?")
        npc<Neutral>("Well, now ye've shoved the Blindweed into the mix, what we need is a bucket of stagnant water.")
        player<Quiz>("Where can I get some of that?")
        npc<Happy>("Ye won't have to go far, ${ladOrLass()}, we have a pool of the stuff here!")
        player<Neutral>("Here in the brewery?")
        npc<Laugh>("No, ${ladOrLass()}, that would be a strange thing to have in a brewery!")
        npc<Neutral>("It's up the mountain to the north.")
        player<Quiz>("Up a mountain?")
        npc<Happy>("Well, 'tis technically a volcano, but ye get the general idea.")
        player<Quiz>("And I assume the place is crawling with these zombies?")
        npc<Happy>("No ${ladOrLass()}, there be not a zombie in sight.")
        player<Neutral>("Oh, good!")
        npc<Neutral>("All ye have to do is get past the keen-eyed lookout that's been spottin' my men when I send 'em.")
        npc<Neutral>("I'll tell ye that it won't be easy!")
        player<Sad>("When is it ever...")
        npc<Happy>("Don't ferget yer bucket!")
        if (!inventory.contains("bucket") && hasSpace()) {
            addOrDrop("bucket")
        }
        set("rum_deal", "fetch_water")
    }

    // ===== Progress 10: Sluglings step =====

    private suspend fun Player.nextSluglings() {
        player<Quiz>("So is that everything?")
        npc<Neutral>("No, ${ladOrLass()}, next ye'll need to go outside and catch five loads of Sluglings fer the brew.")
        player<Quiz>("What? Sluglings? That's disgusting!")
        npc<Neutral>("Arr, ${ladOrLass()}, that it be.")
        npc<Shifty>("To a weak-stomached, knock-kneed landlubber...")
        player<Neutral>("Why Sluglings?")
        npc<Neutral>("'Cos, ${ladOrLass()} they're one of our super-secret ingredients!")
        npc<Shifty>("Yer not too susceptible to mind control are ye ${ladOrLass()}?")
        player<Quiz>("Why?")
        npc<Shifty>("Because they have been known te, well, influence people every now and again.")
        if (isSeaSlugComplete()) {
            player<Shock>("They aren't related to those Seaslugs are they?")
            npc<Angry>("Aye they're related! Tis a good job they'll starve if they tried te eat yer brains!")
        } else {
            npc<Happy>("I'm sure ye've got nothing te worry about!")
        }
        player<Quiz>("So what should I do with these Sluglings anyway?")
        npc<Neutral>("Well, ye shove them in the Pressure Barrel in the attic.")
        npc<Shifty>("And then ye...")
        player<Quiz>("And then I...")
        npc<Neutral>("And then ye...")
        npc<Shifty>("Pressurise 'em.")
        player<Shock>("Pressurise them?!")
        npc<Neutral>("Look, we don't have all day, get movin'.")
        if (hasSpace()) {
            set("rum_deal", "catch_creatures")
            if (inventory.contains("fishbowl_and_net")) {
                npc<Neutral>("Arr! I see ye've already got a fishbowl in a net.")
            } else {
                addOrDrop("fishbowl_and_net")
                npc<Neutral>("Here. Ye'll need this to catch them.")
            }
            player<Quiz>("What should I do with it?")
            npc<Neutral>("Just dunk it in the water. I'm sure a clever ${ladOrLass()} like yerself will have no problem.")
            npc<Neutral>("Oh, and if ye haul up some squiddy-looking things, don't tate to shove 'em in the barrel too.")
            npc<Neutral>("They add a special, fishy texture to the drink.")
        } else {
            npc<Neutral>("Ye'll be needin' a fishbowl in a big net to help ye catch them slimy little blighters.")
            set("rum_deal", "catch_creatures")
            npc<Neutral>("Pity ye ain't got any free space to carry one.")
        }
    }

    // ===== Progress 12: Slug-shock recovery =====

    private suspend fun Player.slugShockSnap() {
        player<Drunk>("How could I kill my sluggy brethren...?")
        npc<Angry>("Snap out of it, ${ladOrLass()}! Yer in slug-shock!")
        player<Quiz>("What? Who?")
        player<Neutral>("Gah! Sorry about that.")
        npc<Neutral>("No problem. Well, now ye have just one more ingredient to grab, and then we can get this 'rum' flowin!")
        player<Quiz>("Well, how far away will I have to go to grab it?")
        npc<Happy>("Not far at all, ${ladOrLass()}. Ye've just got to get it from the basement!")
        player<Neutral>("Great! What is it?")
        npc<Neutral>("Hold yer horses, ${ladOrLass()}! While ye was off gallivantin' with yer slimy aquatic playmates, the 'rum' achieved spiritual critical mass.")
        npc<Neutral>("To put it in terms ye'll understand: the brewin' equipment is possessed.")
        player<Shock>("Possessed!")
        npc<Happy>("Don't ye worry yerself about it! This happens all the time.")
        npc<Neutral>("Well, to tell the truth, my lads are a little quicker off the mark, so it only happens occasionally.")
        npc<Neutral>("Not that I'm criticisin' yer performance, ${ladOrLass()}.")
        if (hasSpace()) {
            inventory.add("wrench")
            set("rum_deal", "bless_wrench")
            npc<Happy>("Give the controls a couple of belts with this wrench.")
            npc<Neutral>("One of the lads did a little priestin' on the side before he came here. Get him to bless it and ye'll do fine.")
        } else {
            set("rum_deal", "bless_wrench")
            npc<Happy>("We've got a special item fer dealin' with this problem. Dump some of that stuff yer carryin' and I'll hand it over!")
        }
    }

    // ===== Progress 14: Final ingredient (fever spider body) =====

    private suspend fun Player.finalIngredient() {
        npc<Neutral>("Well, now that ye've got that spirit out of there ye can dump in the final ingredient.")
        player<Quiz>("And that is?")
        npc<Neutral>("We need the body of a diseased Fever Spider!")
        player<Shock>("Remind me never to drink anything you have ever made. Or touched.")
        npc<Neutral>("When yer quite done flappin' yer lips, go down into the basement and whack spiders until ye find a fever spider body.")
        npc<Neutral>("Shove it in the hopper, and then we're in business.")
        when {
            wearingSlayerGloves() -> npc<Happy>("I see yer already wearin' some Slayer Gloves. That'll keep the Fever Spiders from gnawin' yer hands off!")
            inventory.contains("slayer_gloves") -> {
                npc<Neutral>("Ye'll be wanting to put them Slayer Gloves of yours on before ye head off ${ladOrLass()}, as those Fever Spiders carry a nasty disease.")
                npc<Neutral>("They'll give it to ye if ye aren't wearin' somethin' too thick fer them to bite through.")
            }
            else -> {
                npc<Neutral>("Ye may want to hop back to the mainland and see a Slayer Master.")
                npc<Neutral>("These Fever Spiders carry a nasty disease, and ye'll need a pair of Slayer Gloves on to not catch it!")
            }
        }
        set("rum_deal", "kill_spider")
    }

    // ===== Progress 16: Spider in hopper, brew sequence =====

    private suspend fun Player.spiderInHopper() {
        player<Neutral>("Well, I stuck your spider in the hopper, what now?")
        npc<Happy>("Now ye stand well back and watch the glory of brewin' at its best!")
        playBrewCutscene()
        player<Quiz>("Is that it?")
        npc<Happy>("Aye ${ladOrLass()}! Now get outside and feed that stuff to the pirates.")
        npc<Neutral>("Try givin' it to the Captain, he's in charge. Get him bladdered and the rest will fall!")
        when {
            inventory.contains("bucket") -> {
                npc<Neutral>("Ye'll need to use that bucket of yours.  Most stuff can't stand bein' in contact with our 'rum' fer too long.")
                npc<Neutral>("Took us a lot of dissolvin' to work that one out.")
                set("rum_deal", "collect_swill")
            }
            hasSpace() -> {
                inventory.add("bucket")
                npc<Neutral>("Here, ye'll need to use one of these.  The rum tends to eat through almost everything else.")
                set("rum_deal", "collect_swill")
            }
            else -> {
                npc<Neutral>("Oh, and don't try using anything but a bucket. Our 'rum' tends to eat through stuff.")
            }
        }
    }

    // ===== Progress 17/18: Reporting back from the Captain =====

    private suspend fun Player.spokeToCaptainNo() {
        npc<Quiz>("So, what did he say?")
        player<Quiz>("Who?")
        npc<Angry>("The Cap'n!")
        player<Neutral>("Oh! I haven't spoken to him yet!")
        npc<Angry>("Well, get a move on!")
        if (!inventory.contains("unsanitary_swill")) {
            npc<Neutral>("And don't ferget the 'rum'!")
        }
    }

    private suspend fun Player.spokeToCaptainYes() {
        npc<Quiz>("So, what did he say?")
        player<Neutral>("Not much that was coherent.")
        player<Quiz>("Who is Rabid Jack?")
        npc<Shock>("Rabid Jack!")
        npc<Neutral>("THE Rabid Jack!")
        npc<Neutral>("Egad...I haven't heard that name...")
        npc<Shifty>("...before.")
        player<Quiz>("So, who is he?")
        npc<Shifty>("Dunno ${ladOrLass()}.")
        npc<Happy>("Almost as if I aren't changing subjects, well done!")
        npc<Neutral>("With those rottin' dogs legless they'll never keep fighting us now, so we've decided to stay here and keep the 'rum' flowin!")
        npc<Neutral>("Thanks, ${ladOrLass()}. We'd never have managed without ye!")
        completeRumDeal()
    }

    // ===== Progress 19: Post-quest "want some rum?" =====

    private suspend fun Player.offerRum() {
        npc<Quiz>("Fancy some 'rum', ${ladOrLass()}? It's still fresh. Well, fresh-ish...")
        player<Shifty>("No... I think I'll pass.")
    }

    // ===== Default greeting / generic options menu =====

    private suspend fun Player.defaultGreeting() {
        player<Quiz>("So...")
        defaultOptionsChoice()
    }

    suspend fun Player.defaultOptionsChoice() {
        choice {
            whatDoYouWantMeToDo()
            whyTalkLikePirate()
            whatDoYouMakeHere()
        }
    }

    fun ChoiceOption.whatDoYouWantMeToDo(): Unit = option<Neutral>("What exactly do you want me to do?") {
        when (questStage("rum_deal")) {
            3 -> npc<Neutral>("Well, I need ye to go grow me some Blindweed, but ye are carryin' too much stuff fer me to give ye the seed.")
            4 -> growBlindweedReminder()
            6 -> putBlindweedInHopper()
            8 -> goGetStagnantWater()
            9 -> pourBucketInHopper()
            11 -> catchFiveSeaCreatures()
            13 -> clearEvilSpirit()
            15 -> jamBodyInHopper()
        }
    }

    fun ChoiceOption.whyTalkLikePirate(): Unit = option("Why do you talk like a pirate?") {
        player<Neutral>("Why do you talk like a pirate? Didn't you tell me you were an alchemist?")
        npc<Neutral>("Arr, ${ladOrLass()}, 'tis true.")
        npc<Neutral>("However, 'tis also true that I stumbled across the basic recipe fer my most potent of brews in a terrible alchemical accident.")
        npc<Neutral>("See, 'twas a dark and stormy night, and the wind was howlin' around the trees as I worked late into the night.")
        npc<Neutral>("Steppin' too close to a candle with my flask in my hand, I was suddenly swept up in a terrible, yet potently alcoholic, explosion.")
        player<Quiz>("And?")
        npc<Happy>("Well' ${ladOrLass()}, it seems the fumes from that first batch of me 'rum' did strange things to me brain.")
        npc<Neutral>("I don't remember the exact words the healers used, but apparently the stuff burned out the tiny, specialised part of me brain that tells me not to talk like a pirate.")
        player<Neutral>("That... that... that sounds utterly impossible!")
        npc<Angry>("Arr! That be what I told them!")
    }

    fun ChoiceOption.whatDoYouMakeHere(): Unit = option<Neutral>("So what do you make here anyway?") {
        npc<Happy>("'Rum', ${ladOrLass()}!")
        npc<Neutral>("The finest, most potent, most flammable and most debilitatin' 'rum' in the whole of RuneScape!")
        player<Neutral>("Rum, eh?")
        npc<Angry>("No, ${ladOrLass()}, 'rum'!")
        player<Quiz>("What's the difference?")
        npc<Neutral>("Well, see, it's like this.")
        npc<Neutral>("If we called the stuff we make 'rum' without makin' the little quote gestures every time, then the Cookin' Guild has promised to do entertaininly painful things to us with whisks.")
        npc<Neutral>("See, technically -")
        npc<Shifty>("And by that I mean technically according to the Disposal of Hazardous Waste Act and the Health and Safety Laws -")
        npc<Neutral>("technically, what we're brewin' here is Artificially Produced Hyper Condensed Sweetened 'Rum' Flavour Distillate.")
        player<Quiz>("Riiiiiiiight...")
        npc<Neutral>("So ye see, ${ladOrLass()}, we just call it 'rum' because the real name be a bit of a mouthful.")
        npc<Happy>("Want a drop?")
        player<Shifty>("No thanks... I think I'll skip it for now.")
    }

    // ===== Per-progress reminder branches from the options menu =====

    private suspend fun Player.growBlindweedReminder() {
        npc<Neutral>("Arr, well I want ye to get outside and grow some Blindweed.")
        npc<Neutral>("Best be careful, ${ladOrLass()}, fer them pirates will skin ye alive if they catch ye.")
        when {
            get("farming_blindweed_patch_braindeath_island", 0) >= 4 -> {
                npc<Happy>("Ye'll need ter keep an eye on yer plants, ${ladOrLass()}.")
                npc<Neutral>("Wouldn't want them pirates to trample all over 'em, would ye?")
            }
            inventory.contains("blindweed_seed") -> {
                // Player already has the seed — nothing more to say
            }
            else -> {
                player<Quiz>("And what happens if I lose the seed?")
                npc<Angry>("Then I'll have ye flogged, hung, slapped with a haddock and sent back out there to fight the zombie hordes with little more than harsh language!")
                if (hasSpace()) {
                    npc<Neutral>("Arr... Here, ${ladOrLass()}. Just take this and get goin'!")
                    inventory.add("blindweed_seed")
                } else {
                    npc<Neutral>("Come back and see me when ye ain't loaded down with stuff, and I'll give ye another.")
                }
            }
        }
    }

    private suspend fun Player.putBlindweedInHopper() {
        npc<Neutral>("I want ye to go put yer Blindweed in the intake hopper, and to be sharp about it!")
        if (inventory.contains("blindweed")) {
            return
        }
        player<Quiz>("And what happens if I lose the Blindweed between here and the intake hopper?")
        player<Shifty>("Theoretically, of course, as it would be almost impossible for me to simply lose the stuff in the time it will take me.")
        npc<Angry>("Gah!")
        npc<Neutral>("Yer nothin' better than a sea-slug wearin', lilly-legged, bandy-livered landlubber!")
        if (hasSpace()) {
            npc<Neutral>("Here, I was savin' a little fer an emergency, so take this and shove it in the hopper before I bung ye in there and turn the handle!")
            addOrDrop("blindweed")
            player<Neutral>("You had some all the time?")
            player<Neutral>("Why was I risking my neck killing pirates to grow some if you had some all along?")
            npc<Neutral>("Less talk, ${ladOrLass()}, haven't ye got a hopper to be fillin'?")
        } else {
            npc<Neutral>("I've got a little put aside fer an emergency. Drop some of that junk yer carryin', and come see me to get it.")
        }
    }

    private suspend fun Player.goGetStagnantWater() {
        npc<Neutral>("To get off yer lazy behind and head out to the stagnant lake on top of the volcano.")
        npc<Neutral>("Chop chop, ${ladOrLass()}.")
        if (!inventory.contains("bucket") && hasSpace()) {
            inventory.add("bucket")
        }
        npc<Happy>("Don't ferget yer bucket!")
    }

    private suspend fun Player.pourBucketInHopper() {
        npc<Neutral>("I want ye to go pour yer bucket into yonder hopper.")
        npc<Neutral>("Take yer time, ${ladOrLass()}, we're only bein' besieged by zombies.")
        if (inventory.contains("bucket_of_water_stagnant")) {
            return
        }
        // Stolen-bucket scene
        player<Shifty>("Err... Captain...")
        npc<Neutral>("Aye, what is it, ${ladOrLass()}?")
        player<Sad>("Well, on my way over to the hopper some big boy stole the bucket and ran away...")
        npc<Angry>("Give me strength!")
        npc<Neutral>("If ye weren't so good at distractin' the zombies I'd skin ye alive!")
        if (hasSpace()) {
            inventory.add("bucket_of_water_stagnant")
            npc<Neutral>("Take this bucket and pour it into the hopper. If ye don't get it right this time I'll have yer guts fer garters!")
        } else {
            npc<Neutral>("Well, 'tis a good job I saved a little, isn't it? Now get rid of something so I can give it to ye.")
        }
    }

    private suspend fun Player.catchFiveSeaCreatures() {
        npc<Neutral>("I want ye to go outside and catch five sea creatures from the squid fishin' spot.")
        npc<Neutral>("When ye've grabbed them, jam 'em in the barrel in the attic and pressurise 'em.")
        if (inventory.contains("fishbowl_and_net")) {
            return
        }
        player<Quiz>("How am I suppost to catch these things?")
        npc<Neutral>("Ye'll be needin' to use a fishbowl in a big net to snare these little devils.")
        player<Quiz>("And if I don't have one?")
        when {
            !inventory.contains("fishbowl") && inventory.contains("big_fishing_net") -> {
                if (hasSpace()) {
                    inventory.add("fishbowl")
                    npc<Angry>("Yer lucky all my precious fish died last week, ${ladOrLass()}. Take this and wrap it in that net, then go grab some Sluglings!")
                } else {
                    npc<Angry>("Well, I'll give ye a fishbowl fer that net of yours if ye'll drop some of that clutter in yer pack.")
                }
            }
            !inventory.contains("big_fishing_net") && inventory.contains("fishbowl") -> {
                npc<Angry>("Gah! I don't believe it!")
                if (hasSpace()) {
                    inventory.add("big_fishing_net")
                    npc<Neutral>("Here, wrap this around yer fishbowl and get back out there!")
                } else {
                    npc<Neutral>("Well, I'll give ye a net fer that fishbowl of yours if ye'll drop some of that clutter in yer pack.")
                }
            }
            else -> {
                npc<Angry>("Why, I oughta...")
                if (hasSpace()) {
                    npc<Neutral>("Here! Try not ter lose this one! These things don't grow on trees, ye know.")
                    inventory.add("fishbowl_and_net")
                } else {
                    npc<Neutral>("Come see me when yer not so laden down. I've got another one fer ye if ye need it.")
                }
            }
        }
    }

    private suspend fun Player.clearEvilSpirit() {
        npc<Neutral>("I want ye to go clear the Evil Spirit out of the brewin' controls. Use the wrench I gave ye, but get it blessed first.")
        when {
            inventory.contains("holy_wrench") -> npc<Neutral>("I see ye've got the wrench good and blessed, so go whack the controls till the Evil Spirit pops out.")
            inventory.contains("wrench") -> {
                // Player has the unblessed wrench — nothing more to say
            }
            else -> {
                player<Shifty>("Mumblemumblewhatwrenchmumble...")
                npc<Angry>("What were that?")
                player<Shifty>("I said, what if I've lost the wrench?")
                if (hasSpace()) {
                    inventory.add("wrench")
                    npc<Angry>("Arr! I'd get more work out of a minty flavoured brick!")
                    npc<Neutral>("If ye lose this one I'll have ye turned inside out, covered with spiders and turned rightside in so they'll eat ye alive!")
                    player<Shock>("Steady on!")
                } else {
                    npc<Angry>("Arr, ye landlubber!")
                    npc<Neutral>("Calm down, remember yer ulcer...")
                    npc<Neutral>("Look, I've got another wrench, but ye don't have any free space fer it. Come back when ye do.")
                }
            }
        }
    }

    private suspend fun Player.jamBodyInHopper() {
        when {
            wearingSlayerGloves() -> npc<Happy>("I see yer already wearin' some Slayer Gloves. That'll keep the Fever Spiders from gnawin' yer hands off!")
            inventory.contains("fever_spider_body") -> {
                npc<Happy>("I want ye to jam that body into the hopper and come see me.")
                npc<Neutral>("Arr! Yer not as slow as I took ye fer!")
            }
            else -> {
                npc<Neutral>("I want ye to go kill a Fever Spider and jam its body in the hopper.")
                npc<Neutral>("Whenever yer ready, ${ladOrLass()}.")
            }
        }
    }

    // ===== Helpers =====

    private fun Player.ladOrLass(): String = if (male) "lad" else "lass"

    private fun Player.hasSpace(): Boolean = inventory.spaces > 0

    private fun Player.wearingSlayerGloves(): Boolean = equipped(EquipSlot.Hands).id == "slayer_gloves"

    private fun Player.isSeaSlugComplete(): Boolean = get("sea_slug", "") == "completed"

    private suspend fun Player.playProtestCutscene() {
        open("fade_out")
        delay(3)
        // 1957 is an unnamed, model-less npc; transforming into it hides the player while
        // the camera pans over the protest
        transform("rupert_the_beard_3")

        jingle("zombie_pirates")
        tele(2152, 5098, 0)
        moveCamera(
            tile = Tile(2145, 5088),
            height = 310,
            acceleration = 100,
        )
        turnCamera(
            tile = Tile(2145, 5066),
            height = 300,
            acceleration = 100,
        )
        delay(1)

        moveCamera(
            tile = Tile(2156, 5088),
            height = 310,
            speed = 2,
            acceleration = 0,
        )
        delay(2)

        close("fade_out")
        delay(2)

        open("rum_deal_title")
        delay(17)

        open("fade_out")
        delay(2)

        close("rum_deal_title")
        clearTransform()
        tele(2144, 5108, 1)
        delay(2)

        clearCamera()
        open("fade_in")
        player<Quiz>("Are they...")
    }

    private suspend fun Player.playBrewCutscene() {
        moveCamera(
            tile = Tile(2144, 5106),
            height = 250,
        )
        turnCamera(
            tile = Tile(2144, 5099),
            height = 100,
        )
        delay(3)
        set("rum_deal_brewing_control", 2)
        delay(3)
        clearCamera()
    }

    private fun Player.completeRumDeal() {
        jingle("quest_complete_1")
        exp(Skill.Prayer, 7000.0)
        exp(Skill.Fishing, 7000.0)
        exp(Skill.Farming, 7000.0)
        addOrDrop("holy_wrench")
        inc("quest_points", 2)
        AuditLog.event(this, "quest_completed", "rum_deal")
        set("rum_deal", "completed")
        set("rum_deal_pressure_count", 0)
        set("rum_deal_brewing_control", 2)
        set("rum_deal_slugling_count", 0)
        set("rum_deal_karamthulhu_count", 0)
        for (swab in 'a'..'f') {
            set("rum_deal_swab_$swab", 1)
        }
        set("farming_blindweed_patch_braindeath_island", "weeds_0")
        refreshQuestJournal()
        questComplete(
            "Rum Deal",
            "2 Quest Points",
            "7,000 Prayer XP",
            "7,000 Fishing XP",
            "7,000 Farming XP",
            "A Holy Wrench",
            item = "holy_wrench",
        )
    }
}
