package content.area.kandarin.baxtorian_falls

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Glorie : Script {
    init {
        npcOperate("Talk-to", "golrie") { golrieDialogue() }
        objectOperate("Open", "gate_33_closed") { (target) -> freeGolrie(target) }
        itemOnObjectOperate("a_key", "gate_33_closed") { (target) -> freeGolrie(target) }
    }

    private suspend fun Player.golrieDialogue() {
        if (questCompleted("path_of_glouphrie")) {
            golrieAfterGlouphrie()
            return
        }
        if (quest("waterfall_quest") == "unstarted") {
            npc<Idle>("What are you doing down here? Leave before you get yourself into trouble.") // TODO expressions
            return
        }
        if (!get("waterfall_golrie_freed", false)) {
            if (!inventory.contains("a_key")) {
                lockedGolrieDialogue()
            }
            return
        }
        if (carriesItem("glarials_pebble")) {
            player<Idle>("Hello, Golrie.") // TODO expressions
            npc<Idle>("Hello again.")
            player<Idle>("Any luck getting out?")
            npc<Idle>("Not yet, but don't worry. I'm sure I'll work something out. I just need some time to think.")
            player<Idle>("Well good luck.")
            return
        }
        if (get("waterfall_golrie_key_given", false) || questCompleted("waterfall_quest")) {
            player<Idle>("Hello, Golrie.") // TODO expressions
            npc<Idle>("Hello again.")
            player<Idle>("Do you mind if I have another look through this stuff?")
            npc<Idle>("No, of course not.")
            if (inventory.spaces == 0) {
                message("You look amongst the junk on the floor and find Glarial's pebble but you don't have enough room to take it.")
                return
            }
            addOrDrop("glarials_pebble")
            message("You look amongst the junk on the floor and find Glarial's pebble.")
            if (quest("waterfall_quest") == "read_book") {
                set("waterfall_quest", "found_pebble")
            }
            return
        }
        if (!inventory.contains("a_key")) {
            lockedGolrieDialogue()
            return
        }
        when (quest("waterfall_quest")) {
            "unstarted", "started", "found_hudon" -> {
                player<Idle>("Who are you, and what are you doing here?") // TODO expressions
                npc<Idle>("I'm Golrie, and this is my home. Thing is, as you can see, I've got a small problem. Those hob-gobs are all over the place trying to steal my family's heirlooms. I've been stuck here for ages!")
                player<Idle>("Do you need some help?")
                npc<Idle>("Oh don't worry, I'm sure I'll work something out. I just need some time to think.")
                player<Idle>("In that case, do you mind if I have a look around?")
            }
            else -> {
                player<Neutral>("Hello, is your name Golrie?")
                npc<Neutral>("That's me. I've been stuck in here for weeks, those goblins are trying to steal my family's heirlooms. My grandad gave me all sorts of old junk.")
                player<Neutral>("Do you mind if I have a look?")
            }
        }
        npc<Neutral>("No, of course not.")
        message("You look amongst the junk on the floor.")
        delay(2)
        message("Mixed with the junk on the floor you find Glarial's pebble.")
        player<Neutral>("Could I take this old pebble?")
        npc<Neutral>("Oh that, yes have it, it's just some old elven junk I believe.")
        delay(2)
        addOrDrop("glarials_pebble")
        if (quest("waterfall_quest") == "read_book") {
            set("waterfall_quest", "found_pebble")
        }
        if (inventory.remove("a_key")) {
            message("You give Golrie the key.")
            set("waterfall_golrie_key_given", true)
            delay(2)
            npc<Neutral>("Thanks a lot for the key traveler. I think I'll wait in here until those goblins get bored and leave.")
            player<Neutral>("OK... Take care Golrie.")
        }
    }

    private suspend fun Player.golrieAfterGlouphrie() {
        player<Idle>("Hello, Golrie. How's it going?") // TODO expressions
        npc<Idle>("Better now I'm on the right side of these bars.")
        choice {
            anythingInteresting(this@golrieAfterGlouphrie)
            device(this@golrieAfterGlouphrie)
            lostPebble(this@golrieAfterGlouphrie)
            useKey(this@golrieAfterGlouphrie)
            leaveItToYou()
        }
    }

    private fun ChoiceOption.device(player: Player) {
        if (!player["golrie_device_explained", false]) {
            option<Idle>("I managed to find that device.") {
                // TODO expressions
                npc<Idle>("Wonderful! Though you know, I don't think you ever actually mentioned what the device does.")
                player<Idle>("It counteracts illusion magic in the surrounding area. This one should cover the whole of the Tree Gnome Village. It might be useful for someone to remember that in case it's needed again.")
                npc<Idle>("Oh, well I'll be sure to keep that in mind.")
                set("golrie_device_explained", true)
                choice {
                    diary()
                    crystalBow()
                    getGoing()
                }
            }
        } else {
            option<Idle>("I have a question about your storeroom.") {
                // TODO expressions
                choice {
                    diary()
                    crystalBow()
                    getGoing()
                }
            }
        }
    }

    private fun ChoiceOption.lostPebble(player: Player) {
        if (!player.inventory.contains("glarials_pebble")) {
            option<Idle>("I lost that pebble.") {
                // TODO expressions
                npc<Idle>("Not to worry. It turned up here again.")
                addOrDrop("glarials_pebble")
                message("Golrie gives you the pebble.")
                player<Idle>("Thanks.")
                choice {
                    anythingInteresting(player)
                    device(player)
                    useKey(player)
                    leaveItToYou()
                }
            }
        }
    }

    private fun ChoiceOption.useKey(player: Player) {
        if (player.inventory.contains("a_key")) {
            return
        }
        option<Idle>("Can I use your key?") {
            // TODO expressions
            npc<Idle>("If you wanna go in there with them hob-gobs, that's your business.")
            addOrDrop("a_key")
            message("Golrie gives you a key.")
            player<Idle>("Thanks.")
            choice {
                anythingInteresting(player)
                device(player)
                lostPebble(player)
                leaveItToYou()
            }
        }
    }

    private fun ChoiceOption.leaveItToYou() {
        option<Idle>("I'll leave you to it.") {
            // TODO expressions
            npc<Idle>("Yes, lots to do. Them hob-gobs left my stuff out here in a right mess. Until next time.")
        }
    }

    private fun ChoiceOption.anythingInteresting(player: Player) {
        if (player.inventory.contains("cadarn_lineage")) {
            return
        }
        option<Idle>("Found anything interesting recently?") {
            // TODO expressions
            npc<Idle>("It just so happens I have. Take a look at this book.")
            addOrDrop("cadarn_lineage")
            message("Golrie gives you a book.")
            player<Idle>("Thanks.")
            choice {
                device(player)
                lostPebble(player)
                useKey(player)
                leaveItToYou()
            }
        }
    }

    private fun ChoiceOption.getGoing() {
        option<Idle>("I'd better get going.") {
            // TODO expressions
            npc<Idle>("See you later.")
        }
    }

    private fun ChoiceOption.crystalBow() {
        option<Idle>("Where did that crystal bowl come from?") {
            npc<Idle>("A huge battle tortoise arrived a while back carrying a crate with it in. Sent by gramps, of course. It looks pretty, doesn't it? Would probably look great with some plants in it.")
            player<Idle>("I don't think it's meant to be used as a plant pot.")
            npc<Idle>("Well, what's it for then?")
            player<Idle>("It's a singing bowl. You can use it to shape elven crystal.")
            npc<Idle>("Ohh... How exciting!")
            choice {
                diary()
                getGoing()
            }
        }
    }

    private fun ChoiceOption.diary() {
        option<Idle>("I found Bolrie's diary. Do you know anything about it?") {
            // TODO expressions
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
            choice {
                crystalBow()
                getGoing()
            }
        }
    }

    private suspend fun Player.freeGolrie(target: GameObject) {
        if (quest("waterfall_quest") == "unstarted") {
            npc<Idle>("golrie", "What are you doing down here? Leave before you get yourself into trouble.") // TODO expressions
            return
        }
        if (get("waterfall_golrie_key_given", false) && !inventory.contains("a_key")) {
            message("Golrie has locked himself in.")
            return
        }
        if (!get("waterfall_golrie_key_given", false) && !inventory.contains("a_key")) {
            message("The gate is locked.")
            lockedGolrieDialogue()
            return
        }
        if (inventory.contains("a_key")) {
            player<Neutral>("I found a key.")
            npc<Neutral>("Well don't wait all day, give it a try!")
            player<Neutral>("OK...")
        }
        set("waterfall_golrie_freed", true)
        enterDoor(target)
    }

    private suspend fun Player.lockedGolrieDialogue() {
        player<Neutral>("Hello, are you okay?")
        npc<Neutral>("golrie", "Oh, don't worry, I'm totally fine. I locked myself in here for protection, but I've left the key somewhere.")
        player<Neutral>("Okay... I'll have a look for a key.")
    }
}
