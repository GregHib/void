package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.client.*
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.engine.map.instance.InstancePool
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.effect.clearTransform
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.player.music.playTrack
import world.gregs.voidps.world.interact.entity.sound.areaSound
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playSound

on<NPCOption>({ npc.id == "gypsy_aris" && option == "Talk-to" }) { player: Player ->
    when (player["demon_slayer", "unstarted"]) {
        "unstarted" -> {
            npc<Talk>("Hello, young one.")
            npc<Talk>("""
                Cross my palm with silver and the future will be
                revealed to you.
            """)
            if (!player.inventory.contains("coins")) {
                player<Upset>("Oh dear. I don't have any money.")
                return@on
            }
            val choice = choice("""
                Okay, here you go.
                Who are you called 'young one'?
                No, I don't believe in that stuff.
                With silver?
            """)
            when (choice) {
                1 -> hereYouGo()
                2 -> whoYouCallingYoung()
                3 -> notBeliever()
                4 -> withSilver()
            }
        }
        "stage1" -> delrithWillCome()
        "stage2" -> howGoesQuest()
        "complete" -> {
            npc<Talking>("Greetings young one.")
            npc<Talking>("You're a hero now. That was a good bit of demon-slaying.")
        }
    }
}

suspend fun NPCOption.whatToDo() {
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        Wally doesn't sound like a very heroic name.
    """)
    when (choice) {
        1 -> cityDestroyer()
        2 -> whereIsHe()
        3 -> notVeryHeroicName()
    }
}

suspend fun NPCOption.howToDo() {
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        So, how did Wally kill Delrith?
    """)
    when (choice) {
        1 -> cityDestroyer()
        2 -> whereIsHe()
        3 -> howWallyWon()
    }
}

suspend fun NPCOption.howWallyWon() {
    player<Unsure>("So, how did Wally kill Delrith?")
    player.playTrack("wally_the_hero") // TODO
    cutscene(player, npc)
}

suspend fun NPCOption.lastQuestions() {
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        Wally doesn't sound like a very heroic name.
        What is the magical incantation?
        Okay, thanks I'll do my best to stop the demon.
    """)
    when (choice) {
        1 -> cityDestroyer()
        2 -> whereIsHe()
        3 -> notVeryHeroicName()
        4 -> {
            incantation()
            lastQuestions()
        }
        5 -> okThanks()
    }
}

suspend fun NPCOption.cityDestroyer() {
    player<Afraid>("""
        How am I meant to fight a demon who can destroy
        cities?
    """)
    npc<Talk>("""
        If you face Delrith while he is still weak from being
        summoned, and use the correct weapon, you will not
        find the task too arduous.
    """)
    npc<Talk>("""
        Do not fear. If you follow the path of the great hero
        Wally, then you are sure to defeat the demon.
    """)
    val choice = choice("""
        Okay, where is he? I'll kill him for you.
        Wally doesn't sound like a very heroic name.
        So how did Wally kill Delrith?
    """)
    when (choice) {
        1 -> whereIsHe()
        2 -> notVeryHeroicName()
        3 -> howWallyWon()
    }
}

suspend fun NPCOption.whereIsHe() {
    player<Cheerful>("Okay, where is he? I'll kill him for you.")
    npc<Chuckle>("Ah, the overconfidence of the young!")
    npc<Talk>("""
        Delrith can't be harmed by ordinary weapons. You
        must face him using the same weapon that Wally used.
    """)
    howToDo()
}

suspend fun NPCOption.notVeryHeroicName() {
    player<Cheerful>("Wally doesn't sound like a very heroic name.")
    npc<Talk>("""
       Yes, I know. Maybe that is why history doesn't
       remember him. However, he was a great hero.
    """)
    npc<Talk>("""
       Who knows how much pain and suffering Delrith would
       have brought forth without Wally to stop him!
    """)
    npc<Talk>("It looks like you are needed to perform similar heroics.")
    howToDo()
}

suspend fun NPCOption.incantation() {
    player<Talk>("What is the magical incantation?")
    npc<Talk>("Oh yes, let me think a second.")
    npc<Talking>("""
        Aright, I think I've got it now, it goes... Camerinthum...
        Aber... Purchai.,. Gabindo.,. Carlem. Have you got that?
    """)
    player<Talking>("I think so, yes.")
}

suspend fun NPCOption.notBeliever() {
    player<Talk>("No, I don't believe in that stuff.")
    npc<Upset>("Ok suit yourself.")
}

suspend fun NPCOption.hereYouGo() {
    player<Talk>("Okay, here you go.")
    player.inventory.remove("coins", 1)
    npc.setGraphic("aris_orb")
    npc.setAnimation("aris_orb")
    npc<Cheerful>("""
        Come closer and listen carefully to what the future
        holds, as I peer into the swirling mists o the crystal
        ball.
    """)
    areaSound("aris_orb", npc.tile)
    npc<Talk>("I can see images forming. I can see you.")
    npc<Uncertain>("""
        You are holding a very impressive-looking sword. I'm
        sure I recognise it...
    """)
    npc<Uncertain>("There is a big, dark shadow appearing now.")
    npc<Afraid>("Aaargh!")
    player<Unsure>("Are you all right?")
    npc<Afraid>("It's Delrith! Delrith is coming!")
    player<Afraid>("Who's Delrith?")
    npc<Upset>("Delrith...")
    npc<Talk>("Delrith is a powerful demon.")
    npc<Afraid>("""
        Oh! I really hope he didn't see me looking at him
        through my crystal ball!
    """)
    npc<Upset>("""
        He tried to destroy this city 150 years ago. He was
        stopped just in time by the great hero Wally.
    """)
    npc<Upset>("""
        Using his magic sword Silverlight, Wally managed to
        trap the demon in the stone circle just south
        of this city.
    """)
    npc<Surprised>("""
        Ye gods! Silverlight was the sword you were holding in
        my vision! You are the one destined to stop the demon
        this time.
    """)
    whatToDo()
}

suspend fun NPCOption.whoYouCallingYoung() {
    player<Angry>("Who are you calling 'young one'?")
    npc<Talk>("""
        You have been on this world a relatively short time. At
        least compared to me.
    """)
    npc<Talk>("So, do you want your fortune told or not?")
    val choice = choice("""
        Okay, here you go.
        No, I don't believe in that stuff.
        Ooh, how old are you then?
    """)
    when (choice) {
        1 -> hereYouGo()
        2 -> notBeliever()
        3 -> {
            npc<Talking>("""
                Count the number of legs on the stools in the Blue
                Moon inn, and multiply that number by seven.
            """)
            player<Unsure>("Er, yeah, whatever.")
        }
    }
}


val instances: InstancePool by inject()
val chunks: DynamicChunks by inject()

val tabs = listOf(
    "combat_styles",
    "task_system",
    "stats",
    "quest_journals",
    "inventory",
    "worn_equipment",
    "prayer_list",
    "modern_spellbook",
    "emotes",
    "notes"
)

fun cutscene(player: Player, npc: NPC) {
    player.strongQueue("demon_slayer_wally_cutscene") {
        player.start("movement_delay", -1)
        player.open("fade_out")
        player.minimap(Minimap.HideMap)
        statement("", clickToContinue = false)
        tabs.forEach {
            player.close(it)
        }
        player.sendScript(71, 16)
        pause(2)
        val region = Region(12852)
        val instance = instances.obtain()
        val offset = instance.tile.minus(region.tile)
        chunks.copy(region, instance)
        pause(1)
        player.tele(offset.add(3225, 3371), clearInterfaces = false)
        pause(2)
        player.transform("wally")
        player.clearCamera()
        player.moveCamera(offset.add(3227, 3369), 300)
        player.turnCamera(offset.add(3229, 3367), 250)
        player.shakeCamera(type = 1, intensity = 0, movement = 10, speed = 10, cycle = 0)
        player.shakeCamera(type = 3, intensity = 0, movement = 90, speed = 1, cycle = 0)
        player.playSound("rumbling") // TODO
        pause(1)
        player.close("fade_out")
        player.sendScript(71, 16)
        npc<Talk>("gypsy_aris", """
            Wally managed to arrive at the stone circle just as
            Delrith was summoned by a cult of chaos druids...
        """)
        player.sendScript(71, 16)
        npc<Furious>("wally", "Die, foul demon!", clickToContinue = false)
        player.face(Direction.NORTH)
        player.clearCamera()
        player.turnCamera(offset.add(3227, 3367), height = 200, constantSpeed = 2, variableSpeed = 10)
        player.turnCamera(offset.add(3227, 3367), height = 100, constantSpeed = 1, variableSpeed = 10)
        player.shakeCamera(type = 3, intensity = 0, movement = 0, speed = 0, cycle = 0)
        player.playSound("rumbling")
        player.tele(offset.add(3225, 3363), clearInterfaces = false)
        pause(2)
        player.start("no_clip", 2)
        player.running = true
        player.walkTo(offset.add(3227, 3367))
        pause(2)
        player.face(Direction.NORTH)
        player.setAnimation("wally_demon_slay")
        player.playSound("ds_wally_sword", delay = 10) // TODO
        pause(4)

        player.clearCamera()
        player.moveCamera(offset.add(3227, 3369), height = 100, constantSpeed = 2, variableSpeed = 10)
        player.shakeCamera(type = 1, intensity = 0, movement = 10, speed = 5, cycle = 0)
        player.shakeCamera(type = 3, intensity = 0, movement = 2, speed = 50, cycle = 0)
        player.playSound("rumbling")

        npc<Unsure>("wally", "Now, what was that incantation again?")

        npc<Angry>("wally", "Gabindo... Carlem... Camerinthum... Aber... Purchai!") // TODO Random order
        player.open("fade_out")
        pause(4)
        player.close("fade_out")
        player.clearCamera()
        player.shakeCamera(type = 1, intensity = 0, movement = 0, speed = 0, cycle = 0)
        player.shakeCamera(type = 3, intensity = 0, movement = 0, speed = 0, cycle = 0)
        player.playSound("rumbling")
        player.moveCamera(offset.add(3225, 3363), height = 500)
        player.turnCamera(offset.add(3227, 3367), height = 200)
        player.playSound("equip_silverlight")
        player.playJingle("152")
        player.face(Direction.SOUTH_WEST)
//        player.setAnimation("wally_demon_win")
        player.setGraphic("wally_sword_glint")
        npc<Happy>("wally", "I am the greatest demon slayer EVER!")
        npc<Talk>("""
            By reciting the correct magical incantation, and
            thrusting Silverlight into Delrith while he was newly
            summoned, Wally was able to imprison Delrith in the
            stone table at the centre of the circle.
        """)
        statement("", clickToContinue = false)
        player.open("fade_out")
        pause(2)
        player.tele(3203, 3424)
        player.face(Direction.WEST)
        instances.free(instance)
        tabs.forEach {
            player.open(it)
        }
        player.close("fade_out")
        player.stop("movement_delay")
        player.clearMinimap()
        player.clearCamera()
        player.clearTransform()
        player["demon_slayer"] = "stage1"
        player.mode = Interact(player, npc, NPCOption(player, npc, npc.def, "Talk-to"))
    }
}

suspend fun NPCOption.withSilver() {
    // https://www.youtube.com/watch?v=3Y_pDnT3RhM
    player<Unsure>("With silver?")
    npc<Talking>("""
        Oh, sorry, I forgot. With gold, I mean. They haven't
        used silver coins since before you were born! So, do
        you want your fortune told?
    """)
    val choice = choice("""
        Ok, here you go.
        No, I don't believe in that stuff.
    """)
    when (choice) {
        1 -> hereYouGo()
        2 -> notBeliever()
    }
}

suspend fun NPCOption.delrithWillCome() {
    npc<Upset>("Delrith will come forth from the stone circle again.")
    npc<Upset>("""
        I would imagine an evil sorcerer is already beginning
        the rituals to summon Delrith as we speak.
    """)
    player["demon_slayer"] = "stage2"
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        What is the magical incantation?
        Where can I find Silverlight?
    """)
    when (choice) {
        1 -> cityDestroyer()
        2 -> whereIsHe()
        3 -> {
            incantation()
            lastQuestions()
        }
        4 -> {
            whereSilverlight()
            lastQuestions()
        }
    }
}

suspend fun NPCOption.whereSilverlight() {
    player<Angry>("Where can I find Silverlight?")
    npc<Talk>("""
        Silverlight has been passed down by Wally's
        descendants. I believe it is currently in the care of one
        of the king's knights called Sir Prysin.
    """)
    npc<Happy>("""
        He shouldn't be too hard to find. He lives in the royal
        palace in this city. Tell him Gypsy Aris sent you.
    """)
}

suspend fun NPCOption.howGoesQuest() {
    npc<Cheerful>("Greetings. How goes thy quest?")
    player<Talk>("I'm still working on it.")
    npc<Talk>("""
        Well if you need any advice I'm always here, young
        one.
    """)
    val choice = choice("""
        What is the magical incantation?
        Where can I find Silverlight?
        Stop calling me that!
        Well I'd better press on with it.
    """)
    when (choice) {
        1 -> incantationReminder()
        2 -> silverlightReminder()
        3 -> stopCallingMeThat()
        4 -> {
            player<Talk>("Well I'd better press on with it.")
            npc<Talk>("See you anon.")
        }
    }
}

suspend fun NPCOption.okThanks() {
    player<Talk>("Ok thanks. I'll do my best to stop the demon.")
    npc<Cheerful>("Good luck, and may Guthix be with you!")
}

suspend fun NPCOption.silverlightReminder() {
    whereSilverlight()
    val choice = choice("""
        Ok thanks. I'll do my best to stop the demon.
        What is the magical incantation?
    """)
    when (choice) {
        1 -> okThanks()
        2 -> incantationReminder()
    }
}

suspend fun NPCOption.incantationReminder() {
    incantation()
    val choice = choice("""
        Ok thanks. I'll do my best to stop the demon.
        Where can I find Silverlight?
    """)
    when (choice) {
        1 -> okThanks()
        2 -> silverlightReminder()
    }
}

suspend fun NPCOption.stopCallingMeThat() {
    player<Furious>("Stop calling me that!")
    npc<Talk>("In the scheme of things you are very young.")
    val choice = choice("""
        Ok but how old are you?
        Oh if it's in the scheme of things that's ok.
    """)
    when (choice) {
        1 -> {
            player<Talk>("Ok, but how old are you?")
            npc<Talk>("""
                Count the number of legs on the stools in the Blue
                Moon inn, and multiply that number by seven.
            """)
            player<Talk>("Er, yeah, whatever.")
        }
        2 -> {
            player<Cheerful>("Oh if it's in the scheme of things that's ok.")
            npc<Cheerful>("You show wisdom for one so young.")
        }
    }
}