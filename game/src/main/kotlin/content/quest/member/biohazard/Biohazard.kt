package content.quest.member.biohazard

import content.entity.obj.door.Gate
import content.entity.obj.door.enterDoor
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.entity.proj.shoot
import content.quest.questCompleted
import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Biohazard : Script {

    init {
        questJournalOpen("biohazard") {
            val stage = questStage("biohazard")
            questJournal("Biohazard", if (stage == 0) notStartedJournal() else startedJournal(stage))
        }

        objectOperate("Investigate", "bio_watchtower_fence") {
            val mourner = NPCs.findOrNull(tile.regionLevel, "mourner_vial_4") ?: return@objectOperate
            talkWith(mourner)
            npc<Neutral>("Keep away civilian.")
            player<Angry>("What's it to you?")
            npc<Neutral>("This tower is here for your protection.")
        }

        itemOnObjectOperate("bird_feed", "bio_watchtower_fence") {
            throwBirdFeed()
        }

        itemOption("Open", "pigeon_cage_full") {
            releasePigeons()
        }

        objectOperate("Squeeze-through", "mourner_stew_fence") { (target) ->
            squeezeFence(target)
        }

        itemOnObjectOperate("rotten_apple", "mourner_stew_cauldron") {
            poisonStew()
        }

        itemOption("Eat", "rotten_apple") { (_, slot) ->
            if (!inventory.remove(slot, "rotten_apple")) {
                return@itemOption
            }
            message("You eat an apple...")
            player<Shock>("Yuck!")
            message("...but it's rotten so you spit it out.")
        }

        objectOperate("Open", "mourner_stew_door_closed") { (target) ->
            openStewDoor(target)
        }

        objectOperate("Open", "mourner_stew_door_up_closed") { (target) ->
            if (tile.x >= target.tile.x || equipped(EquipSlot.Chest).id == "doctors_gown") {
                enterDoor(target)
                return@objectOperate
            }
            message("The mourner is refusing to open the door.")
        }

        objectOperate("Open", "mourner_quarters_gate_left_closed,mourner_quarters_gate_right_closed") { (target) ->
            if (tile.x < target.tile.x && !inventory.contains("key_biohazard")) {
                message("The gate is locked.")
                sound("locked")
                delay(1)
                message("You need a key.")
                return@objectOperate
            }
            unlockGate(target)
        }

        itemOnObjectOperate("key_biohazard", "mourner_quarters_gate_left_closed,mourner_quarters_gate_right_closed") { (target) ->
            unlockGate(target)
        }

        objectOperate("Search", "mourner_crate_up") {
            searchDistillatorCrate()
        }

        objectOperate("Search", "crate_272,crate_273") {
            message("You search the crate but find nothing.")
        }

        teleportLand("*") {
            destroyPlagueSample()
        }

        npcOperate("Talk-to", "mourner_1") { (target) ->
            if (questStage("biohazard") !in FOOD_POISONED) {
                npc<Angry>("How did you get in here? This is a restricted area!")
                target.interactPlayer(this, "Attack")
                return@npcOperate
            }
            player<Neutral>("Hello.")
            npc<Sad>("Hello Doc, I feel terrible. I think it was the stew.")
            player<Neutral>("Be more careful with your ingredients next time.")
        }

        npcOperate("Talk-to", "mourner_3") {
            if (questStage("biohazard") !in FOOD_POISONED) {
                npc<Angry>("Who are you? Go away!")
                return@npcOperate
            }
            if (equipped(EquipSlot.Chest).id != "doctors_gown") {
                player<Neutral>("Hello there.")
                npc<Sad>("Oh dear oh dear. I feel terrible, I think it was the stew.")
                player<Neutral>("You should be more careful with your ingredients.")
                npc<Sad>("I need a doctor. The nurse's hut is to the south west. Go now and bring us a doctor, that's an order.")
                return@npcOperate
            }
            player<Neutral>("Hello.")
            npc<Sad>("Hello Doc, I feel terrible. I think it was the stew.")
            player<Neutral>("Be more careful with your ingredients next time.")
            npc<Sad>("There is one mourner, who's really sick, resting upstairs. You should see to him first.")
            player<Neutral>("Ok, I'll see what I can do.")
        }

        npcOperate("Talk-to", "mourner_2") { (target) ->
            sickMourner(target)
        }
    }

    private suspend fun Player.throwBirdFeed() {
        if (questStage("biohazard") != 2) {
            message("Nothing interesting happens.")
            return
        }
        if (!inventory.remove("bird_feed")) {
            return
        }
        set("biohazard", "birdfeed_thrown")
        message("You throw a handful of seeds onto the watchtower.")
        delay(2)
        message("The mourners do not seem to notice.")
    }

    private suspend fun Player.releasePigeons() {
        if (questStage("biohazard") != 3 || tile.x !in TOWER_X || tile.y !in TOWER_Y) {
            message("The pigeons don't want to leave.")
            return
        }
        if (!inventory.replace("pigeon_cage_full", "pigeon_cage_empty")) {
            return
        }
        set("biohazard", "pigeons_released")
        for (tile in PIGEON_TILES) {
            shoot("biopigeon_launch", tile, delay = 5, flightTime = 126, height = 16, endHeight = 125, curve = 20, offset = 10)
        }
        message("The pigeons fly towards the watchtower.")
        delay(2)
        message("The mourners are frantically trying to scare the pigeons away.")
    }

    private suspend fun Player.squeezeFence(fence: GameObject) {
        val enter = tile.x <= fence.tile.x
        val direction = if (enter) Direction.EAST else Direction.WEST
        walkToDelay(Tile(if (enter) FENCE_WEST else FENCE_EAST, fence.tile.y))
        face(direction)
        message("You squeeze through the fence.")
        anim("regicide_tightfit")
        exactMoveDelay(Tile(if (enter) FENCE_EAST else FENCE_WEST, fence.tile.y), delay = 76, direction = direction)
    }

    private suspend fun Player.poisonStew() {
        if (!inventory.remove("rotten_apple")) {
            return
        }
        message("You place the rotten apple in the pot...")
        delay(4)
        if (questStage("biohazard") == 5) {
            message("and it quickly dissolves into the stew.")
            set("biohazard", "poisoned_stew")
            delay(4)
        }
        message("That wasn't very nice.")
    }

    private suspend fun Player.openStewDoor(door: GameObject) {
        val southDoor = door.tile.y == STEW_DOOR_SOUTH_Y
        val enter = if (southDoor) tile.y <= door.tile.y else tile.y >= door.tile.y
        if (!enter) {
            enterDoor(door)
            return
        }
        if (questStage("biohazard") !in FOOD_POISONED) {
            statement("The door is locked. You can hear the mourners eating... You need to distract them from their stew.")
            return
        }
        if (equipped(EquipSlot.Chest).id == "doctors_gown") {
            npc<Happy>("mourner_armed_vis", "In you go doc.")
            enterDoor(door)
            return
        }
        npc<Neutral>("mourner_armed_vis", "Stay away from there.")
        player<Quiz>("Why?")
        npc<Neutral>("mourner_armed_vis", "Several mourners are ill with food poisoning, we're waiting for a doctor.")
    }

    private fun Player.destroyPlagueSample() {
        val amount = inventory.count("plague_sample")
        if (amount <= 0) {
            return
        }
        inventory.removeToLimit("plague_sample", amount)
        message("The Plague Sample is fragile and is destroyed in the crossing.")
    }

    private suspend fun Player.unlockGate(gate: GameObject) {
        val entering = tile.x < gate.tile.x
        val row = if (tile.y == QUARTERS_GATE_LEFT.y || tile.y == QUARTERS_GATE_RIGHT.y) tile.y else gate.tile.y
        val near = Tile(if (entering) QUARTERS_GATE_LEFT.x - 1 else QUARTERS_GATE_LEFT.x, row, 1)
        val far = Tile(if (entering) QUARTERS_GATE_LEFT.x else QUARTERS_GATE_LEFT.x - 1, row, 1)
        if (tile != near) {
            walkOverDelay(near)
        }
        if (entering && inventory.contains("key_biohazard")) {
            message("The key fits the gate.")
            delay(1)
        }
        val left = GameObjects.findOrNull(QUARTERS_GATE_LEFT, "mourner_quarters_gate_left_closed")
        val right = GameObjects.findOrNull(QUARTERS_GATE_RIGHT, "mourner_quarters_gate_right_closed")
        if (left == null || right == null) {
            walkOverDelay(far)
            return
        }
        Gate.replaceTogether(
            this, left, right,
            flip = false,
            ticks = GATE_TICKS,
            collision = false,
            current = "_closed",
            next = "_opened",
            objRotation = 3,
            hingeTileRotation = 1,
        )
        walkOverDelay(far)
    }

    private suspend fun Player.searchDistillatorCrate() {
        message("You search the crate...")
        delay(4)
        if (ownsItem("distillator") || questStage("biohazard") < 6) {
            message("It's empty.")
            return
        }
        if (questStage("biohazard") == 6) {
            set("biohazard", "found_distillator")
        }
        addOrDrop("distillator")
        message("and find Elena's distillator.")
    }

    private suspend fun Player.sickMourner(mourner: NPC) {
        if (questStage("biohazard") != 6) {
            message("The mourner is sick.")
            delay(4)
            message("He doesn't feel like talking.")
            return
        }
        if (equipped(EquipSlot.Chest).id != "doctors_gown") {
            message("The mourner doesn't feel like talking.")
            return
        }
        player<Neutral>("Hello there.")
        npc<Sad>("You're here at last! I don't know what I've eaten but I feel like I'm on death's door.")
        player<Neutral>("Hmm... interesting, sounds like food poisoning.")
        npc<Sad>("Yes, I'd figured that out already. What can you give me to help.")
        choice {
            holdBreath(mourner)
            prayForYou(mourner)
            fatal(mourner)
        }
    }

    private fun ChoiceOption.holdBreath(mourner: NPC): Unit = option<Neutral>("Just hold your breath and count to ten.") {
        npc<Quiz>("What? How will that help? What kind of doctor are you?")
        player<Shifty>("Erm... I'm new, I just started.")
        unmasked(mourner)
    }

    private fun ChoiceOption.prayForYou(mourner: NPC): Unit = option<Neutral>("The best I can do is pray for you.") {
        npc<Sad>("Pray for me? You're not a doctor... You're an imposter!")
        mourner.interactPlayer(this, "Attack")
    }

    private fun ChoiceOption.fatal(mourner: NPC): Unit = option<Neutral>("There's nothing I can do, it's fatal.") {
        npc<Scared>("No, I'm too young to die! I've never even had a girlfriend.")
        player<Neutral>("That's life for you.")
        npc<Quiz>("Wait a minute, where's your equipment?")
        player<Shifty>("It's erm... at home.")
        unmasked(mourner)
    }

    private suspend fun Player.unmasked(mourner: NPC) {
        npc<Angry>("You're no doctor!")
        mourner.interactPlayer(this, "Attack")
    }

    private fun Player.notStartedJournal(): List<String> {
        val plague = questCompleted("plague_city")
        return listOf(
            "<navy>I can start this quest by speaking to <maroon>Elena <navy>who is in <maroon>East",
            "<maroon>Ardougne<navy>.",
            "",
            "<navy>Requirements.",
            if (plague) "<str>I need to complete Plague City before I can attempt this" else "<navy>I need to complete <maroon>Plague City <navy>before I can attempt this",
            if (plague) "<str>quest." else "<navy>quest.",
        )
    }

    private fun Player.startedJournal(stage: Int): List<String> {
        val lines = mutableListOf<String>()
        lines += "<str>I've spoken to Elena, the Mourners stole her distillator."
        lines += ""
        if (stage < 2) {
            lines += "<navy>I need to talk to <maroon>Jerico <navy>about getting over the wall and into"
            lines += "<maroon>West Ardougne<navy>."
            return lines
        }
        lines += "<str>I've spoken to Jerico about getting into West Ardougne."
        if (stage < 5) {
            lines += ""
            lines += watchtowerJournal(stage)
            return lines
        }
        lines += "<str>I've crossed the wall into West Ardougne."
        lines += "<str>Omart and Kilron will stay to help me out again."
        lines += ""
        if (stage < 7) {
            lines += "<navy>Somewhere in the city is <maroon>Elena's distillator <navy>- I must find it"
            lines += "<navy>and return it to her."
            if (stage == 6) {
                lines += ""
                lines += "<navy>I have rather unkindly poisoned the mourners' stew by"
                lines += "<navy>putting rotten apples into it!"
                lines += "<navy>At least the mourners aren't very nice people, so I don't"
                lines += "<navy>feel bad about this."
            }
            return lines
        }
        if (stage == 7) {
            lines += "<str>I managed to find Elena's distillator."
            lines += ""
            if (carriesItem("distillator")) {
                lines += "<navy>Now I can return the <maroon>distillator <navy>to <maroon>Elena<navy>."
            } else {
                lines += "<navy>I seem to have mislaid the <maroon>distillator"
                lines += "<navy>I had better try to find another, or <maroon>Elena <navy>will be upset."
            }
            return lines
        }
        if (stage == 10) {
            lines += "<str>I've found Elena's distillator and returned it to her."
            lines += ""
            lines += deliveryJournal()
            return lines
        }
        lines += "<str>Elena gave me some chemicals to take to Guidor."
        lines += ""
        if (stage < 14) {
            lines += "<navy>The Varrock guards are looking out for someone carrying"
            lines += "<navy>suspicious materials. I must smuggle the items to <maroon>Guidor"
            lines += "<navy>secretly."
            return lines
        }
        lines += "<str>I've given all the items to Guidor."
        lines += ""
        if (stage == 14) {
            lines += "<maroon>Guidor's <navy>findings were very interesting."
            lines += "<navy>Apparently there is no <maroon>Plague!"
            lines += "<navy>I should see what Elena thinks of this."
            return lines
        }
        lines += "<str>I've told Elena of Guidor's findings, she was horrified!"
        lines += ""
        if (stage == 15) {
            lines += "<navy>I need to confront the <maroon>King of East Ardougne<navy>."
            return lines
        }
        lines += "<str>I've spoken to King Lathas, he admits the Plague was fake!"
        lines += "<str>I have been given permission to use the Royal Training"
        lines += "<str>Grounds which are located in North West of Ardougne."
        lines += "<col=ff0000>QUEST COMPLETE!"
        return lines
    }

    private fun watchtowerJournal(stage: Int): List<String> = when (stage) {
        2 -> listOf(
            "<maroon>Omart <navy>will be able to get me over the wall.",
            "<navy>He's waiting at the <maroon>South <navy>end of the wall.",
        )
        3 -> listOf("<navy>I've chucked some birdfeed onto the Watch Tower.")
        else -> listOf(
            "<navy>I've chucked some birdfeed onto the Watch Tower.",
            "<navy>The Watch Tower is now surrounded by flapping pigeons!",
            "<navy>Maybe I can sneak over the wall while the mourners are",
            "<navy>distracted.",
        )
    }

    private fun Player.deliveryJournal(): List<String> = listOf(
        "<navy>Elena's asked me to take the following items to <maroon>Guidor <navy>who",
        "<navy>lives in <maroon>Varrock<navy>.",
        if (carriesItem("plague_sample")) "<str>I have the Plague Sample." else "<maroon>Plague Sample <navy>from <maroon>Elena<navy>.",
        if (carriesItem("liquid_honey")) "<str>I have the Liquid Honey." else "<maroon>Liquid Honey",
        if (carriesItem("sulphuric_broline")) "<str>I have the Sulphuric Broline." else "<maroon>Sulphuric Broline",
        if (carriesItem("ethenea")) "<str>I have the Ethenea." else "<maroon>Ethenea",
        if (carriesItem("touch_paper")) "<str>I have the Touch Paper." else "<maroon>Touch Paper <navy>from the <maroon>Chemist <navy>in <maroon>Rimmington<navy>.",
    )

    private companion object {
        val FOOD_POISONED = 6..7
        val TOWER_X = 2559..2563
        val TOWER_Y = 3300..3307

        val QUARTERS_GATE_LEFT = Tile(2552, 3325, 1)
        val QUARTERS_GATE_RIGHT = Tile(2552, 3326, 1)
        const val GATE_TICKS = 3
        val PIGEON_TILES = listOf(Tile(2560, 3304), Tile(2561, 3303), Tile(2561, 3305))
        const val FENCE_WEST = 2541
        const val FENCE_EAST = 2542
        const val STEW_DOOR_SOUTH_Y = 3320
    }
}
