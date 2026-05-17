package content.skill.summoning.pet

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

private const val SCAN_RADIUS = 10
private const val CHASE_RADIUS = 8
private const val CATCH_CHANCE = 0.33

private fun NPC.isRat(): Boolean {
    val id = this.id
    return id == "rat" ||
        id.startsWith("rat_") ||
        id.endsWith("_rat") ||
        "_rat_" in id
}

fun Player.hasCatspeakAmulet(): Boolean {
    val id = equipped(EquipSlot.Amulet).id
    return id == "catspeak_amulet" || id == "catspeak_amulet_e"
}

fun Player.isAdultCat(npc: NPC): Boolean {
    val def = get<PetDefinitions>().forNpc(npc.id) ?: return false
    if (!def.isCatLike) return false
    val stage = def.stageForNpc(npc.id) ?: return false
    return stage == PetStage.Grown || stage == PetStage.Overgrown
}

class KittenInteract(definitions: PetDefinitions) : Script {

    init {
        val registered = mutableSetOf<String>()
        for (def in definitions.all) {
            if (!def.isCatLike) continue
            for (npcId in listOfNotNull(def.babyNpc, def.grownNpc, def.overgrownNpc)) {
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
        choice("Interact with ${if (isAdultCat(cat)) "Cat" else "Kitten"}") {
            option("Stroke") { stroke(cat) }
            option("Chase vermin") { chaseVermin(cat) }
            option("Shoo away") { shooConfirm(cat) }
        }
    }

    private suspend fun Player.stroke(cat: NPC) {
        steps.clear()
        cat.steps.clear()
        cat.mode = EmptyMode
        cat.watch(this)
        face(cat)
        anim("pet_stroke_player")
        cat.anim("pet_stroke_kitten")
        if (hasCatspeakAmulet() && isAdultCat(cat)) {
            player<Happy>("Who's a good cat then?")
            npc<Happy>(cat.id, "Me, me. Scratch me behind the ears.")
            statement("Purr...purr...")
            statement("The cat turns on its side while you bend down to pet it.")
        } else {
            statement("You softly stroke your cat.")
            cat.say("Purr...purr...")
            statement("The cat turns on its side while you bend down to pet it.")
        }
        player<Happy>("That cat sure loves to be stroked.")
        if (pet?.index == cat.index) {
            cat.say("Miaow!")
            cat.mode = Follow(cat, this)
        }
    }

    private suspend fun Player.chaseVermin(cat: NPC) {
        if (hasCatspeakAmulet() && isAdultCat(cat)) {
            player<Happy>("Go on get that nasty rodent.")
            npc<Happy>(cat.id, "Yesss, food.")
        } else {
            player<Quiz>("Do you fancy a bit of hunting?")
            cat.say("Meoowww. Yeah! Let's go kick some fur!")
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
        weakQueue("kitten_chase", 5) {
            val current = pet
            if (current != null && current.index == cat.index) {
                current.mode = Follow(current, this)
            }
            if (caught && nearbyRat.tile.distanceTo(current?.tile ?: nearbyRat.tile) <= 1) {
                NPCs.remove(nearbyRat)
                val count = get("pet_rats_caught", 0) + 1
                set("pet_rats_caught", count)
                say("Hey well done puss, you got it!")
                current?.say("MeeeoooooW!")
                if (count % 10 == 0) {
                    say("Well done puss! $count horrible rodents caught!")
                }
            } else {
                message("The rat manages to get away!")
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

/** Adult-cat catspeak Talk-to: 4-option chathead loop. */
suspend fun Player.talkToCatWithAmulet(cat: NPC) {
    while (true) {
        var keepGoing = true
        choice("What would you like to ask?") {
            option("How are you doing?") {
                player<Quiz>("How are you doing?")
                npc<Happy>(cat.id, "I'm good. But could we go adventuring soon? I'd like to chase things and eat them.")
            }
            option("How old are you now?") {
                player<Quiz>("How old are you now?")
                npc<Happy>(cat.id, "I'm not too old, and not too young. The perfect age really.")
            }
            option("Where do you want to go?") {
                player<Quiz>("Where do you want to go?")
                npc<Happy>(cat.id, "Can we go to Varrock Sewer and chase some rats?")
            }
            option("What do you want to do now?") {
                player<Quiz>("What do you want to do now?")
                npc<Happy>(cat.id, "I want to go chase things, kill them and then eat them.")
                player<Quiz>("Always with the hunting...")
            }
            option("That's enough talking for now.") {
                keepGoing = false
            }
        }
        if (!keepGoing) return
    }
}

/** Simple talk for kittens / adult cats without the amulet. */
suspend fun Player.talkToCatPlain(cat: NPC) {
    player<Happy>("Hey puss! Any news?")
    cat.say("Purr.")
    cat.say("Meow!")
}

/** "Pick-up" with the amulet equipped — adult cat only. */
suspend fun Player.pickupCatWithAmulet(cat: NPC, definitions: PetDefinitions) {
    player<Happy>("Come here furball.")
    npc<Happy>(cat.id, "Can we go adventuring together again, soon?")
    player<Happy>("Soon, I promise.")
    pickupPet(definitions)
}

/** Adult cat summon (Drop / Release item) with the amulet equipped. */
suspend fun Player.summonCatWithAmulet(def: PetDefinition, itemId: String) {
    val npcId = def.grownNpc ?: def.babyNpc
    player<Quiz>("Hey cat, do you fancy stretching your legs a bit?")
    npc<Happy>(npcId, "Miaaow, Are we going adventuring?")
    player<Happy>("We'll see puss, we'll see.")
}
