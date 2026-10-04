package content.quest.member.waterfall_quest

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.obj.door.Door
import content.entity.player.AdventurersLogs
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.modal.book.openBook
import content.quest.quest
import content.quest.questComplete
import content.quest.questJournal
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class WaterfallQuest : Script {
    init {
        npcOperate("Talk-to", "almera_baxtorian_falls") {
            almeraDialogue()
        }
        npcApproach("Talk-to", "hudon_baxtorian_falls") {
            // Hudon is across the river from the island where Almera's raft crashes.
            approachRange(5)
            if (tile.x !in 2510..2513 || tile.y !in 3476..3482) {
                message("Hudon can't hear you because of the noise of the waterfall. Perhaps the acoustics would be better from that island?")
                return@npcApproach
            }
            if (questStage(QUEST) >= 2) {
                hudonReturnDialogue()
                return@npcApproach
            }
            player<Idle>("Hello son, are you okay? You need help?")
            npc<Idle>("It looks like you need the help.")
            player<Idle>("Your mum sent me to find you.")
            npc<Idle>("Don't play nice with me, I know you're looking for the treasure too.")
            player<Idle>("Where is this treasure you talk of?")
            npc<Idle>("Just because I'm small doesn't mean I'm dumb! If I told you, you would take it all for yourself.")
            player<Idle>("Maybe I could help.")
            npc<Idle>("I'm fine alone.")
            player<Idle>("Hmm... I wonder what this treasure is.")
            if (quest(QUEST) != "unstarted") advance("found_hudon")
        }
        npcOperate("Talk-to", "hadley_baxtorian_falls") { hadleyDialogue() }
        npcOperate("Talk-to", "gerald_baxtorian_falls") { geraldDialogue() }
        npcOperate("Talk-to", "golrie") { golrieDialogue() }
        objectOperate("Search", "waterfall_bookcase,waterfall_bookcase_2") { (target) ->
            if (target.tile.level == 1 && target.tile.x in 2516..2521 && target.tile.y in 3423..3431) {
                message("You search the bookcase.")
                delay(2)
                if (questStage(QUEST) >= 2 && !carriesItem("book_on_baxtorian") && giveUnique("book_on_baxtorian")) {
                    message("You find a book named 'Book on Baxtorian' on the bookcase.")
                }
            }
        }
        itemOption("Read", "book_on_baxtorian") {
            openBook("book_on_baxtorian")
            if (questStage(QUEST) >= 2) {
                advance("read_book")
                if (carriesItem("glarials_pebble")) advance("found_pebble")
            }
        }
        objectOperate("Search", "crate_10") {
            if (!carriesItem("a_key") && giveUnique("a_key")) message("You find a key in the crate.")
        }
        objectOperate("Open", "gate_33_closed") { (target) -> freeGolrie(target) }
        itemOnObjectOperate("a_key", "gate_33_closed") { (target) -> freeGolrie(target) }

        objectOperate("Board", "waterfall_raft") {
            if (quest(QUEST) == "unstarted") {
                message("You're not sure if the raft is safe to use. Best to leave it alone.")
            } else {
                message("You board the small raft and push off down stream...")
                delay(2)
                tele(2511, 3481)
                message("...you crash into a small island.")
            }
        }
        objectApproach("Swim", "waterfall_river") { (target) ->
            if (target.tile.x in 2511..2513 && target.tile.y in 3475..3476) swimDownstream()
        }
        itemOnObjectApproach("rope", "waterfall_rock*") { (target) ->
            walkOverDelay(Tile(2512, 3476))
            face(target.tile)
            anim("waterfall_rope_throw")
            // The throw holds a rope during the wind-up; its flying effect starts at release.
            gfx("waterfall_rope_throw", delay = 65, height = 0)
            delay(3)
            GameObjects.replace(target, "waterfall_rock_rope", ticks = 10)
            for (y in 3469..3475) {
                GameObjects.add("waterfall_crossing_rope", Tile(2512, y), ticks = 10, collision = false)
            }
            delay()
            message("You throw the rope around the rock and pull yourself across.")
            anim("waterfall_rope_pull", override = true)
            // The client sequence lasts 72 frames; keep the movement inside that animation.
            exactMove(Tile(2512, 3469), delay = 72, direction = Direction.SOUTH)
            // Round up: two server ticks would clear the pull before the client finishes moving.
            delay(3)
            clearAnim()
            walkOverDelay(Tile(2513, 3468))
        }
        objectApproach("Swim to", "waterfall_rock*") { swimDownstream() }
        itemOnObjectApproach("rope", "dead_tree_13") {
            message("You tie the rope to the tree and climb down to the ledge below.")
            tele(2511, 3463)
        }
        objectApproach("Climb", "dead_tree_13") {
            message("You try to use the tree to climb down...")
            delay(2)
            message("...but you slip and fall into the water.")
            washOut()
        }
        objectOperate("Get in", "waterfall_barrel") {
            message("You climb into the barrel and push off the edge. You are carried down the river.")
            tele(2527, 3413)
        }
        objectOperate("Enter", "waterfall_entrance*") {
            if (questStage(QUEST) < 4 || !equipment.contains("glarials_amulet")) {
                message("You try to open the door, but the ledge is suddenly flooded with water...")
                delay(2)
                message("...you are pushed over the waterfall and into the river.")
                washOut()
            } else {
                advance("entered_dungeon")
                tele(2575, 9861)
                message("You enter the waterfall.")
            }
        }
        objectOperate("Open", "waterfall_exit") { (target) ->
            if (target.tile == Tile(2574, 9860)) {
                tele(2511, 3463)
                message("You exit the dungeon.")
            }
        }
        objectOperate("Read", "glarials_tombstone") {
            open("glarials_tombstone_inscription")
            val lines = listOf(
                "Here lies Glarial, wife of Baxtorian,",
                "true friend of nature in life and death.",
                "",
                "May she now rest knowing",
                "only visitors with peaceful intent can enter.",
            )
            repeat(12) { index ->
                interfaces.sendText("glarials_tombstone_inscription", "line$index", lines.getOrElse(index) { "" })
            }
        }
        interfaceOption("Close", "glarials_tombstone_inscription:close") {
            close("glarials_tombstone_inscription")
        }
        itemOnObjectOperate("glarials_pebble", "glarials_tombstone") {
            if (canEnterTomb()) {
                message("You place the pebble in the gravestone's small indent.")
                delay(2)
                message("You hear a loud creak.")
                delay(2)
                message("The stone slab slides back revealing a ladder down.")
                delay(2)
                tele(2555, 9844)
                message("You climb down to an underground passage.")
            } else {
                message("You place the pebble in the gravestone's small indent but nothing happens.")
            }
        }
        objectOperate("Open", "glarials_chest_closed") {
            if (giveUnique("glarials_amulet")) {
                advance("found_amulet")
                message("You search the chest and find a small amulet.")
            }
        }
        objectOperate("Search", "glarials_chest_opened") {
            if (giveUnique("glarials_amulet")) {
                advance("found_amulet")
                message("You search the chest and find a small amulet.")
            }
        }
        objectOperate("Search", "glarials_tomb") {
            message("You search the coffin.")
            delay(2)
            if (giveUnique("glarials_urn")) {
                advance("found_urn")
                message("Inside you find an urn full of ashes.")
                message("You take the urn and close the coffin.")
            }
        }
        objectOperate("Climb-up", "tithe_roof_g") { (target) ->
            // Object 1757 shares its definition with Fenkenstrain's cave ladders.
            // Its existing specific handler takes precedence over generic object teleports.
            if (target.tile == Tile(2556, 9844)) tele(2559, 3444)
        }
        objectOperate("Search", "crate_11") {
            message("You search the crate")
            delay(2)
            if (giveUnique("a_key_waterfall_dungeon")) message("and find a large key")
        }
        objectOperate("Open", "door_29_closed") { (target) -> enterChamberDoor(target) }
        itemOnObjectOperate("a_key_waterfall_dungeon", "door_29_closed") { (target) -> enterChamberDoor(target) }
        itemOnObjectOperate("air_rune,water_rune,earth_rune", "pillar") { interaction ->
            if (placeRune(interaction.target.tile, interaction.item.id)) {
                message("You place the rune on the stand.")
                areaGfx("smokepuff_large", interaction.target.tile, height = 100)
                delay(2)
                message("The rune stone disappears in a puff of smoke.")
            }
        }
        itemOnObjectOperate("glarials_amulet", "waterfall_glarial_statue") {
            if (quest(QUEST) == "completed") return@itemOnObjectOperate
            if (questStage(QUEST) < 4 || get("waterfall_pillars", 0) != ALL_RUNES) {
                floodChamber("You go to place the amulet around the neck of the statue. However, water floods into the room as you do...")
            } else if (quest(QUEST) == "restored_amulet" || inventory.remove("glarials_amulet")) {
                advance("restored_amulet")
                message("You place the amulet around the neck of the statue.")
                delay(2)
                message("You hear a loud rumble from beneath...")
                delay(2)
                message("The ground raises up before you!")
                // The cache contains a second chamber with the raised floor, offset east and south.
                tele(2603, 9914)
            }
        }
        // The chalice is above the player. Approach allows interaction with it across the ledge.
        itemOnObjectApproach("glarials_urn", "waterfall_chalice") {
            if (quest(QUEST) != "restored_amulet") {
                if (quest(QUEST) != "completed") floodChamber("You go to take the treasure from the chalice. However, water floods into the room as you do...")
            } else if (inventory.spaces < 5) {
                message("You do not have enough space for the reward, you will need 5 free slots to accept it.")
            } else {
                message("You carefully pour the ashes into the chalice")
                delay(2)
                message("as you remove the Treasure of Baxtorian.")
                delay(2)
                message("The chalice remains standing.")
                delay(2)
                if (completeQuest()) {
                    message("Inside you find a mithril case")
                    message("containing 40 seeds,")
                    message("two diamonds and two gold bars.")
                    message("Congratulations! Quest complete!")
                }
            }
        }
        objectApproach("Take treasure", "waterfall_chalice") {
            if (quest(QUEST) == "completed") message("The chalice only contains some old ashes.") else floodChamber("You go to take the treasure from the chalice. However, water floods into the room as you do...")
        }
        questJournalOpen(QUEST) {
            val instruction = when (quest(QUEST)) {
                "unstarted" -> "Speak to Almera north-east of Baxtorian Falls."
                "started" -> "Board Almera's raft and find Hudon."
                "found_hudon" -> "Visit Hadley and search the bookcases upstairs."
                "read_book" -> "Find Golrie in the Tree Gnome Village dungeon."
                "found_pebble" -> "Use Glarial's pebble on her tombstone without weapons, armour or runes."
                "found_amulet" -> "Search Glarial's tomb for the urn containing her ashes."
                "found_urn", "entered_dungeon" -> "Enter the waterfall with Glarial's amulet, a rope and her urn."
                "restored_amulet" -> "Use Glarial's urn on the Chalice of Eternity."
                else -> "<red>QUEST COMPLETE!"
            }
            val lines = when (quest(QUEST)) {
                "completed" -> listOf("<str>I found Hudon and investigated Baxtorian's history.", "<str>I returned Glarial's ashes to the Chalice of Eternity.", "", "<red>QUEST COMPLETE!")
                "found_urn", "entered_dungeon" -> listOf("<navy>$instruction", "", "<navy>Search the eastern passage for the key to the locked doors.", "<navy>Place an air, water and earth rune on each of the six pillars.", "<navy>Then return Glarial's amulet to her statue.", "<navy>Bring five free inventory spaces for the treasure.")
                else -> listOf("<navy>$instruction")
            }
            questJournal("Waterfall Quest", lines)
        }
    }

    private suspend fun Player.almeraDialogue() {
        when (if (questStage(QUEST) == 2 && inventory.contains("book_on_baxtorian")) 3 else questStage(QUEST)) {
            0 -> startDialogue()
            1 -> {
                player<Idle>("Hello Almera.")
                npc<Idle>("Hello brave adventurer, have you seen my boy yet?")
                player<Idle>("I'm afraid not, but I'm sure he hasn't gone far.")
                npc<Idle>("I do hope so, you can't be too careful these days.")
            }
            2 -> {
                player<Idle>("Hello again.")
                npc<Idle>("Well hello, you're still around then.")
                player<Idle>("I saw Hudon by the river but he refused to come back with me.")
                npc<Idle>("Yes he told me, the foolish lad came in drenched to the bone, he had fallen into the waterfall, lucky he wasn't killed! Now he can spend the rest of the summer in his room.")
                player<Idle>("Any ideas what I could do while I'm here?")
                npc<Idle>("Why don't you visit the tourist centre south of the waterfall?")
            }
            3, 4 -> {
                player<Idle>("Hello again Almera.")
                npc<Idle>("Well hello again brave adventurer, are you enjoying the tranquil scenery of these parts?")
                player<Idle>("Yes, very relaxing.")
                npc<Idle>("Well I'm glad to hear it The authorities wanted to dig up this whole area for a mine, but the few locals who lived here wouldn't budge and they gave up.")
                player<Idle>("Good for you.")
                npc<Idle>("Good for all of us!")
            }
            else -> {
                player<Idle>("Hello Almera.")
                npc<Idle>("Hello adventurer, how's your treasure hunt going?")
                player<Idle>("Oh, I'm just sight seeing.")
                npc<Idle>("No adventurer stays here this long just to sight see. But your business is yours alone, if you need to use the raft go ahead. But please try not crash it this time!")
                player<Idle>("Thanks Almera.")
            }
        }
    }

    private suspend fun Player.hudonReturnDialogue() {
        when (questStage(QUEST)) {
            2 -> {
                player<Idle>("So you're still there.")
                npc<Idle>("I'll find that treasure soon, just you wait and see.")
            }
            in 3..6 -> {
                player<Idle>("Hello Hudon.")
                npc<Idle>("Oh it's you, trying to find my treasure again are you?")
                player<Idle>("I didn't know it belonged to you.")
                npc<Idle>("It will do when I find it. I just need to get into this blasted waterfall I've been washed downstream three times already.")
            }
            7, 8 -> {
                player<Idle>("Hello again.")
                npc<Idle>("Not you still, why don't you give up?")
                player<Idle>("And miss all the fun!")
                npc<Idle>("You do understand that anything you find you have to share with me.")
                player<Idle>("Why's that?")
                npc<Idle>("Because I told you about the treasure.")
                player<Idle>("Well, I wouldn't count on it.")
                npc<Idle>("That's not fair.")
                player<Idle>("Neither is life kid.")
            }
            else -> {
                player<Idle>("How are you doing, Hudon?")
                npc<Idle>("No luck yet I'm afraid.")
                player<Idle>("Me neither. but I don't give up easily.")
            }
        }
    }

    private suspend fun Player.geraldDialogue() {
        if (questStage(QUEST) == 2) {
            npc<Idle>("Blimey! Where did you come from? Not another one of those treasure hunters are you?")
            player<Idle>("Treasure hunters?")
            npc<Idle>("They say there's treasure hidden within the waterfall, left behind by the old elven king. Not that anyone's ever found anything.")
            player<Idle>("Interesting, is there somewhere I can learn more about this?")
            npc<Idle>("You could ask Hadley the tourist guide. He'll be in this building just here.")
            return
        }
        player<Idle>("Hello.")
        if (questStage(QUEST) <= 1) {
            npc<Idle>("Hello there.")
            player<Idle>("Have you seen a small boy?")
            npc<Idle>("Nope, plenty of small fish though.")
        } else {
            npc<Idle>("Hello traveller. Are you here to fish or to hunt for treasure?")
            player<Idle>("Why do you say that?")
            npc<Idle>("Adventurers pass through here every week, they never find anything though.")
            player<Idle>("What treasure are they looking for?")
            npc<Idle>("They say there's treasure hidden within the waterfall, left behind by the old elven king. Not that anyone's ever found anything.")
            player<Idle>("Interesting, is there somewhere I can learn more about this?")
            npc<Idle>("You could ask Hadley the tourist guide. He'll be in this building just here.")
        }
    }

    private suspend fun Player.startDialogue() {
        player<Idle>("Hello.")
        npc<Idle>("Ah, hello there. Nice to see an outsider for a change, are you busy? I have a problem.")
        when (choice(listOf("I'm afraid I'm in a rush.", "How can I help?"))) {
            2 -> {
                npc<Idle>("It's my son Hudon, he's always getting into trouble, the boy's convinced there's hidden treasure in the river and I'm a bit worried about his safety, the poor lad can't even swim.")
                player<Idle>("I could go and take a look for you if you like?")
                npc<Idle>("Would you? You are kind. You can use the small raft out back if you wish, do be careful, the current down stream is very strong.")
                advance("started")
                return
            }
            1 -> {
                player<Idle>("I'm afraid I'm in a rush.")
                npc<Idle>("Oh okay, never mind.")
            }
            else -> return
        }
    }

    private suspend fun Player.hadleyDialogue() {
        if (questStage(QUEST) >= 3) {
            player<Idle>("Hello there.")
            npc<Idle>("I hope you're enjoying your stay, there should be lots of useful information in that book: places to go, people to see.")
        } else if (inventory.contains("book_on_baxtorian")) {
            player<Idle>("Hello there.")
            npc<Idle>("I hope you're enjoying your stay, there should be lots of useful infomation in that book you've got. Make sure you give it a read.")
        } else {
            player<Idle>("Hello there.")
            npc<Idle>("Are you on holiday? If so you've come to the right place. I'm Hadley the tourist guide, anything you need to know just ask me we have some of the most unspoilt wildlife and scenery in RuneScape.")
            npc<Idle>("People come from miles around to fish in the clear lakes or to wander the beautiful hillsides.")
            player<Idle>("It is quite pretty.")
            npc<Idle>("Surely pretty is an understatement kind ${if (male) "Sir" else "Lady"}. Beautiful, amazing or possibly life-changing would be more suitable wording. Have you seen the Baxtorian waterfall? Named after the elf king who was buried beneath.")
        }
        while (true) {
            when (choice(listOf("Can you tell me what happened to the elf king?", "Where else is worth visiting around here?", "Is there treasure under the waterfall?", "Thanks then, goodbye."))) {
                1 -> {
                    player<Idle>("Can you tell me what happened to the elf king?")
                    npc<Idle>("There are many myths about Baxtorian. One popular story is that after defending his kingdom against the invading dark forces from the west, Baxtorian returned to find his wife Glarial had been captured by the")
                    npc<Idle>("enemy!")
                    npc<Idle>("This destroyed Baxtorian, after years of searching he became a recluse. In the secret home had had made for Glarial under the waterfall, he never came out and it is told that only Glarial could enter.")
                    player<Idle>("What happened to him?")
                    npc<Idle>("Oh, I don't know. I believe we have some pages on him upstairs in our archives. If you wish to look at them please be careful, they're all pretty delicate.")
                }
                2 -> {
                    player<Idle>("Where else is worth visiting around here?")
                    npc<Idle>("There is a lovely spot for a picnic on the hill to the north east, there's a monument to the elven queen Glarial. It really is quite pretty.")
                    player<Idle>("Who was Glarial?")
                    npc<Idle>("Baxtorian's wife, the only other person who could also enter the waterfall apart from him. She was queen when this land was inhabited by elven kind.")
                    npc<Idle>("Glarial was kidnapped while Baxtorian was away, but they eventually recovered her body and brought her home to rest.")
                    player<Idle>("That's sad.")
                    npc<Idle>("True, I believe we have a book on Baxtorian and Glarial upstairs if you want to learn more.")
                }
                3 -> {
                    player<Idle>("Is there treasure under the waterfall?")
                    npc<Idle>("Ha ha... Another treasure hunter. Well if there is no one's been able to get to it. They've been searching that river for decades, all to no avail.")
                }
                4 -> {
                    player<Idle>("Thanks then, goodbye.")
                    npc<Idle>("Enjoy your visit.")
                    return
                }
                else -> return
            }
        }
    }

    private suspend fun Player.golrieDialogue() {
        if (get("path_of_glouphrie", "unstarted") == "completed") {
            golrieAfterGlouphrie()
            return
        }
        if (questStage(QUEST) == 0) {
            npc<Idle>("What are you doing down here? Leave before you get yourself into trouble.")
            return
        }
        if (!get("waterfall_golrie_freed", false)) {
            if (!inventory.contains("a_key")) {
                lockedGolrieDialogue()
            }
            return
        }
        if (carriesItem("glarials_pebble")) {
            player<Idle>("Hello, Golrie.")
            npc<Idle>("Hello again.")
            player<Idle>("Any luck getting out?")
            npc<Idle>("Not yet, but don't worry. I'm sure I'll work something out. I just need some time to think.")
            player<Idle>("Well good luck.")
            return
        }
        if (get("waterfall_golrie_key_given", false) || get(QUEST, "unstarted") == "completed") {
            player<Idle>("Hello, Golrie.")
            npc<Idle>("Hello again.")
            player<Idle>("Do you mind if I have another look through this stuff?")
            npc<Idle>("No, of course not.")
            if (inventory.spaces == 0) {
                message("You look amongst the junk on the floor and find Glarial's pebble but you don't have enough room to take it.")
                return
            }
            if (giveUnique("glarials_pebble")) {
                message("You look amongst the junk on the floor and find Glarial's pebble.")
                if (questStage(QUEST) >= 3) advance("found_pebble")
            }
            return
        }
        if (!inventory.contains("a_key")) {
            lockedGolrieDialogue()
            return
        }
        if (questStage(QUEST) < 3) {
            player<Idle>("Who are you, and what are you doing here?")
            npc<Idle>("I'm Golrie, and this is my home. Thing is, as you can see, I've got a small problem. Those hob-gobs are all over the place trying to steal my family's heirlooms. I've been stuck here for ages!")
            player<Idle>("Do you need some help?")
            npc<Idle>("Oh don't worry, I'm sure I'll work something out. I just need some time to think.")
            player<Idle>("In that case, do you mind if I have a look around?")
        } else {
            player<Idle>("Hello, is your name Golrie?")
            npc<Idle>("That's me. I've been stuck in here for weeks, those goblins are trying to steal my family's heirlooms. My grandad gave me all sorts of old junk.")
            player<Idle>("Do you mind if I have a look?")
        }
        npc<Idle>("No, of course not.")
        message("You look amongst the junk on the floor.")
        delay(2)
        message("Mixed with the junk on the floor you find Glarial's pebble.")
        delay(2)
        player<Idle>("Could I take this old pebble?")
        npc<Idle>("Oh that, yes have it, it's just some old elven junk I believe.")
        if (!giveUnique("glarials_pebble")) return
        if (questStage(QUEST) >= 3) advance("found_pebble")
        if (inventory.remove("a_key")) {
            message("You give Golrie the key")
            set("waterfall_golrie_key_given", true)
            delay(2)
        }
        npc<Idle>("Thanks a lot for the key traveler. I think I'll wait in here until those goblins get bored and leave.")
        player<Idle>("OK... Take care Golrie.")
    }

    private suspend fun Player.golrieAfterGlouphrie() {
        player<Idle>("Hello, Golrie. How's it going?")
        npc<Idle>("Better now I'm on the right side of these bars.")
        var talking = true
        while (talking) {
            choice {
                if (!inventory.contains("cadarn_lineage")) {
                    option("Found anything interesting recently?") {
                        player<Idle>("Found anything interesting recently?")
                        npc<Idle>("It just so happens I have. Take a look at this book.")
                        if (giveUnique("cadarn_lineage")) {
                            message("Golrie gives you a book.")
                            player<Idle>("Thanks.")
                        }
                    }
                }
                if (!get("golrie_device_explained", false)) {
                    option("I managed to find that device.") {
                        player<Idle>("I managed to find that device.")
                        npc<Idle>("Wonderful! Though you know, I don't think you ever actually mentioned what the device does.")
                        player<Idle>("It counteracts illusion magic in the surrounding area. This one should cover the whole of the Tree Gnome Village. It might be useful for someone to remember that in case it's needed again.")
                        npc<Idle>("Oh, well I'll be sure to keep that in mind.")
                        set("golrie_device_explained", true)
                        golrieStoreroom()
                        talking = false
                    }
                } else {
                    option("I have a question about your storeroom.") {
                        player<Idle>("I have a question about your storeroom.")
                        golrieStoreroom()
                        talking = false
                    }
                }
                if (!inventory.contains("glarials_pebble")) {
                    option("I lost that pebble.") {
                        player<Idle>("I lost that pebble.")
                        npc<Idle>("Not to worry. It turned up here again.")
                        if (giveUnique("glarials_pebble")) {
                            message("Golrie gives you the pebble.")
                            player<Idle>("Thanks.")
                        }
                    }
                }
                if (!inventory.contains("a_key")) {
                    option("Can I use your key?") {
                        player<Idle>("Can I use your key?")
                        npc<Idle>("If you wanna go in there with them hob-gobs, that's your business.")
                        if (giveUnique("a_key")) {
                            message("Golrie gives you a key.")
                            player<Idle>("Thanks.")
                        }
                    }
                }
                option("I'll leave you to it.") {
                    player<Idle>("I'll leave you to it.")
                    npc<Idle>("Yes, lots to do. Them hob-gobs left my stuff out here in a right mess. Until next time.")
                    talking = false
                }
            }
        }
    }

    private suspend fun Player.golrieStoreroom() {
        var talking = true
        while (talking) {
            choice {
                option("I found Bolrie's diary. Do you know anything about it?") {
                    golrieDiary()
                }
                option("Where did that crystal bowl come from?") {
                    player<Idle>("Where did that crystal bowl come from?")
                    npc<Idle>("A huge battle tortoise arrived a while back carrying a crate with it in. Sent by gramps, of course. It looks pretty, doesn't it? Would probably look great with some plants in it.")
                    player<Idle>("I don't think it's meant to be used as a plant pot.")
                    npc<Idle>("Well, what's it for then?")
                    player<Idle>("It's a singing bowl. You can use it to shape elven crystal.")
                    npc<Idle>("Ohh... How exciting!")
                }
                option("I'd better get going.") {
                    player<Idle>("I'd better get going.")
                    npc<Idle>("See you later.")
                    talking = false
                }
            }
        }
    }

    private suspend fun Player.golrieDiary() {
        player<Idle>("I found Bolrie's diary. Do you know anything about it?")
        npc<Idle>("Of course! Bolrie is my grandfather... assuming he's still alive, at least.")
        player<Idle>("Bolrie... Golrie... I should have guessed. You seem to like giving your children similar names, just to be confusing.")
        npc<Idle>("Certainly, in our family that's true. King Bolren is also Bolrie's younger brother, in case you didn't know.")
        player<Idle>("So that means you're King Bolren's grand-nephew, right?")
        npc<Idle>("That's right.")
        player<Idle>("Does that mean that you're in line for the throne?")
        npc<Idle>("Gnomes don't have a hereditary monarchy. We have a democratic monarchy. We vote for our kings.")
        player<Idle>("Oh, that's unusual. So you've never thought about being the King?")
        npc<Idle>("That might be fun. I wouldn't want to be like gramps though. He liked wars a bit too much. I do sometimes discuss the Khazard War with the hobgobbos, but they don't have a particularly sharp political sense.")
        npc<Idle>("Personally, I don't think we should fight his troops head- on. We're smarter than that. We should come up with some ingenious method of defeating Khazard, like they did at the Battle of Atarisundri!")
        player<Idle>("You're very well informed for a gnome in a cave. Most gnomes I've talked to have forgotten most of their own history.")
        npc<Idle>("Gnomes don't tend to dwell on the past, but I suppose I'm not a typical gnome. I get lots of time to think and read in here.")
        player<Idle>("So have you heard from Bolrie recently?")
        npc<Idle>("No, the parcels stopped arriving a few years ago. I guess he's either found what he was looking for, or has died in the attempt.")
        player<Idle>("What was he looking for?")
        npc<Idle>("I'm not sure. He was definitely obsessed with finding out as much as he could about elven magic. He also seemed to have a bit of a grudge against Glouphrie.")
        player<Idle>("Maybe he's looking for Glouphrie.")
        npc<Idle>("Who knows, eh? Who knows?")
    }

    private fun Player.advance(stage: String) {
        if (questStage(QUEST) < content.quest.questStage(QUEST, stage)) {
            set(QUEST, stage)
            refreshQuestJournal()
        }
    }

    private fun Player.giveUnique(id: String): Boolean {
        if (carriesItem(id)) {
            message("You already have this item.")
            return false
        }
        if (!inventory.add(id)) {
            message("You need a free inventory space.")
            return false
        }
        return true
    }

    private suspend fun Player.freeGolrie(target: GameObject) {
        if (questStage(QUEST) == 0) {
            npc<Idle>("golrie", "What are you doing down here? Leave before you get yourself into trouble.")
            return
        }
        if (get(QUEST, "unstarted") == "completed" && !inventory.contains("a_key")) {
            message("Golrie has locked himself in.")
            return
        }
        if (!get("waterfall_golrie_key_given", false) && !inventory.contains("a_key")) {
            message("The gate is locked.")
            lockedGolrieDialogue()
            return
        }
        val destination = Tile(target.tile.x, if (tile.y <= target.tile.y) target.tile.y + 1 else target.tile.y - 1, target.tile.level)
        if (!Door.openDoor(this, target, ticks = 10)) return
        set("waterfall_golrie_freed", true)
        try {
            delay()
            walkOverDelay(destination)
        } finally {
            GameObjects.timers.execute(target)
        }
    }

    private suspend fun Player.lockedGolrieDialogue() {
        player<Idle>("Hello, are you okay?")
        npc<Idle>("golrie", "Oh, don't worry, I'm totally fine. I locked myself in here for protection, but I've left the key somewhere.")
        player<Idle>("Okay... I'll have a look for a key.")
    }

    internal fun Player.canEnterTomb(): Boolean = questStage(QUEST) >= 4 &&
        inventory.contains("glarials_pebble") &&
        (inventory.items + equipment.items).none { item ->
            val slot: EquipSlot = item.def.getOrNull("slot") ?: EquipSlot.None
            !item.isEmpty() && (item.id.endsWith("_rune") || slot in setOf(EquipSlot.Weapon, EquipSlot.Shield, EquipSlot.Ammo) || COMBAT_BONUSES.any { item.def.get(it, 0) != 0 })
        }

    private suspend fun Player.enterChamberDoor(target: GameObject) {
        if (!inventory.contains("a_key_waterfall_dungeon")) {
            message("The door is locked.")
            return
        }
        val entering = tile.y <= target.tile.y
        val destination = target.tile.copy(y = if (entering) target.tile.y + 1 else target.tile.y - 1)
        if (!Door.openDoor(this, target, ticks = 10)) return
        try {
            delay()
            walkOverDelay(destination)
        } finally {
            GameObjects.timers.execute(target)
        }
        message("You open the door and walk through.")
        // The raised chamber uses a separate copy of the map.
        if (target.tile.x >= 2600) {
            tele(2566, 9900)
        } else if (target.tile.y == 9901 && entering && questStage(QUEST) >= 8) {
            tele(2603, 9901)
        }
    }

    internal fun Player.placeRune(pillar: Tile, rune: String): Boolean {
        val index = PILLARS.indexOf(pillar)
        val runeIndex = RUNES.indexOf(rune)
        if (index == -1 || runeIndex == -1 || questStage(QUEST) < 4 || questStage(QUEST) >= 8) return false
        val bit = 1 shl (index * 3 + runeIndex)
        val placed = get("waterfall_pillars", 0)
        if (placed and bit != 0) {
            message("You have already placed this type of rune on this pillar.")
            return false
        }
        if (!inventory.remove(rune)) return false
        set("waterfall_pillars", placed or bit)
        return true
    }

    private fun Player.washOut() {
        damage(80)
        tele(2527, 3413)
    }

    private suspend fun Player.floodChamber(text: String) {
        message(text)
        delay(2)
        washOut()
        message("...you are washed out of the cave and down the river.")
    }

    private suspend fun Player.swimDownstream() {
        message("You swim out into the water...")
        delay(2)
        washOut()
        message("...but the current is too strong, washing you downstream.")
    }

    internal fun Player.completeQuest(): Boolean {
        if (quest(QUEST) != "restored_amulet" || inventory.spaces < 5 || !inventory.contains("glarials_urn")) return false
        val success = inventory.transaction {
            remove("glarials_urn")
            add("glarials_urn_empty")
            add("gold_bar", 2)
            add("diamond", 2)
            add("mithril_seeds", 40)
        }
        if (!success) return false
        set(QUEST, "completed")
        exp(Skill.Attack, 13750.0)
        exp(Skill.Strength, 13750.0)
        AuditLog.event(this, "quest_completed", QUEST)
        AdventurersLogs.questCompleted(this, QUEST, points = 1)
        inc("quest_points")
        refreshQuestJournal()
        jingle("quest_complete_1")
        longQueue("quest_complete", 1) {
            questComplete("Waterfall Quest", "1 Quest Point", "13,750 Attack XP", "13,750 Strength XP", "2 gold bars", "2 diamonds", "40 mithril seeds", "Access to the Waterfall Dungeon", item = "glarials_urn")
        }
        return true
    }

    companion object {
        const val QUEST = "waterfall_quest"
        const val ALL_RUNES = (1 shl 18) - 1
        val RUNES = listOf("air_rune", "water_rune", "earth_rune")
        private val COMBAT_BONUSES = listOf("stab_attack", "slash_attack", "crush_attack", "magic_attack", "range_attack", "stab_defence", "slash_defence", "crush_defence", "magic_defence", "range_defence", "strength", "ranged_strength", "magic_strength", "prayer_bonus")
        val PILLARS = listOf(Tile(2562, 9910), Tile(2562, 9912), Tile(2562, 9914), Tile(2569, 9910), Tile(2569, 9912), Tile(2569, 9914))
    }
}
