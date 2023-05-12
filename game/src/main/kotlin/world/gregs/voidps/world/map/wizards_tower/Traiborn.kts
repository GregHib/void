import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.removeToLimit
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
val objects: GameObjectFactory by inject()

var Player.bonesRequired: Int
    get() = get("demon_slayer_bones", -1)
    set(value) = set("demon_slayer_bones", value)

on<NPCOption>({ npc.id == "traiborn" && option == "Talk-to" }) { player: Player ->
    npc<Uncertain>("Ello young thingummywut.")
    when (player["demon_slayer", "unstarted"]) {
        "key_hunt" -> keyCheck()
        else -> thingummywutChoice()
    }
}

suspend fun NPCOption.thingummywut() {
    player<Uncertain>("What's a thingummywut?")
    npc<Uncertain>("A thingummywut? Where? Where?")
    npc<Uncertain>("""
        Those pesky thingummywuts. They get everywhere.
        They leave a terrible mess too."
    """)
    val choice = choice("""
        Err you just called me a thingummywut.
        Tell me what they look like and I'll mash 'em.
    """)
    when (choice) {
        1 -> youCalledMeIt()
        2 -> mashEm()
    }
}

suspend fun NPCOption.youCalledMeIt() {
    player<Talking>("Err you just called me thingummywut.")
    npc<Uncertain>("""
        You're a thingummywut? I've never seen one up close
        before. They said I was mad!
    """)
    npc<Uncertain>("""
        Now you are my proof! There ARE thingummywuts in
        this tower. Now where can I find a cage big enough to
        keep you?
    """)
    betterBeOffChoice()
}

suspend fun NPCOption.betterBeOff() {
    player<Talk>("Err I'd better be off really.")
    npc<Uncertain>("""
        Oh ok, have a good time, and watch out for sheep!
        They're more cunning than they look.
    """)
}

suspend fun NPCOption.youAreMad() {
    player<Talking>("They're right, you are mad.")
    npc<Upset>("""
        That's a pity. I thought maybe they were winding me
        up.
    """)
}

suspend fun NPCOption.mashEm() {
    player<Furious>("Tell me what they look like and I'll mash 'em.")
    npc<Uncertain>("Don't be ridiculous. No-one has ever seen one.")
    npc<Uncertain>("""
        They're invisible, or a myth, or a figment of my
        imagination. Can't remember which right now.
    """)
}

suspend fun NPCOption.teachMe() {
    player<Talk>("Teach me to be a mighty and powerful wizard.")
    npc<Uncertain>("""
        Wizard eh? You don't want any truck with that sort.
        They're not to be trusted. That's what I've heard
        anyways.
    """)
    val choice = choice("""
        So aren't you a wizard?
        Oh I'd better stop talking to you then.
    """)
    when (choice) {
        1 -> youAreAWizardHarry()
        2 -> stopTalking()
    }
}

suspend fun NPCOption.youAreAWizardHarry() {
    player<Unsure>("So aren't you a wizard?")
    npc<Furious>("""
        How dare you? Of course I'm a wizard. Now don't be
        so cheeky or I'll turn you into a frog.
    """)
}

suspend fun NPCOption.stopTalking() {
    player<Talk>("Oh I'd better stop talking to you then.")
    npc<Talking>("Cheerio then. It was nice chatting to you.")
}

suspend fun NPCOption.youLookedAfterIt() {
    player<Talk>("He told me you were looking after it for him.")
    npc<Uncertain>("""
        That wasn't very clever of him. I'd lose my head if it
        wasn't screwed on. Go and tell him to find someone else
        to look after his valuables in future.
    """)
    val choice = choice("""
        Okay, I'll go and tell him that.
        Well, have you got any keys knocking around?
    """)
    when (choice) {
        1 -> illTellHim()
        2 -> anyKeys()
    }
}

suspend fun NPCOption.illTellHim() {
    player<Talking>("Okay, I'll go and tell him that.")
    npc<Talking>("Oh that's great, if it wouldn't be too much trouble.")
    val choice = choice("""
        Err I'd better be off really.
        Well, have you got any keys knocking around?
    """)
    when (choice) {
        1 -> betterBeOff()
        2 -> anyKeys()
    }
}

suspend fun NPCOption.needAKey() {
    player<Talk>("I need to get a key given to you by Sir Prysin.")
    npc<Uncertain>("""
        Sir Prysin? Who's that? What would I want his key
        for?
    """)
    val choice = choice("""
        He told me you were looking after it for him.
        He's one of the King's knights.
        Well, have you got any keys knocking around?
    """)
    when (choice) {
        1 -> youLookedAfterIt()
        2 -> kingsKnight()
        3 -> anyKeys()
    }
}

suspend fun NPCOption.betterBeOffChoice() {
    val choice = choice("""
        Err I'd better be off really.
        They're right, you are mad.
    """)
    when (choice) {
        1 -> betterBeOff()
        2 -> youAreMad()
    }
}

suspend fun NPCOption.kingsKnight() {
    player<Talk>("He's one of the King's knights.")
    npc<Cheerful>("""
        Say, I remember one of the King's knights. He had
        nice shoes...
    """)
    npc<Upset>("""
        ...and didn't like my homemade spinach rolls. Would you
        like a spinach roll?
    """)
    val choice = choice("""
        Yes please.
        Just tell me if you have the key.
    """)
    when (choice) {
        1 -> spinachRoll()
        2 -> justTellMe()
    }
}

suspend fun NPCOption.spinachRoll() {
    player<Talking>("Yes please.")
    player.inventory.add("spinach_roll")
    if (player.inventory.transaction.error != TransactionError.None) {
        items.add("spinach_roll", 1, player.tile)
    }
    item("""
        Traiborn digs around in the pockets of his robes. After
        a few moments he triumphantly presents you with a
        spinach roll.
    """, "spinach_roll", 400)
    player<Talking>("Thank you very much.")
    betterBeOffChoice()
}

suspend fun NPCOption.anyKeys() {
    player<Talk>("Well, have you got any keys knocking around?")
    npc<Uncertain>("""
        Now you come to mention it, yes I do have a key. It's
        in my special closet of valuable stuff. Now how do I get
        into that?
    """)
    npc<Uncertain>("""
        I sealed it using one of my magic rituals. So it would
        make sense that another ritual would open it again.
    """)
    player<Talk>("So do you know what ritual to use?")
    npc<Talk>("Let me think a second.")
    npc<Uncertain>("""
        Yes a simple drazier style ritual should suffice. Hmm,
        main problem with that is I'll need 25 sets of bones.
        Now where am I going to get hold of something like
        that?
    """)
    val choice = choice("""
        Hmm, that's too bad. I really need that key.
        I'll get the bones for you.
    """)
    when (choice) {
        1 -> tooBad()
        2 -> helpGetBones()
    }
}

suspend fun NPCOption.lostIt() {
    player<Talk>("You've lost it haven't you?")
    npc<Upset>("Me? Lose things? That's a nasty accusation.")
    anyKeys()
}

suspend fun NPCOption.prettySure() {
    player<Talk>("Yeah, pretty sure.")
    npc<Upset>("That's a pity, waste of a name.")
    betterBeOff()
}

suspend fun NPCOption.keyForSilverlight() {
    player<Talk>("It's the key to get a sword called Silverlight.")
    npc<Uncertain>("""
        Silverlight? Never heard of that. Sounds a good name
        for a ship. Are you sure it's not the name of a ship
        rather than a sword?
    """)
    val choice = choice("""
        Yeah, pretty sure.
        Well, have you got any keys knocking around?
    """)
    when (choice) {
        1 -> prettySure()
        2 -> anyKeys()
    }
}

suspend fun NPCOption.justTellMe() {
    player<Talk>("Just tell me if you have the key.")
    npc<Uncertain>("The key? The key to what?")
    npc<Uncertain>("""
        There's more than one key in the world don't you
        know? Would be a bit odd if there was only the one.
    """)
    val choice = choice("""
        It's the key to get a sword called Silverlight.
        You've lost it, haven't you?
    """)
    when (choice) {
        1 -> keyForSilverlight()
        2 -> lostIt()
    }
}

suspend fun NPCOption.tooBad() {
    player<Upset>("Hmm, that's too bad. I really need that key.")
    npc<Talk>("Ah well, sorry I couldn't be any more help.")
}

suspend fun NPCOption.helpGetBones() {
    player<Talk>("I'll help get the bones for you.")
    player.bonesRequired = 25
    npc<Talking>("Ooh that would be very good of you.")
    player<Talk>("Okay, I'll speak to you when I've got some bones.")
}

suspend fun NPCOption.needAKeyChoice() {
    val choice = choice("""
        What's a thingummywut?
        Teach me to be a mighty and powerful wizard.
        I need a key given to you by Sir Prysin
    """)
    when (choice) {
        1 -> thingummywut()
        2 -> teachMe()
        3 -> needAKey()
    }
}

/*

[18791] 2023-05-12 19:31:06 Npc(Wizard Traiborn, idx: 2842, name: "traiborn", id = 5081, x = 3112, y = 3162, z = 1)FaceEntity(null, index: -1)
[18791] 2023-05-12 19:31:06 Local sound                                                             SoundEffect(name = "ds_cupboard_appear", id = 2977)
[18791] 2023-05-12 19:31:06 Close Sub Interface(name = "npc_dialogue", id = 231, modal = true)      IfCloseSub(topInterface = "chat", id = 162, topComponent = 559)
[18791] 2023-05-12 19:31:06 Add map object                                                          LocAdd(name = "wardrobe_17434", id = 17434, slot = 2, rotation = 0, opflags = 31, Location(x = 3112, y = 3163, z = 1))
[18791] 2023-05-12 19:31:06 Npc(Rick, idx: 2840, name: "rick_5931", id = 5931, x = 3108, y = 3165, z = 1)Movement(type = Walk, Location(x = 3108, y = 3164, z = 1))
[18791] 2023-05-12 19:31:06 Npc(Wizard, idx: 2841, name: "wizard_wizards_tower", id = 3257, x = 3109, y = 3159, z = 1)Movement(type = Walk, Location(x = 3108, y = 3160, z = 1))

[18792] 2023-05-12 19:31:06 Npc(Rick, idx: 2840, name: "rick_5931", id = 5931, x = 3108, y = 3164, z = 1)Movement(type = Walk, Location(x = 3109, y = 3163, z = 1))
[18792] 2023-05-12 19:31:06 Npc(Wizard, idx: 2841, name: "wizard_wizards_tower", id = 3257, x = 3108, y = 3160, z = 1)Movement(type = Walk, Location(x = 3107, y = 3160, z = 1))

[18793] 2023-05-12 19:31:07 Npc(Wizard Traiborn, idx: 2842, name: "traiborn", id = 5081, x = 3112, y = 3162, z = 1)Animation(name = "unlock_chest", id = 536)
[18793] 2023-05-12 19:31:07 Local sound                                                             SoundEffect(name = "chest_open", id = 52)
[18793] 2023-05-12 19:31:07 Npc(Wizard, idx: 2841, name: "wizard_wizards_tower", id = 3257, x = 3107, y = 3160, z = 1)Movement(type = Walk, Location(x = 3108, y = 3160, z = 1))

[18794] 2023-05-12 19:31:08 Npc(Wizard Jalarast, idx: 2839, name: "wizard_3232", id = 3232, x = 3107, y = 3161, z = 1)Movement(type = Walk, Location(x = 3108, y = 3161, z = 1))
[18794] 2023-05-12 19:31:08 Npc(Wizard, idx: 2841, name: "wizard_wizards_tower", id = 3257, x = 3108, y = 3160, z = 1)Movement(type = Walk, Location(x = 3109, y = 3159, z = 1))

[18795] 2023-05-12 19:31:08 Local                                                                   Message(type = MESBOX, text = "Traiborn hands you a key.")
[18795] 2023-05-12 19:31:08 Varbit (varp = "camera_base", id = 1021, oldValue: 1)                   Varbit(name = "dialogue_full_chatbox", id = 5983, value = 0)
[18795] 2023-05-12 19:31:08 Map Object Animation                                                    LocAnim(animation = 4600, object = MapObject(id = 17434, type = 10, rotation = 0, Location(x = 3112, y = 3163, z = 1))
[18795] 2023-05-12 19:31:08 Inventory update                                                        InvComponent(inventory = "inventory", id = 93)
[18795] 2023-05-12 19:31:08 Inventory change                                                        Inv(slotId = 1, item = "silverlight_key", id = 2399, amount = 1)
[18795] 2023-05-12 19:31:08 Local sound                                                             SoundEffect(name = "ds_cupboard_dissappear", id = 2978)
[18795] 2023-05-12 19:31:08 Local                                                                   ClientScript(name = "toplevel_chatbox_resetbackground", id = 2379)
[18795] 2023-05-12 19:31:08 Sub interface                                                           IfOpenSub(name = "item_dialogue", id = 193, topInterface = "chat", id = 162, topComponent = 559, modal = true)
[18795] 2023-05-12 19:31:08 Local                                                                   ClientScript(name = "objbox_setbuttons", id = 2868, converted = ["Click here to continue"], raw = ["Click here to continue"], types = [s])
[18795] 2023-05-12 19:31:08 Interface Object                                                        IfSetObject(interface = "item_dialogue", id = 193, componentId = 1, item = "silverlight_key", id = 2399, modelZoom = 400)
[18795] 2023-05-12 19:31:08 Interface Text                                                          IfSetText(interface = "item_dialogue", id = 193, componentId = 2, text = "Traiborn hands you a key.")
[18795] 2023-05-12 19:31:08 Local(previous = 3384)                                                  RunEnergy(value = 3392)
[18795] 2023-05-12 19:31:08 Interface event                                                         IfSetEvents(interface = "item_dialogue", id = 193, componentId = 0, startIndex = 0, endIndex = 1, events = Continue)
[18795] 2023-05-12 19:31:08 Npc(Wizard Jalarast, idx: 2839, name: "wizard_3232", id = 3232, x = 3108, y = 3161, z = 1)Movement(type = Walk, Location(x = 3109, y = 3160, z = 1))

[18796] 2023-05-12 19:31:09 Npc(Wizard Traiborn, idx: 2842, name: "traiborn", id = 5081, x = 3112, y = 3162, z = 1)FaceEntity(Player(DinhoFury, idx: 1677, x = 3111, y = 3162, z = 1), index: 67213)
 */
suspend fun NPCOption.startSpell() {
    npc<Talking>("Hurrah! That's all 25 sets of bones.")
    npc<Uncertain>("""
        Wings of dark and colour too,
        Spreading in the morning dew;
        Locked away I have a key;
        Return it now, please, unto me.
    """)
    player.playSound("demon_slayer_cupboard_appear")
    val obj = objects.spawn("demon_slayer_spell_wardrobe", npc.tile, 10, 0)
    npc.setAnimation("4602")
    npc.setGraphic("777")
    player.playSound("ds_bone_spell")// 2973
//                            LocAnim(animation = 4600, object = MapObject(id = 17434, type = 10, rotation = 0, Location(x = 3112, y = 3163, z = 1))
//                                [18795] 2023-05-12 19:31:08 Inventory update
//                                animateObject("4600")
    player.playSound("ds_cupboard_dissappear")//2978
    17434
    item("Traiborn hands you a key.", "silverlight_key_wizard_traiborn", 400)
    player<Talking>("Thank you very much.")
    npc<Talking>("Not a problem for a friend of Sir What's-his-face.")
}

suspend fun NPCOption.thingummywutChoice() {
    val choice = choice("""
        What's a thingummywut?
        Teach me to be a mighty and powerful wizard.
    """)
    when (choice) {
        1 -> thingummywut()
        2 -> teachMe()
    }
}

suspend fun NPCOption.somewhereToBe() {
    npc<Uncertain>("""
        Don't you have somewhere to be, young
        thingummywut? You still have that key you asked me
        for.")
    """)
    player<Talk>("You're right. I've got a demon to slay.")
}

suspend fun NPCOption.keyCheck() {
    if (player.inventory.contains("silverlight_key_wizard_traiborn")) {
        somewhereToBe()
    } else {
        bonesCheck()
    }
}

suspend fun NPCOption.bonesCheck() {
    when (player.bonesRequired) {
        0 -> lostKey()
        -1 -> needAKeyChoice()
        else -> {
            npc<Uncertain>("How are you doing finding bones?")
            if (!player.inventory.contains("bones")) {
                player<Talk>("I haven't got any at the moment.")
                npc<Talk>("Nevermind, keep working on it.")
                return
            }

            player<Talk>("I have some bones.")
            npc<Talk>("Give 'em here then.")
            val removed = player.inventory.removeToLimit("bones", player.bonesRequired)
            statement("You give Traiborn $removed ${"set".plural(removed)} of bones.")
            player.bonesRequired -= removed
            if (player.bonesRequired <= 0) {
                player.bonesRequired = 0
                startSpell()
            } else {
                player<Talk>("That's all of them.")
                npc<Uncertain>("I still need ${player.bonesRequired} more.")
                player<Talk>("Ok, I'll keep looking.")
            }
        }
    }
}

suspend fun NPCOption.lostKey() {
    player<Upset>("I've lost the key you gave to me.")
    npc<Uncertain>("""
        Yes I know, it was returned to me. If you want it back
        you're going to have to collect another 25 sets of
        bones.
    """)
    player.bonesRequired = 25
}