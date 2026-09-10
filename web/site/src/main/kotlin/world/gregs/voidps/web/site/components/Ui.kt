package world.gregs.voidps.web.site.components

import kotlinx.html.FlowContent

/**
 * Namespace for the Void design-system components (`ui.button`, `ui.panel`, ...), so call sites
 * read as generic UI components without colliding with kotlinx.html's own tag builders
 * (`button`, `select`, `progress`, `dialog`, ...).
 */
class Ui(internal val receiver: FlowContent)

val FlowContent.ui: Ui get() = Ui(this)
