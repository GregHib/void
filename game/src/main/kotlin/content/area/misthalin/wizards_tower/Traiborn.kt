package content.area.misthalin.wizards_tower

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.sound
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.type.Direction

class Traiborn : Script {

    val floorItems: FloorItems by inject()
    val objects: GameObjects by inject()

    var Player.bonesRequired: Int
        get() = get("demon_slayer_bones", -1)
        set(value) = set("demon_slayer_bones", value)

    init {
        npcOperate("Talk-to", "traiborn") { (target) ->
            npc<Uncertain>("Ello young thingummywut.")
            if (quest("demon_slayer") == "key_hunt") {
                if (inventory.contains("silverlight_key_wizard_traiborn")) {
                    somewhereToBe()
                } else {
                    bonesCheck(target)
                }
            } else {
                choice {
                    thingummywut()
                    teachMe()
                    option<Uncertain>("I'd better go.") {
                        npc<Neutral>("Cheerrio then.")
                    }
                }
            }
        }

        itemOnNPCOperate("bones", "traiborn") {
            if (player.bonesRequired > 0) {
                player.talkWith(target)
                player.giveBones(target)
            }
        }
    }

    fun ChoiceBuilder2.thingummywut(): Unit = option<Uncertain>("What's a thingummywut?") {
        npc<Uncertain>("A thingummywut? Where? Where?")
        npc<Uncertain>("Those pesky thingummywuts. They get everywhere. They leave a terrible mess too.")
        choice {
            option<Neutral>("Err you just called me thingummywut.") {
                npc<Uncertain>("You're a thingummywut? I've never seen one up close before. They said I was mad!")
                npc<Uncertain>("Now you are my proof! There ARE thingummywuts in this tower. Now where can I find a cage big enough to keep you?")
                betterBeOffChoice()
            }
            option<Angry>("Tell me what they look like and I'll mash 'em.") {
                npc<Uncertain>("Don't be ridiculous. No-one has ever seen one.")
                npc<Uncertain>("They're invisible, or a myth, or a figment of my imagination. Can't remember which right now.")
            }
        }
    }

    fun ChoiceBuilder2.betterBeOff(): Unit = option<Talk>("Err I'd better be off really.") {
        npc<Uncertain>("Oh ok, have a good time, and watch out for sheep! They're more cunning than they look.")
    }

    fun ChoiceBuilder2.teachMe(): Unit = option<Talk>("Teach me to be a mighty and powerful wizard.") {
        npc<Uncertain>("Wizard eh? You don't want any truck with that sort. They're not to be trusted. That's what I've heard anyways.")
        choice {
            option<Quiz>("So aren't you a wizard?") {
                npc<Angry>("How dare you? Of course I'm a wizard. Now don't be so cheeky or I'll turn you into a frog.")
            }
            option<Talk>("Oh I'd better stop talking to you then.") {
                npc<Neutral>("Cheerio then. It was nice chatting to you.")
            }
        }
    }

    fun ChoiceBuilder2.youLookedAfterIt(): Unit = option<Talk>("He told me you were looking after it for him.") {
        npc<Uncertain>("That wasn't very clever of him. I'd lose my head if it wasn't screwed on. Go and tell him to find someone else to look after his valuables in future.")
        choice {
            option<Neutral>("Okay, I'll go and tell him that.") {
                npc<Neutral>("Oh that's great, if it wouldn't be too much trouble.")
                choice {
                    betterBeOff()
                    anyKeys()
                }
            }
            anyKeys()
        }
    }

    fun ChoiceBuilder2.needAKey(): Unit = option<Talk>("I need to get a key given to you by Sir Prysin.") {
        npc<Uncertain>("Sir Prysin? Who's that? What would I want his key for?")
        choice {
            youLookedAfterIt()
            kingsKnight()
            anyKeys()
        }
    }

    suspend fun Player.betterBeOffChoice() {
        choice {
            betterBeOff()
            option<Neutral>("They're right, you are mad.") {
                npc<Upset>("That's a pity. I thought maybe they were winding me up.")
            }
        }
    }

    fun ChoiceBuilder2.kingsKnight(): Unit = option<Talk>("He's one of the King's knights.") {
        npc<Happy>("Say, I remember one of the King's knights. He had nice shoes...")
        npc<Upset>("...and didn't like my homemade spinach rolls. Would you like a spinach roll?")
        choice {
            option<Neutral>("Yes please.") {
                spinachRoll()
            }
            justTellMe()
        }
    }

    suspend fun Player.spinachRoll() {
        inventory.add("spinach_roll")
        if (inventory.transaction.error != TransactionError.None) {
            floorItems.add(tile, "spinach_roll", disappearTicks = 300)
        }
        item("spinach_roll", 400, "Traiborn digs around in the pockets of his robes. After a few moments he triumphantly presents you with a spinach roll.")
        player<Neutral>("Thank you very much.")
        betterBeOffChoice()
    }

    fun ChoiceBuilder2.anyKeys(): Unit = option<Talk>("Well, have you got any keys knocking around?") {
        npc<Uncertain>("Now you come to mention it, yes I do have a key. It's in my special closet of valuable stuff. Now how do I get into that?")
        npc<Uncertain>("I sealed it using one of my magic rituals. So it would make sense that another ritual would open it again.")
        player<Talk>("So do you know what ritual to use?")
        npc<Talk>("Let me think a second.")
        npc<Uncertain>("Yes a simple drazier style ritual should suffice. Hmm, main problem with that is I'll need 25 sets of bones. Now where am I going to get hold of something like that?")
        choice {
            option<Upset>("Hmm, that's too bad. I really need that key.") {
                npc<Talk>("Ah well, sorry I couldn't be any more help.")
            }
            option<Talk>("I'll help get the bones for you.") {
                bonesRequired = 25
                npc<Neutral>("Ooh that would be very good of you.")
                player<Talk>("Okay, I'll speak to you when I've got some bones.")
            }
        }
    }

    fun ChoiceBuilder2.keyForSilverlight(): Unit = option<Talk>("It's the key to get a sword called Silverlight.") {
        npc<Uncertain>("Silverlight? Never heard of that. Sounds a good name for a ship. Are you sure it's not the name of a ship rather than a sword?")
        choice {
            option<Talk>("Yeah, pretty sure.") {
                npc<Upset>("That's a pity, waste of a name.")
                betterBeOff()
            }
            anyKeys()
        }
    }

    fun ChoiceBuilder2.justTellMe(): Unit = option<Talk>("Just tell me if you have the key.") {
        npc<Uncertain>("The key? The key to what?")
        npc<Uncertain>("There's more than one key in the world don't you know? Would be a bit odd if there was only the one.")
        choice {
            keyForSilverlight()
            option<Talk>("You've lost it haven't you?") {
                npc<Upset>("Me? Lose things? That's a nasty accusation.")
                anyKeys()
            }
        }
    }

    suspend fun Player.startSpell(target: NPC) {
        npc<Neutral>("Hurrah! That's all 25 sets of bones.")
        target.anim("traiborn_bone_spell")
        target.gfx("traiborn_bone_spell")
        sound("demon_slayer_bone_spell")
        npc<Uncertain>("Wings of dark and colour too, Spreading in the morning dew; Locked away I have a key; Return it now, please, unto me.")
        sound("demon_slayer_cupboard_appear")
        val direction = Direction.westClockwise.first { !target.blocked(it) }
        val rotation = Direction.westClockwise.indexOf(direction.rotate(6))
        val obj = objects.add("demon_slayer_spell_wardrobe", target.tile.add(direction), 10, rotation, 5)
        target.clearWatch()
        target.face(obj)
        delay(1)
        target.anim("open_chest")
        sound("chest_open")
        delay(1)
        inventory.add("silverlight_key_wizard_traiborn")
        obj.anim("demon_slayer_cupboard_disappear")
        sound("demon_slayer_cupboard_disappear")
        target.watch(this)
        item("silverlight_key_wizard_traiborn", 400, "Traiborn hands you a key.")
        player<Neutral>("Thank you very much.")
        npc<Neutral>("Not a problem for a friend of Sir What's-his-face.")
    }

    suspend fun Player.somewhereToBe() {
        npc<Uncertain>("Don't you have somewhere to be, young thingummywut? You still have that key you asked me for.")
        player<Talk>("You're right. I've got a demon to slay.")
    }

    suspend fun Player.bonesCheck(target: NPC) {
        when (bonesRequired) {
            0 -> lostKey()
            -1 -> choice {
                thingummywut()
                teachMe()
                needAKey()
            }
            else -> {
                npc<Uncertain>("How are you doing finding bones?")
                if (!inventory.contains("bones")) {
                    player<Talk>("I haven't got any at the moment.")
                    npc<Talk>("Nevermind, keep working on it.")
                    return
                }

                player<Talk>("I have some bones.")
                npc<Talk>("Give 'em here then.")
                giveBones(target)
            }
        }
    }

    suspend fun Player.lostKey() {
        player<Upset>("I've lost the key you gave to me.")
        npc<Uncertain>("Yes I know, it was returned to me. If you want it back you're going to have to collect another 25 sets of bones.")
        bonesRequired = 25
    }

    suspend fun Player.giveBones(target: NPC) {
        val removed = inventory.removeToLimit("bones", bonesRequired)
        statement("You give Traiborn $removed ${"set".plural(removed)} of bones.")
        bonesRequired -= removed
        if (bonesRequired <= 0) {
            bonesRequired = 0
            startSpell(target)
        } else {
            player<Talk>("That's all of them.")
            npc<Uncertain>("I still need $bonesRequired more.")
            player<Talk>("Ok, I'll keep looking.")
        }
    }
}
