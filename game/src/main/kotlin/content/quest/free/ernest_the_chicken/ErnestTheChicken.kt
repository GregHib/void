package content.quest.free.ernest_the_chicken

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.obj.door.enterDoor
import content.entity.player.bank.bank
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.entity.proj.shoot
import content.quest.quest
import content.quest.questComplete
import content.quest.questJournal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile

internal const val ERNEST = "ernest_the_chicken"
private val PARTS = listOf("pressure_gauge", "rubber_tube", "oil_can")

class ErnestTheChicken : Script {
    init {
        timerStart("ernest_restore_watchdog") { 1 }
        timerTick("ernest_restore_watchdog") {
            val job: Job? = get("ernest_restore_job")
            if (job == null || !job.isActive) {
                -1
            } else {
                // Normal movement/dialogue dismissal can discard a suspension without cancelling it.
                if (suspension == null && dialogue == null) job.cancel()
                1
            }
        }
        playerDespawn { get<Job>("ernest_restore_job")?.cancel() }

        npcOperate("Talk-to", "veronica") {
            when (quest(ERNEST)) {
                "unstarted" -> {
                    npc<Neutral>("Can you please help me? I'm in a terrible spot of trouble.")
                    choice {
                        option<Neutral>("Aha, sounds like a quest. I'll help.") {
                            npc<Neutral>("Yes yes, I suppose it is a quest. My fiance Ernest and I came upon this house.")
                            npc<Neutral>("Seeing as we were a little lost Ernest decided to go in and ask for directions.")
                            npc<Neutral>("That was an hour ago. That house looks spooky, can you go and see if you can find him for me?")
                            player<Neutral>("Ok, I'll see what I can do.")
                            npc<Happy>("Thank you, thank you. I'm very grateful.")
                            set(ERNEST, "started")
                        }
                        option<Neutral>("No, I'm looking for something to kill.") {
                            npc<Neutral>("Oooh, you violent person you.")
                        }
                    }
                }
                "completed" -> npc<Happy>("Thank you for helping Ernest!")
                "parts" -> {
                    npc<Neutral>("Have you found my sweetheart yet?")
                    player<Neutral>("Yes, he's a chicken.")
                    npc<Neutral>("I know he's not exactly brave but I think you're being a bit harsh.")
                    player<Neutral>("No no, he's been turned into an actual chicken by a mad scientist.")
                    npc<Neutral>("Eeeeeek!")
                    npc<Neutral>("My poor darling, why must these things happen to us.")
                    player<Neutral>("Well I'm doing my best to turn him back.")
                    npc<Neutral>("Well be quick, I'm sure being a chicken can't be good for him.")
                }
                else -> {
                    npc<Neutral>("Have you found my sweetheart yet?")
                    player<Neutral>("No, not yet.")
                }
            }
        }
        objectOperate("Search", "fountain") { (target) ->
            if (target.tile != Tile(3087, 3334)) return@objectOperate
            if (!get("ernest_fountain_poisoned", false)) {
                player<Neutral>("There seems to be a pressure gauge in here...")
                message("Something in the water bites you.")
                damage(10)
                player<Neutral>("Ow!")
                player<Neutral>("... and a lot of piranhas! I can't get the gauge out.")
            } else if (ownsErnestPart("pressure_gauge")) {
                player<Neutral>("It's full of dead fish!")
            } else {
                player<Neutral>("There seems to be a pressure gauge in here...")
                player<Neutral>("... and a lot of dead fish.")
                if (inventory.add("pressure_gauge")) {
                    message("You get the pressure gauge from the fountain.")
                } else {
                    message("You don't have enough inventory space to take the pressure gauge.")
                }
            }
        }
        itemOnObjectOperate("poisoned_fish_food", "fountain") { (target) ->
            if (target.tile != Tile(3087, 3334)) return@itemOnObjectOperate
            if (inventory.remove("poisoned_fish_food")) {
                set("ernest_fountain_poisoned", true)
                message("You pour the poisoned fish food into the fountain.")
                message("The piranhas start eating the food...")
                message("... then die and float to the surface.")
            }
        }
        itemOnObjectOperate("fish_food", "fountain") { (target) ->
            if (target.tile != Tile(3087, 3334)) return@itemOnObjectOperate
            if (inventory.remove("fish_food")) {
                message("You pour the fish food into the fountain.")
                message("The piranhas start eating the food...")
                message("Now they seem hungrier than ever!")
            }
        }
        itemOnObjectOperate("*", "fountain") { (target, item) ->
            if (target.tile != Tile(3087, 3334) || item.id in setOf("fish_food", "poisoned_fish_food")) return@itemOnObjectOperate
            if (get("ernest_fountain_poisoned", false)) {
                if (item.def.contains("full")) player<Neutral>("I don't think I want to use that water any more.")
            } else {
                message("Something in the water bites you.")
                damage(10)
                player<Neutral>("Ow!")
            }
        }
        objectOperate("Search", "ernest_compost") { searchCompost() }
        itemOnObjectOperate("spade", "ernest_compost") { searchCompost() }
        itemOption("Dig", "spade") {
            if (tile.level == 0 && tile.x in 3083..3086 && tile.y in 3359..3362) searchCompost()
        }
        objectOperate("Open", "door_11_closed") { (target) ->
            if (target.tile != Tile(3107, 3368)) return@objectOperate
            openTubeDoor(target)
        }
        itemOnObjectOperate("grimy_key", "door_11_closed") { (target) ->
            if (target.tile == Tile(3107, 3368)) openTubeDoor(target)
        }
        takeable("rubber_tube") { item, telegrab ->
            if (item.tile != Tile(3112, 3368)) {
                item.id
            } else if (telegrab || !get("ernest_tube_unlocked", false)) {
                message("You need to unlock the room and collect the tube from inside.")
                null
            } else {
                item.id
            }
        }
        questJournalOpen(ERNEST) {
            val lines = when (quest(ERNEST)) {
                "completed" -> listOf("<str>I found the missing machine parts and helped restore Ernest.", "", "<red>QUEST COMPLETE!")
                "parts" -> buildList {
                    add("<navy>Professor Oddenstein turned Ernest into a chicken.")
                    add("<navy>I must find three parts to repair his machine:")
                    for (part in PARTS) add("${if (ownsErnestPart(part)) "<str>" else "<maroon>"}${part.replace('_', ' ')}")
                    add("<navy>The parts are hidden around Draynor Manor.")
                }
                "started" -> listOf("<navy>Veronica asked me to find Ernest inside Draynor Manor.", "<navy>I should speak to the professor on the top floor.")
                else -> listOf("<navy>I can start this quest by speaking to Veronica", "<navy>outside the gates of Draynor Manor.")
            }
            questJournal("Ernest the Chicken", lines)
        }
    }

    private suspend fun Player.searchCompost() {
        if (!inventory.contains("spade")) {
            message("You need a spade to search the compost heap properly.")
            return
        }
        if (inventory.contains("grimy_key") || bank.contains("grimy_key")) {
            message("You have already found the key.")
            return
        }
        message("You dig through the compost...")
        anim("ernest_dig")
        delay(2)
        if (inventory.add("grimy_key")) {
            message("... and find a small key.")
        } else {
            message("You don't have enough inventory space to take the key.")
        }
    }

    private suspend fun Player.openTubeDoor(target: GameObject) {
        if (!get("ernest_tube_unlocked", false)) {
            if (!inventory.contains("grimy_key")) {
                message("The door is locked.")
                return
            }
            set("ernest_tube_unlocked", true)
            message("You unlock the door with the grimy key.")
        }
        enterDoor(target)
    }
}

private fun Player.ownsErnestPart(id: String) = inventory.contains(id) || bank.contains(id)

/** Return false to retain the professor's ordinary and post-quest dialogue. */
suspend fun Player.oddensteinErnest(professor: NPC): Boolean {
    get<Job>("ernest_restore_job")?.cancel()
    when (quest(ERNEST)) {
        "started" -> {
            npc<Neutral>("Be careful in here, there's lots of dangerous equipment.")
            choice {
                option<Neutral>("I'm looking for a guy called Ernest.") {
                    npc<Neutral>("Ah Ernest, top notch bloke. He's helping me with my experiments.")
                    player<Neutral>("So you know where he is then?")
                    npc<Neutral>("He's that chicken over there.")
                    player<Neutral>("Ernest is a chicken...? Are you sure?")
                    npc<Neutral>("Oh, he isn't normally a chicken, or at least he wasn't until he helped me test my pouletmorph machine.")
                    npc<Neutral>("It was originally going to be called a transmutation machine. But after testing pouletmorph seems more appropriate.")
                    choice {
                        option<Neutral>("I'm glad Veronica didn't actually get engaged to a chicken.") {
                            npc<Neutral>("Who's Veronica?")
                            player<Neutral>("Ernest's fiancee. She probably doesn't want to marry a chicken.")
                            npc<Neutral>("Ooh I dunno. She could have free eggs for breakfast.")
                            player<Neutral>("I think you'd better change him back.")
                            explainErnestParts()
                        }
                        option<Neutral>("Change him back this instant!") { explainErnestParts() }
                    }
                }
                option<Neutral>("What does this machine do?") {
                    npc<Neutral>("Nothing at the moment... It's broken. It's meant to be a transmutation machine.")
                    npc<Neutral>("It has also spent time as a time travel machine, and a dramatic lightning generator, and a thing for generating monsters.")
                }
                option<Neutral>("Is this your house?") {
                    npc<Neutral>("No, I'm just one of the tenants. It belongs to the count who lives in the basement.")
                }
            }
            return true
        }
        "parts" -> return restoreErnest(professor)
        else -> return false
    }
}

private suspend fun Player.explainErnestParts() {
    npc<Neutral>("Umm... It's not so easy...")
    npc<Neutral>("My machine is broken, and the house gremlins have run off with some vital bits.")
    player<Neutral>("Well I can look for them.")
    npc<Neutral>("That would be a help. They'll be somewhere in the manor house or its grounds, the gremlins never get further than the entrance gate.")
    npc<Neutral>("I'm missing the pressure gauge and a rubber tube. They've also taken my oil can, which I'm going to need to get this thing started again.")
    set(ERNEST, "parts")
}

/** Report the parts actually carried, without taking a partial hand-in. */
private suspend fun Player.reportErnestParts(missing: List<String>) {
    val found = PARTS.filterNot { it in missing }.toSet()
    when {
        found.isEmpty() -> {
            player<Neutral>("I'm afraid I don't have any of them yet!")
            npc<Neutral>("I need a rubber tube, a pressure gauge and a can of oil. Then your friend can stop being a chicken.")
        }
        found.size == 1 -> {
            val part = found.single().replace('_', ' ')
            player<Neutral>("I've only got the $part so far.")
            val rest = when (found.single()) {
                "rubber_tube" -> "pressure gauge and oil can"
                "pressure_gauge" -> "oil can and rubber tube"
                else -> "pressure gauge and rubber tube"
            }
            npc<Neutral>("It's a good start, but I'll need my $rest too.")
            player<Neutral>("Ok, I'll go and look for them.")
        }
        else -> {
            val parts = when (missing.single()) {
                "oil_can" -> "rubber tube and the pressure gauge"
                "pressure_gauge" -> "oil can and the rubber tube"
                else -> "oil can and the pressure gauge"
            }
            player<Neutral>("I've got the $parts.")
            npc<Neutral>("That's good, but I still need my ${missing.single().replace('_', ' ')}.")
            player<Neutral>("Ok, I'll go and look for it.")
        }
    }
}

/** The shared quest chicken keeps its identity; only this player's model is overridden. */
private suspend fun Player.restoreErnest(professor: NPC): Boolean {
    val busy: Job? = professor.get("ernest_restore_job")
    if (busy?.isActive == true) {
        message("The professor is busy with the machine. Please wait a moment.")
        return true
    }
    npc<Neutral>("Have you found anything yet?")
    val missing = PARTS.filterNot { inventory.contains(it) }
    if (missing.isNotEmpty()) {
        reportErnestParts(missing)
        return true
    }
    player<Neutral>("I have everything!")
    npc<Neutral>("Give 'em here then.")
    val chicken = NPCs.findOrNull(professor.tile.regionLevel, "chicken_draynor") ?: run {
        message("Ernest isn't nearby. Please try again in a moment.")
        return true
    }
    val active: Job? = professor.get("ernest_restore_job")
    if (active?.isActive == true) {
        message("The professor is busy with the machine. Please wait a moment.")
        return true
    }
    val job = currentCoroutineContext().job
    val oldProfessorMode = professor.mode
    val oldProfessorDelay = professor.delay
    val oldChickenMode = chicken.mode
    var heldParts = false
    set("ernest_restore_job", job)
    professor["ernest_restore_job"] = job
    softTimers.start("ernest_restore_watchdog")
    try {
        if (!inventory.transaction { for (part in PARTS) remove(part) }) return true
        heldParts = true
        message("You give the rubber tube, a pressure gauge, and a can of oil to the professor.")
        npc<Neutral>("Let's get this fixed then.")
        val machine = Tile(3112, 3367, 2)
        val machineSide = Tile(3111, 3367, 2)
        professor.walkTo(machineSide, forceWalk = true)
        var attempts = 0
        while (professor.tile != machineSide && attempts++ < 12) delay(1, cancellable = true)
        if (professor.tile != machineSide) {
            message("The professor can't reach the machine. Please try again.")
            return true
        }
        professor.mode = EmptyMode
        professor.face(machine)
        professor.anim("take")
        professor.delay = 12
        delay(2, cancellable = true)
        message("Oddenstein starts up the machine.")
        message("The machine hums and shakes.")
        areaGfx("weaken_cast", machine, height = 80)
        val flight = machine.shoot("weaken", chicken, delay = 0, height = 40)
        delay(((flight + 29) / 30).coerceAtLeast(1), cancellable = true)
        chicken.gfx("weaken_impact")
        chicken.sound("weaken_impact")
        delay(1, cancellable = true)
        chicken.gfx("stun_long")
        delay(3, cancellable = true)
        chicken.clearGfx()
        viewport?.npcForm(chicken, NPCDefinitions.get("ernest_human").id)
        chicken.mode = Follow(chicken, this)
        attempts = 0
        while (chicken.tile.distanceTo(tile) > 1 && attempts++ < 12) delay(1, cancellable = true)
        chicken.mode = Face(chicken, this)
        chicken.face(this)
        npc<Happy>("ernest_human", "Thank you, ${if (male) "sir" else "m'lady"}. It was dreadfully irritating being a chicken. How can *cluck* I ever thank you?")
        player<Neutral>("Well a cash reward is always nice...")
        npc<Happy>("ernest_human", "Of course, of course.")
        if (quest(ERNEST) != "parts") return true
        if (!inventory.transaction {
                add("coins", 3000)
                add("egg_noted", 10)
                add("feather", 300)
            }
        ) {
            message("Make room for your reward, then talk to the professor again.")
            return true
        }
        heldParts = false
        set(ERNEST, "completed")
        inc("quest_points", 4)
        jingle("quest_complete_1")
        questComplete("Ernest the Chicken", "4 Quest Points", "3,000 coins", "10 noted eggs", "300 feathers", item = "feather")
    } catch (_: CancellationException) {
        // Routine dismissal/movement ends this quest interaction; never continue into other dialogue.
        return true
    } finally {
        if (heldParts) for (part in PARTS) addOrDrop(part)
        viewport?.npcForm(chicken, null)
        chicken.clearGfx()
        chicken.clearWatch()
        chicken.mode = oldChickenMode
        professor.mode = oldProfessorMode
        professor.delay = oldProfessorDelay
        if (get<Job>("ernest_restore_job") === job) clear("ernest_restore_job")
        if (professor.get<Job>("ernest_restore_job") === job) professor.clear("ernest_restore_job")
    }
    return true
}
