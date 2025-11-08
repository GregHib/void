package world.gregs.voidps.engine

fun script(block: Script.() -> Unit) {
    object : Script {
        init {
            block(this)
        }
    }
}