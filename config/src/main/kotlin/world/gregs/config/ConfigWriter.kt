package world.gregs.config

import java.io.Writer

typealias ConfigWriter = Writer

fun ConfigWriter.writeSection(name: String) = write("[$name]\n")

fun ConfigWriter.writePair(key: String, value: Any, escapeKey: Boolean = false) {
    writeKey(key, escapeKey)
    writeValue(value)
    write("\n")
}

fun ConfigWriter.writeValue(value: Any?, escapeKey: Boolean = false) {
    when (value) {
        is String -> {
            write("\"")
            write(value)
            write("\"")
        }
        is Double -> {
            val string = value.toBigDecimal().stripTrailingZeros().toPlainString()
            if (string.contains('.')) {
                write(string)
            } else {
                write("$string.0")
            }
        }
        is Float -> {
            val string = value.toBigDecimal().stripTrailingZeros().toPlainString()
            if (string.contains('.')) {
                write(string)
            } else {
                write("$string.0")
            }
        }
        is Number, is Boolean -> write(value.toString())
        is IntArray -> list(value.size) { writeValue(value[it]) }
        is DoubleArray -> list(value.size) { writeValue(value[it]) }
        is List<*> -> list(value.size) { writeValue(value[it]) }
        is Array<*> -> list(value.size) { writeValue(value[it]) }
        is Map<*, *> -> map(value.keys, escapeKey) { writeValue(value[it]) }
        null -> write("\"null\"")
        else -> {
            write("\"")
            write(value.toString())
            write("\"")
        }
    }
}

fun ConfigWriter.writeKey(key: String, escapeKey: Boolean = key.any { it == ' ' || it == '\t' || it == '=' }) {
    if (escapeKey) {
        write("\"${key.replace("\"", "\\\"")}\"")
    } else {
        write(key)
    }
    write(" = ")
}

fun <T> ConfigWriter.map(keys: Set<T>, escapeKey: Boolean = false, block: Writer.(T) -> Unit) {
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

fun ConfigWriter.list(size: Int, block: Writer.(Int) -> Unit) {
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
