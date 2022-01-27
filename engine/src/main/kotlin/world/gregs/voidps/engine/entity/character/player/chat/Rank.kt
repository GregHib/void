package world.gregs.voidps.engine.entity.character.player.chat

enum class Rank(val value: Int, val string: String) {
    None(-1, "No-one"),
    Anyone(-128, "Anyone"),
    Friend(0, "Any friends"),
    Recruit(1, "Recruit+"),
    Corporeal(2, "Corporal+"),
    Sergeant(3, "Sergeant+"),
    Lieutenant(4, "Lieutenant+"),
    Captain(5, "Captain+"),
    General(6, "General+"),
    Owner(7, "Only me"),
    Admin(127, "");

    companion object {
        val all = values()

        fun of(option: String) : Rank {
            return all.firstOrNull { it.string == option } ?: None
        }
    }
}