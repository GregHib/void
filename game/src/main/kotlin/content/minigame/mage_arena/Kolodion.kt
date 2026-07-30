package content.minigame.mage_arena

import content.entity.combat.Combat
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.melee.weapon.fightStyle
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile

class Kolodion : Script {

    val arenaPlayerTile = Tile(3104, 3933)
    val arenaBossTile = Tile(3105, 3933)
    val bankTile = Tile(2540, 4717)

    val nextForm = mapOf(
        "kolodion_human" to "ogre",
        "kolodion_ogre" to "troll",
        "kolodion_troll" to "dark_beast",
        "kolodion_dark_beast" to "demon",
    )

    val formLines = mapOf(
        "human" to "You must prove yourself... now!",
        "ogre" to "This is only the beginning; you can't beat me!",
        "troll" to "Foolish mortal; I am unstoppable.",
        "dark_beast" to "Now you feel it... The dark energy.",
        "demon" to "Aaaaaaaarrgghhhh! The power!",
    )

    init {
        npcOperate("Talk-to", "kolodion") { (target) ->
            when (get("mage_arena", "unstarted")) {
                "started" -> {
                    player<Neutral>("Hi.")
                    npc<Neutral>("You return, young conjurer. You obviously have a taste for the dark side of magic.")
                    startFight(target)
                }
                "completed" -> {
                    player<Happy>("Hello, Kolodion.")
                    npc<Happy>("Hello, you mage. You're a tough one.")
                    player<Quiz>("What now?")
                    npc<Neutral>("Step into the magic pool. It will take you to a chamber. There, you must decide which god you will represent in the arena.")
                    player<Happy>("Thanks, Kolodion.")
                    npc<Happy>("That's what I'm here for.")
                }
                "staff_received" -> {
                    npc<Happy>("Hey there, how are you? Are you enjoying the bloodshed?")
                    player<Neutral>("I think I've had enough for now.")
                    npc<Neutral>("A shame. You're a good battle mage. I hope to see you soon.")
                }
                else -> {
                    player<Quiz>("Hello there. What is this place?")
                    if (levels.get(Skill.Magic) < 60) {
                        npc<Angry>("Do not waste my time with trivial questions. I am the Great Kolodion, master of battle magic. I have an arena to run.")
                        player<Quiz>("Can I enter?")
                        npc<Angry>("Hah! A wizard of your level? Don't be absurd.")
                    } else {
                        intro(target)
                    }
                }
            }
        }

        npcDeath("kolodion_human,kolodion_ogre,kolodion_troll,kolodion_dark_beast") {
            val player = owner ?: return@npcDeath
            if (player["kolodion_index", -1] != index) {
                return@npcDeath
            }
            val bossIndex = index
            player["kolodion_form"] = nextForm.getValue(id)
            World.queue("kolodion_transform_${player.index}", 3) {
                if (player["kolodion_index", -1] == bossIndex && player.tile in Areas["mage_arena"]) {
                    spawnForm(player)
                }
            }
        }

        npcDeath("kolodion_demon") {
            val player = owner ?: return@npcDeath
            if (player["kolodion_index", -1] != index) {
                return@npcDeath
            }
            player["mage_arena"] = "completed"
            player.clear("kolodion_form")
            player.clear("kolodion_index")
            World.queue("kolodion_defeated_${player.index}", 3) {
                player.tele(bankTile)
                player.queue("kolodion_defeated_dialogue", 1) {
                    player.npc<Happy>("kolodion", "Well done, young adventurer. You truly are a worthy battle mage. Step into the magic pool; it will take you to a chamber where you must choose your allegiance.")
                }
            }
        }

        canAttack("kolodion_human,kolodion_ogre,kolodion_troll,kolodion_dark_beast,kolodion_demon") { target ->
            when {
                target.owner != this -> {
                    message("He's not interested in fighting you.")
                    false
                }
                fightStyle != "magic" -> {
                    message("You can only use magic in the arena!")
                    false
                }
                else -> true
            }
        }

        exited("mage_arena") {
            despawnBoss(this)
        }
    }

    suspend fun Player.intro(target: NPC) {
        npc<Neutral>("I am the great Kolodion, master of battle magic, and this is my battle arena. Top wizards travel from all over Gielinor to fight here.")
        choice {
            option<Quiz>("Can I fight here?") {
                npc<Neutral>("My arena is open to any high level wizard, but this is no game. Many wizards fall in this arena, never to rise again. The strongest mages have been destroyed.")
                npc<Quiz>("If you're sure you want in?")
                choice {
                    option<Happy>("Yes indeed.") {
                        npc<Happy>("Good, good. You have a healthy sense of competition.")
                        npc<Neutral>("Remember, traveller - in my arena, hand-to-hand combat is useless. Your strength will diminish as you enter the arena, but the spells you can learn are amongst the most powerful in all of Gielinor.")
                        npc<Neutral>("Before I can accept you in, we must duel.")
                        choice {
                            option<Happy>("Okay, let's fight.") {
                                npc<Neutral>("I must first check that you are up to scratch.")
                                player<Happy>("You don't need to worry about that.")
                                npc<Neutral>("Not just any magician can enter - only the most powerful and most feared. Before you can use the power of this arena, you must prove yourself against me.")
                                set("mage_arena", "started")
                                set("kolodion_form", "human")
                                startFight(target)
                            }
                            option<Neutral>("No thanks.")
                        }
                    }
                    option<Neutral>("No I don't.")
                }
            }
            option<Quiz>("What's the point of that?") {
                npc<Neutral>("They want to crown themselves the best mage in all of Gielinor!")
            }
            option<Angry>("That's barbaric!")
        }
    }

    suspend fun Player.startFight(target: NPC) {
        target.face(this)
        target.anim("cast_god_spell")
        anim("teleport_modern")
        gfx("teleport_modern")
        sound("teleport")
        delay(3)
        tele(arenaPlayerTile)
        anim("teleport_land_modern")
        gfx("teleport_land_modern")
        sound("teleport_land")
        spawnForm(this)
    }

    fun spawnForm(player: Player) {
        var form = player["kolodion_form", "human"]
        // Saves from before the troll/dark_beast form rename may hold an unknown form
        if (!formLines.containsKey(form)) {
            form = "human"
            player["kolodion_form"] = form
        }
        val npc = NPCs.add("kolodion_$form", arenaBossTile, ticks = -1, owner = player)
        npc["in_multi_combat"] = true
        player["kolodion_index"] = npc.index
        npc.face(player)
        npc.gfx("kolodion_spawn")
        npc.say(formLines.getValue(form))
        // Engage combat directly; the Interact mode used by interactPlayer gives up (cantReach)
        // when blocked by an obstacle, leaving the npc to wander off instead of pursuing.
        Combat.combat(npc, player)
    }

    fun despawnBoss(player: Player) {
        val npc = NPCs.indexed(player["kolodion_index", -1]) ?: return
        if (npc.owner == player && npc.id.startsWith("kolodion_")) {
            NPCs.remove(npc)
        }
        player.clear("kolodion_index")
    }
}
