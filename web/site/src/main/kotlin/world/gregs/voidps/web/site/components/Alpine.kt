package world.gregs.voidps.web.site.components

import kotlinx.html.Tag

/**
 * Thin wrappers over Alpine.js directives so components read like plain attributes
 * instead of raw `attributes["..."] = ...` calls.
 */
fun Tag.xData(expression: String) {
    attributes["x-data"] = expression
}

fun Tag.xShow(expression: String) {
    attributes["x-show"] = expression
}

fun Tag.xModel(expression: String) {
    attributes["x-model"] = expression
}

fun Tag.xText(expression: String) {
    attributes["x-text"] = expression
}

fun Tag.xBindClass(expression: String) {
    attributes["x-bind:class"] = expression
}

/**
 * Toggles a handful of style declarations (`"prop:value;prop:value"`) based on [condition],
 * merging them onto the element via `void.js`'s `voidBx` helper instead of replacing the whole
 * `style` attribute — which is what a plain `x-bind:style="expr"` string binding would do,
 * wiping out any static layout styles set at render time.
 */
fun Tag.xToggleStyle(condition: String, whenTrue: String, whenFalse: String = "") {
    attributes["x-effect"] = "voidBx(${'$'}el, $condition, '$whenTrue', '$whenFalse')"
}

/** Continuously sets one CSS custom/standard [property] from a JS [expression], without touching the rest of `style`. */
fun Tag.xEffectStyle(property: String, expression: String) {
    attributes["x-effect"] = "${'$'}el.style.setProperty('$property', $expression)"
}

fun Tag.onClick(statement: String) {
    attributes["@click"] = statement
}

fun Tag.onClickOutside(statement: String) {
    attributes["@click.outside"] = statement
}

fun Tag.onClickStop(statement: String) {
    attributes["@click.stop"] = statement
}

fun Tag.onMouseEnter(statement: String) {
    attributes["@mouseenter"] = statement
}

fun Tag.onMouseLeave(statement: String) {
    attributes["@mouseleave"] = statement
}

fun Tag.transition() {
    attributes["x-transition"] = ""
}
