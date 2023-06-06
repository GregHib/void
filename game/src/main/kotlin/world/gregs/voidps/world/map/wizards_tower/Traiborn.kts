import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.client.update.batch.animate
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.removeToLimit
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.interact.NPCInteraction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.sound.playSound

val floorItems: FloorItems by inject()
val objects: GameObjects by inject()

var Player.bonesRequired: Int
    get() = get("demon_slayer_bones", -1)
    set(value) = set("demon_slayer_bones", value)

on<NPCOption>({ operate && npc.id == "traiborn" && option == "Talk-to" }) { player: Player ->
    npc<Uncertain>("Ello young thingummywut.")
    if (player["demon_slayer", "unstarted"] == "key_hunt") {
        keyCheck()
    } else {
        thingummywutChoice()
    }
}

on<InterfaceOnNPC>({ operate && npc.id == "traiborn" && item.id == "bones" && player.bonesRequired > 0 }) { player: Player ->
    player.talkWith(npc)
    giveBones()
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
        floorItems.add(player.tile, "spinach_roll")
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

suspend fun NPCInteraction.startSpell() {
    npc<Talking>("Hurrah! That's all 25 sets of bones.")
    npc.setAnimation("traiborn_bone_spell")
    npc.setGraphic("traiborn_bone_spell")
    player.playSound("demon_slayer_bone_spell")
    npc<Uncertain>("""
        Wings of dark and colour too,
        Spreading in the morning dew;
        Locked away I have a key;
        Return it now, please, unto me.
    """)
    player.playSound("demon_slayer_cupboard_appear")
    val direction = Direction.westClockwise.first { !npc.blocked(it) }
    val rotation = Direction.westClockwise.indexOf(direction.rotate(6))
    val obj = objects.add("demon_slayer_spell_wardrobe", npc.tile.add(direction), 10, rotation, 5)
    npc.clearWatch()
    npc.face(obj)
    delay(1)
    npc.setAnimation("unlock_chest")
    player.playSound("chest_open")
    delay(1)
    player.inventory.add("silverlight_key_wizard_traiborn")
    obj.animate("demon_slayer_cupboard_disappear")
    player.playSound("demon_slayer_cupboard_disappear")
    npc.watch(player)
    item("Traiborn hands you a key.", "silverlight_key_wizard_traiborn", 400)
    player<Talking>("Thank you very much.")
    npc<Talking>("Not a problem for a friend of Sir What's-his-face.")
}

suspend fun NPCOption.thingummywutChoice() {
    val choice = choice("""
        What's a thingummywut?
        Teach me to be a mighty and powerful wizard.
        I'd better go.
    """)
    when (choice) {
        1 -> thingummywut()
        2 -> teachMe()
        3 -> {
            player<Uncertain>("I'd better go.")
            npc<Talking>("Cheerrio then.")
        }
    }
}

suspend fun NPCOption.somewhereToBe() {
    npc<Uncertain>("""
        Don't you have somewhere to be, young
        thingummywut? You still have that key you asked me
        for.
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
            giveBones()
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

suspend fun NPCInteraction.giveBones() {
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