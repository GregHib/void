package content.skill.summoning.pet

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

private const val SCAN_RADIUS = 10
private const val CHASE_RADIUS = 8
private const val CATCH_CHANCE = 0.33

private fun NPC.isRat(): Boolean = id.startsWith("rat")

fun Player.hasCatspeakAmulet(): Boolean {
    val id = equipped(EquipSlot.Amulet).id
    return id == "catspeak_amulet" || id == "catspeak_amulet_e"
}

fun isAdultCat(npc: NPC): Boolean {
    val row = petRowForNpc(npc.id) ?: return false
    if (!row.isCatLike()) return false
    val stage = row.stageForNpc(npc.id) ?: return false
    return stage == PetStage.Grown || stage == PetStage.Overgrown
}

class KittenInteract : Script {

    init {
        val registered = mutableSetOf<String>()
        for (row in allPetRows()) {
            if (!row.isCatLike()) continue
            for (npcId in listOfNotNull(row.npcOrNull("baby_npc"), row.npcOrNull("grown_npc"), row.npcOrNull("overgrown_npc"))) {
                if (!registered.add(npcId)) continue
                npcOperate("Interact-with", npcId) { interact ->
                    if (pet?.index != interact.target.index) {
                        message("This isn't your pet.")
                        return@npcOperate
                    }
                    openMenu(interact.target)
                }
            }
        }
    }

    private suspend fun Player.openMenu(cat: NPC) {
        // Any Interact-with click counts as attention; reset the kitten
        // loneliness counter before the player even picks an option.
        petRowForNpc(cat.id)?.let { row ->
            if (row.isCatLike() && row.stageForNpc(cat.id) == PetStage.Baby) {
                resetKittenLoneliness(row.rowId)
            }
        }
        choice("Interact with ${if (isAdultCat(cat)) "Cat" else "Kitten"}") {
            option("Stroke") { stroke(cat) }
            option("Chase vermin") { chaseVermin(cat) }
            option("Shoo away") { shooConfirm(cat) }
        }
    }

    private suspend fun Player.stroke(cat: NPC) {
        steps.clear()
        cat.steps.clear()
        cat.start("movement_delay", Int.MAX_VALUE)
        cat.watch(this)
        face(cat)
        anim("pet_stroke_player")
        cat.anim("pet_stroke_kitten")
        if (hasCatspeakAmulet() && isAdultCat(cat)) {
            statement("You softly stroke your cat.")
            player<Happy>("Who's a good cat then?")
            npc<Happy>(cat.id, "Me, me. Scratch me behind the ears. Purr...purr...")
            statement("The cat turns on its side while you bend down to pet it.")
        } else {
            statement("You softly stroke your cat.")
            cat.say("Purr...purr...")
            statement("The cat turns on its side while you bend down to pet it.")
        }
        player<Happy>("That cat sure loves to be stroked.")
        if (pet?.index == cat.index) {
            cat.say("Miaow!")
            cat.stop("movement_delay")
            cat.steps.clear()
        }
    }

    private suspend fun Player.chaseVermin(cat: NPC) {
        if (hasCatspeakAmulet() && isAdultCat(cat)) {
            player<Happy>("Go on get that nasty rodent.")
            npc<Happy>(cat.id, "Yesss, food.")
        } else {
            player<Quiz>("Do you fancy a bit of hunting?")
            npc<Happy>(cat.id, "Meoowww. Yeah! Let's go kick some fur!")
            player<Happy>("Take it easy sport, just don't hurt yourself.")
        }

        say("Go on puss...kill that rat!")
        val nearbyRat = NPCs.at(tile.regionLevel)
            .filter { it.isRat() }
            .filter { it.tile.distanceTo(tile) <= SCAN_RADIUS }
            .minByOrNull { it.tile.distanceTo(cat.tile) }
        if (nearbyRat == null) {
            message("There aren't any vermin around.")
            return
        }
        if (nearbyRat.tile.distanceTo(cat.tile) > CHASE_RADIUS) {
            message("Your cat cannot get to its prey.")
            return
        }
        val caught = Math.random() < CATCH_CHANCE
        cat.say("Meeeoooooowwww!")
        nearbyRat.say("Eeek!")
        cat.mode = EmptyMode
        cat.walkTo(nearbyRat.tile)
        queue("kitten_chase", 4) {
            val current = pet
            if (current != null && current.index == cat.index && nearbyRat.tile.distanceTo(current.tile) <= 1) {
                current.face(nearbyRat)
                current.anim("pet_pounce_kitten")
            }
            queue("kitten_chase_resolve", 1) {
                val resolved = pet
                if (resolved != null && resolved.index == cat.index) {
                    resolved.mode = Follow(resolved, this)
                }
                if (caught && nearbyRat.tile.distanceTo(resolved?.tile ?: nearbyRat.tile) <= 1) {
                    NPCs.remove(nearbyRat)
                    val count = inc("pet_rats_caught", 1)
                    player<Happy>("Hey well done puss, you got it!")
                    resolved?.say("MeeeoooooW!")
                    if (count % 10 == 0) {
                        player<Happy>("Well done puss! $count horrible rodents caught!")
                    }
                } else {
                    message("The rat manages to get away!")
                }
            }
        }
    }

    private suspend fun Player.shooConfirm(cat: NPC) {
        choice("Are you sure you want to shoo away the cat?") {
            option<Quiz>("Yes I am.") {
                if (pet?.index != cat.index) return@option
                say("Shoo cat!")
                cat.say("Meeeooow!")
                dismissPet()
                message("The cat has run away.")
            }
            option<Sad>("No I'm not.") {
                message("You choose not to shoo away the cat.")
            }
        }
    }
}

fun isHellcat(cat: NPC): Boolean = petRowForNpc(cat.id)?.rowId == "hellcat"

/** Adult-cat catspeak Talk-to: 4-option chathead tree that recurses until the player picks the quit option. */
suspend fun Player.talkToCatWithAmulet(cat: NPC) {
    if (isHellcat(cat)) {
        talkToHellcatWithAmulet(cat)
        return
    }
    choice("What would you like to ask?") {
        option("How are you doing?") {
            player<Quiz>("How are you doing?")
            npc<Happy>(cat.id, "I'm good. But could we go adventuring soon? I'm tired of talkin, meeoow.")
            talkToCatWithAmulet(cat)
        }
        option("How old are you now?") {
            player<Quiz>("How old are you now?")
            npc<Happy>(cat.id, "I'm not too old, and not too young. In fact I think I'm just right.")
            talkToCatWithAmulet(cat)
        }
        option("Where do you want to go?") {
            player<Quiz>("Where do you want to go?")
            npc<Happy>(cat.id, "Can we go to Varrock Sewer and chase some rats?")
            talkToCatWithAmulet(cat)
        }
        option("What do you want to do now?") {
            player<Quiz>("What do you want to do now?")
            npc<Happy>(cat.id, "I want to go chase things, kill them and then eat them. Purrr.")
            talkToCatWithAmulet(cat)
        }
        if (questCompleted("icthlarins_little_helper")) {
            option("Did you understand what went on in that quest with the devourer and Icthlarin?") {
                icthlarinRecap(cat)
                talkToCatWithAmulet(cat)
            }
        }
        option("That's enough talking for now.")
    }
}

private suspend fun Player.icthlarinRecap(cat: NPC) {
    val asked = inc("cat_icthlarin_asks", 1)
    if (asked >= 4) {
        player<Quiz>("Can you explain to me about Icthlarin and the devourer again?")
        npc<Happy>(cat.id, "I'm getting a little bit sick of talking about Icthlarin and all that stuff. If you didn't understand it the first time around, nor when I filled you in on a few details and then when I spelt it out to you what happened I don't think you'll ever get it.")
        player<Quiz>("Aw come on. Just tell me one more time.")
        npc<Happy>(cat.id, "Look, can we just leave it. Ask one of your adventuring friends.")
        player<Happy>("Alright puss.")
        return
    }
    player<Quiz>("Did you understand what went on in that quest with the devourer and Icthlarin?")
    npc<Happy>(cat.id, "It wasn't all that difficult to understand.")
    player<Quiz>("Really? I was, no actually am, still confused.")
    npc<Happy>(cat.id, "Ok, I'll tell you what I think happened.")
    player<Happy>("Great! Story time.")
    npc<Happy>(cat.id, "There once was a high priest of some crazy religion called Klenter.")
    player<Quiz>("What, a religion called Klenter? What kind of name is that?")
    npc<Happy>(cat.id, "No no. The high priest was called Klenter and he was the high priest of Icthlarin, god of the dead.")
    player<Happy>("Ok, I'm with you that far.")
    npc<Happy>(cat.id, "He dies, and a struggle starts for his soul. Another god - the Devourer - wants to destroy his soul because she has some dispute with Icthlarin. So she takes on the guise of a human.")
    player<Happy>("Ah - the wanderer.")
    npc<Happy>(cat.id, "Very good. Now she needs help in getting Klenter's soul so she tricks some gullible fool into helping her.")
    player<Quiz>("So who was that?")
    npc<Happy>(cat.id, "You!")
    player<Quiz>("Oh. So why doesn't she have a high priest too like Icthlarin.")
    npc<Happy>(cat.id, "Well she's the god of destruction. If she had a priest or a temple or anything she would just destroy it.")
    player<Quiz>("So if she's so powerful why does she need this gullible fool's help?")
    npc<Happy>(cat.id, "Because of my kind.")
    player<Quiz>("Your kind?")
    npc<Happy>(cat.id, "Cats!")
    player<Quiz>("What? Why would she care about you? All you ever want is a bit of attention and the odd fish or two.")
    npc<Happy>(cat.id, "We have other powers, not clear to you. Anyway I'm moving away from the story. The wanderer recruits your help by hypnotising you. Do you remember that?")
    player<Quiz>("Vaguely, I think. Why did she choose me to help her then.")
    npc<Happy>(cat.id, "Because of me.")
    player<Quiz>("I don't understand.")
    npc<Happy>(cat.id, "Cats are the only things that can open the pyramid's door, so she needed an adventurer with one. So you entered the pyramid with me - under her mind control - and stole a canopic jar containing an organ belonging to Klenter.")
    player<Happy>("Ahh I think I'm beginning to get a better understanding.")
    npc<Happy>(cat.id, "You then started to return with the jar to the Devourer, but as a sting on the tail she made you plant one of her symbols in the ceremonial room of the pyramid. You then tried to flee the pyramid but Icthlarin appeared just as you reached the exit.")
    player<Quiz>("The guy with the head of a dog?")
    npc<Happy>(cat.id, "Yes Player. Well he either broke the devourer's hold on you or else Klenter did.")
    player<Quiz>("Klenter? I thought he was dead.")
    npc<Happy>(cat.id, "He was, and still is. Ok, Icthlarin summoned Klenter's soul to torment you into returning the jar, the end result was that you were freed from the devourer's grasp.")
    player<Happy>("So that's when I woke up with that jar in my inventory and had that intolerable ghost harassing me.")
    npc<Happy>(cat.id, "You then bumbled around for a bit and returned the jar, discovering that the high priest still hadn't completed the final ceremony so you got him all the bits and pieces.")
    player<Happy>("So then I remembered about the devourer's symbol which I placed in the ceremonial room and had to rush back and warn the priests about the devourer.")
    npc<Happy>(cat.id, "And the rest is simple enough to piece together.")
    player<Happy>("Thanks cat, you know you're quite smart for a fish-eating animated ball of fluff.")
    npc<Happy>(cat.id, "You say the sweetest things. Hiss.")
}

/** Hellcat catspeak Talk-to: separate option tree with wily/lazy hellcat flavour. */
private suspend fun Player.talkToHellcatWithAmulet(cat: NPC) {
    choice("What would you like to ask?") {
        option("How are you doing?") {
            player<Quiz>("How are you doing?")
            npc<Happy>(cat.id, "I'm as happy as a demon in a lava pit.")
            talkToCatWithAmulet(cat)
        }
        option("How old are you now?") {
            player<Quiz>("How old are you now?")
            npc<Happy>(cat.id, "I'm feeling a bit like a ghost in a cake shop.")
            player<Quiz>("A ghost in a cake shop? What do you mean?")
            npc<Happy>(cat.id, "You know, in need of exorcise.")
            talkToCatWithAmulet(cat)
        }
        option("Where do you want to go?") {
            player<Quiz>("Where do you want to go?")
            npc<Happy>(cat.id, "Let's go steal some things from stalls...")
            talkToCatWithAmulet(cat)
        }
        option("What do you want to do now?") {
            player<Quiz>("What do you want to do now?")
            npc<Happy>(cat.id, "Curling up on a nice rug in front of a fire...")
            talkToCatWithAmulet(cat)
        }
        option("That's enough talking for now.")
    }
}

/** Simple talk for kittens and adult cats without the amulet. */
suspend fun Player.talkToCatPlain(cat: NPC) {
    if (isHellcat(cat)) {
        player<Happy>("Hey lazy bones!")
        cat.say("Hiss!")
        player<Happy>("Easy, easy don't wear yourself out just yet.")
        return
    }
    player<Happy>("Hey puss! Any news?")
    cat.say("Purr.")
    cat.say("Meow!")
}

/** Pick-up with the amulet equipped, adult cat only. */
suspend fun Player.pickupCatWithAmulet(cat: NPC) {
    player<Happy>("Come here furball.")
    npc<Happy>(cat.id, "Can we go adventuring together again, soon? Maybe some mouse-catching, meoow?")
    player<Happy>("Soon, I promise.")
    pickupPet()
}

/** Adult cat summon (Drop / Release item) with the amulet equipped. */
suspend fun Player.summonCatWithAmulet(row: RowDefinition) {
    val npcId = row.npcOrNull("grown_npc") ?: row.npcOrNull("baby_npc") ?: return
    player<Quiz>("Hey cat, do you fancy stretching your legs a bit?")
    npc<Happy>(npcId, "Miaaow, Are we going adventuring?")
    player<Happy>("We'll see puss, we'll see.")
}
