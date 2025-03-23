package world.gregs.config

import java.io.Writer


fun Writer.writeSection(name: String) = write("[$name]\n")

fun Writer.writePair(key: String, value: Any, escapeKey: Boolean = false) {
    writeKey(key, escapeKey)
    writeValue(value)
    write("\n")
}

fun Writer.writeValue(value: Any?, escapeKey: Boolean = false) {
    when(value) {
        is String -> {
            write("\"")
            write(value)
            write("\"")
        }
        is Boolean -> write(value.toString())
        is Number -> write(value.toString())
        is IntArray -> list(value.size) { writeValue(value[it]) }
        is DoubleArray -> list(value.size) { writeValue(value[it]) }
        is List<*> -> list(value.size) { writeValue(value[it]) }
        is Array<*> -> list(value.size) { writeValue(value[it]) }
        is Map<*, *> -> map(value.keys, escapeKey) { writeValue(value[it]) }
        null -> write("null")
        else -> {
            write("\"")
            write(value.toString())
            write("\"")
        }
    }
}

fun Writer.writeKey(key: String, escapeKey: Boolean = key.any { it == ' ' || it == '\t' || it == '=' }) {
    if (escapeKey) {
        write("\"${key.replace("\"", "\\\"")}\"")
    } else {
        write(key)
    }
    write(" = ")
}

fun <T> Writer.writeList(list: List<T>, block: Writer.(T) -> Unit) {
    write("[")
    var remaining = list.size
    for (item in list) {
        block.invoke(this, item)
        if (--remaining > 0) {
            write(", ")
        }

    }
    write("]")
}
fun Writer.writeArray(array: DoubleArray, block: Writer.(Double) -> Unit) {
    write("[")
    var remaining = array.size
    for (item in array) {
        block.invoke(this, item)
        if (--remaining > 0) {
            write(", ")
        }
    }
    write("]")
}

fun Writer.writeArray(array: IntArray, block: Writer.(Int) -> Unit) {
    write("[")
    var remaining = array.size
    for (item in array) {
        block.invoke(this, item)
        if (--remaining > 0) {
            write(", ")
        }
    }
    write("]")
}

fun <T> Writer.map(keys: Set<T>, escapeKey: Boolean = false, block: Writer.(T) -> Unit) {
    write("{")
    var remaining = keys.size
    for (key in keys) {
        writeKey(key.toString(), escapeKey)
        block.invoke(this, key)
        if (--remaining > 0) {
            write(", ")
        }
    }
    write("}")
}

fun Writer.list(size: Int, block: Writer.(Int) -> Unit) {
    write("[")
    var remaining = size
    for (index in 0 until size) {
        block.invoke(this, index)
        if (--remaining > 0) {
            write(", ")
        }
    }
    write("]")
}

private fun needsQuotes(str: String): Boolean {
    return str.any { it == ' '
            || it == '\t'
            || it == '='
            || it == '['
            || it == ']'
            || it == '{'
            || it == '}'
            || it == ','
            || it == '\n'
            || it == '\r'
            || it == '#'
    }
}