package world.gregs.voidps.engine.entity.character.player.chat

enum class Rank(val value: Int, val string: String) {
    None(-1, "No one"),
    Anyone(-128, "Anyone"),
    Friend(0, "Any friends"),
    Recruit(1, "Recruit+"),
    Corporeal(2, "Corporeal+"),
    Sergeant(3, "Sergeant+"),
    Captain(5, "Captain+"),
    General(6, "General+"),
    Admin(127, ""),
    Lieutenant(4, "Lieutenant+"),
    Owner(7, "Only me")
}