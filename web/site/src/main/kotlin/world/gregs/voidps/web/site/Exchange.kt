package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The Grand Exchange: a market overview, an item search, and a per-item detail page with a
 * price history chart. The stat tiles, filter chips and panel chrome are plain Kotlin + [ui]
 * components since the layout is known at build time; the item dataset, price/volume math and
 * SVG chart geometry live in `void/exchange.js` (`exchangeApp()`) and are emitted below as
 * `<template x-for>` blocks, mirroring the pattern in [Hiscores].
 */
object Exchange {

    fun page(): String = voidPage(
        title = "Void — grand exchange",
        description = "Live buy and sell prices for every tracked item on Void, sampled every five minutes.",
        head = { script(src = "void/exchange.js") {} },
    ) {
        ui.siteHeader(Website.pages, active = "exchange", communityPages = Website.communityPages)

        div {
            xData("exchangeApp()")
            attributes["x-init"] = "init()"

            homeView()
            searchView()
            itemView()
        }

        ui.siteFooter()
    }

    private fun eyebrowText(expression: String): FlowContent.() -> Unit = {
        span {
            style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
            attributes["x-text"] = expression
        }
    }

    /** [align] is `"left"`, `"right"` or `"center"`; a column keeps its own width from [Column.width]. */
    private data class Column(val label: String, val width: String, val align: String = "left")

    /** Mirrors the category list baked into `exchange.js` (`CAT_BORDER`/`CAT_CODE`/`CAT_BLURB`) — keep in sync. */
    private val categories = listOf("All", "Weapons", "Armour", "Runes", "Consumables", "Resources", "Curios")

    /** Mirrors the `TFS` keys in `exchange.js` — keep in sync. */
    private val timeframes = listOf("24H", "7D", "30D", "1Y", "All")

    /** Compact sort dropdown for the results panel header — options mirror `sort` in `exchangeApp()`. */
    private fun FlowContent.sortSelect() {
        div {
            style = "position:relative"
            select {
                attributes["id"] = "ge-sort"
                xModel("sort")
                style = "width:158px;height:26px;padding:0 var(--space-8) 0 10px;appearance:none;cursor:pointer;color-scheme:dark;" +
                    "background:var(--umber-800);color:var(--text-muted);border:1px solid var(--border-strong);" +
                    "border-radius:var(--radius-sm);font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);" +
                    "letter-spacing:var(--tracking-wide)"
                option { value = "vol"; +"Volume traded" }
                option { value = "price"; +"Highest price" }
                option { value = "gain"; +"Biggest gain" }
                option { value = "loss"; +"Biggest fall" }
                option { value = "name"; +"Name A–Z" }
            }
            span {
                attributes["aria-hidden"] = "true"
                style = "position:absolute;right:8px;top:50%;transform:translateY(-50%);" +
                    "pointer-events:none;color:var(--text-faint);font:10px var(--font-ui)"
                +"▾"
            }
        }
    }

    /** A pill-chip filter button, styled and toggled the same way as [Hiscores]'s mode/team chips. */
    private fun FlowContent.chip(label: String, activeExpression: String, onClick: String) {
        button {
            onClick(onClick)
            xToggleStyle(
                condition = activeExpression,
                whenTrue = "background:rgba(224,174,60,.14);color:var(--gold-300);border-color:var(--gold-600)",
                whenFalse = "background:var(--umber-800);color:var(--text-muted);border-color:var(--border-strong)",
            )
            style = "height:28px;padding:0 var(--space-5);border-radius:var(--radius-pill);cursor:pointer;" +
                "font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);letter-spacing:var(--tracking-wide);" +
                "background:var(--umber-800);color:var(--text-muted);border:1px solid var(--border-strong)"
            +label
        }
    }

    private fun FlowContent.tableHeader(vararg columns: Column) {
        div {
            style = "display:grid;grid-template-columns:${columns.joinToString(" ") { it.width }};" +
                "padding:var(--space-4) var(--space-6);background:var(--umber-900);border-bottom:1px solid var(--border-panel);" +
                "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
            for (column in columns) {
                span {
                    if (column.align != "left") {
                        style = "text-align:${column.align}"
                    }
                    +column.label
                }
            }
        }
    }

    /** Coloured, monospace item-code tile — a stand-in for a real item sprite. [size] is CSS pixels. */
    private fun DIV.itemCodeTemplate(borderExpr: String, codeExpr: String, size: Int) {
        unsafe {
            raw(
                """
                <div :style="{ borderColor: $borderExpr }" style="width:${size}px;height:${size}px;flex:none;display:flex;align-items:center;justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down)">
                  <span style="font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)" x-text="$codeExpr"></span>
                </div>
                """.trimIndent(),
            )
        }
    }

    private fun DIV.rowListTemplate(listExpr: String) {
        unsafe {
            raw(
                """
                <template x-for="r in $listExpr" :key="r.id">
                  <div @click="open(r.id)" style="display:grid;grid-template-columns:36px minmax(0,1fr) auto;align-items:center;gap:14px;padding:var(--space-5) var(--space-6);border-bottom:1px solid var(--umber-900);cursor:pointer;transition:background var(--dur-fast) var(--ease-standard)">
                    <div :style="{ borderColor: r.border }" style="width:36px;height:36px;display:flex;align-items:center;justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down)">
                      <span style="font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)" x-text="r.code"></span>
                    </div>
                    <div style="display:flex;flex-direction:column;gap:var(--space-2);min-width:0">
                      <span style="font:var(--weight-semibold) var(--text-base)/1.2 var(--font-ui);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="r.name"></span>
                      <span style="font:var(--type-body-sm);font-size:var(--text-xs);color:var(--text-faint)" x-text="r.cat"></span>
                    </div>
                    <div style="display:flex;flex-direction:column;gap:var(--space-2);align-items:flex-end">
                      <span :style="{ color: r.m1Color }" style="font:var(--type-code);font-size:var(--text-base)" x-text="r.m1"></span>
                      <span style="font:var(--type-code);font-size:var(--text-xs);color:var(--text-faint)" x-text="r.m2"></span>
                    </div>
                  </div>
                </template>
                """.trimIndent(),
            )
        }
    }

    private fun FlowContent.homeView() {
        div {
            xShow("page === 'home'")

            section {
                style = "border-bottom:1px solid var(--border-panel);background:var(--surface-inset)"
                div {
                    style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-11) var(--space-7) var(--space-9);" +
                        "display:flex;flex-direction:column;align-items:center;gap:var(--space-6);text-align:center"
                    span {
                        style = "display:block;width:100%;text-align:center;font:var(--type-label);" +
                            "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--gold-300)"
                        +"Market overview"
                    }
                    h1 {
                        style = "margin:0;font:var(--type-hero);color:var(--text-strong);text-wrap:pretty"
                        +"Grand Exchange"
                    }
                    p {
                        style = "margin:0;max-width:56ch;font:var(--type-body);color:var(--text-muted);text-wrap:pretty"
                        +("Buy and sell prices from every Void world, sampled every five minutes. " +
                            "Prices update live on this page.")
                    }
                    div {
                        style = "display:flex;gap:var(--space-5);align-items:flex-end;width:100%;max-width:560px;margin-top:var(--space-2)"
                        div {
                            style = "flex:1;min-width:0"
                            ui.textInput("ge-search", "Search items", model = "q", placeholder = "Search 26 tracked items", icon = Icons.SEARCH, onEnter = "goSearch()")
                        }
                        ui.button("Search", size = ButtonSize.Medium, glow = true, onClick = "goSearch()")
                    }
                }
            }

            div {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-12);" +
                    "display:flex;flex-direction:column;gap:var(--space-7)"

                div {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:var(--space-6)"
                    unsafe {
                        raw(
                            """
                            <template x-for="s in summary" :key="s.label">
                              <div style="background:var(--surface-panel);border:1px solid var(--border-panel);border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-xs);padding:var(--space-6) var(--space-7);display:flex;flex-direction:column;gap:var(--space-4)">
                                <span style="font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)" x-text="s.label"></span>
                                <span style="font:var(--weight-semibold) var(--text-3xl)/1 var(--font-display);color:var(--text-strong)" x-text="s.value"></span>
                                <span :style="{ color: s.color }" style="font:var(--type-body-sm)" x-text="s.note"></span>
                              </div>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }

                div {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(420px,1fr));gap:var(--space-7)"

                    ui.panel(title = "Top volume", action = eyebrowText("'by gp traded, 24h'"), padded = false) {
                        rowListTemplate("topVolume")
                    }
                    ui.panel(title = "Biggest risers", action = eyebrowText("'today'"), padded = false) {
                        rowListTemplate("risers")
                    }
                    ui.panel(title = "Biggest fallers", action = eyebrowText("'today'"), padded = false) {
                        rowListTemplate("fallers")
                    }
                    ui.panel(title = "Most expensive", action = eyebrowText("'guide price'"), padded = false) {
                        rowListTemplate("mostExpensive")
                    }
                }
            }
        }
    }

    private fun FlowContent.searchView() {
        div {
            xShow("page === 'search'")
            attributes["class"] = "void-flex"
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-12);" +
                "flex-direction:column;gap:var(--space-6)"

            div {
                h1 {
                    style = "margin:0;font:var(--type-title);color:var(--text-strong)"
                    +"Find an item"
                }
                p {
                    style = "margin:var(--space-4) 0 0;font:var(--type-body-sm);color:var(--text-muted)"
                    attributes["x-text"] = "results.length + ' of 26 tracked items · prices sampled 5 minutes ago'"
                }
            }

            div {
                style = "width:320px;max-width:100%"
                ui.textInput("ge-search-2", "Item name", model = "q", placeholder = "Item name", icon = Icons.SEARCH)
            }

            div {
                style = "display:flex;flex-wrap:wrap;gap:var(--space-3);padding:var(--space-3)"
                for (c in categories) {
                    chip(c, "cat === '$c'", "cat = '$c'")
                }
            }

            ui.panel(title = "Results", action = { sortSelect() }, padded = false) {
                tableHeader(
                    Column("", "48px"), Column("Item", "minmax(0,1fr)"),
                    Column("Buy price", "140px", "right"), Column("24h", "120px", "right"),
                    Column("Volume", "130px", "right"), Column("Limit", "90px", "right"),
                )
                unsafe {
                    raw(
                        """
                        <template x-for="r in results" :key="r.id">
                          <div @click="open(r.id)" style="display:grid;grid-template-columns:48px minmax(0,1fr) 140px 120px 130px 90px;gap:var(--space-6);align-items:center;padding:var(--space-5) var(--space-7);border-bottom:1px solid var(--umber-900);cursor:pointer">
                            <div :style="{ borderColor: r.border }" style="width:36px;height:36px;display:flex;align-items:center;justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down)">
                              <span style="font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)" x-text="r.code"></span>
                            </div>
                            <div style="display:flex;flex-direction:column;gap:var(--space-2);min-width:0">
                              <span style="font:var(--weight-semibold) var(--text-base)/1.2 var(--font-ui);color:var(--text-strong)" x-text="r.name"></span>
                              <span style="font:var(--type-body-sm);font-size:var(--text-xs);color:var(--text-faint);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="r.examine"></span>
                            </div>
                            <span style="font:var(--type-code);text-align:right;color:var(--text-strong)" x-text="r.price"></span>
                            <span :style="{ color: r.deltaColor }" style="font:var(--type-code);text-align:right" x-text="r.delta"></span>
                            <span style="font:var(--type-code);text-align:right;color:var(--text-muted)" x-text="r.vol"></span>
                            <span style="font:var(--type-code);text-align:right;color:var(--text-faint)" x-text="r.limit"></span>
                          </div>
                        </template>
                        """.trimIndent(),
                    )
                }
                div {
                    xShow("results.length === 0")
                    style = "padding:var(--space-11) var(--space-6);text-align:center;font:var(--type-body);color:var(--text-faint)"
                    +"No item matches that name. Try a shorter query."
                }
            }
        }
    }

    private fun FlowContent.itemView() {
        div {
            xShow("page === 'item'")
            attributes["class"] = "void-flex"
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-7) var(--space-7) var(--space-12);" +
                "flex-direction:column;gap:var(--space-6)"

            div {
                style = "display:flex;gap:var(--space-4);align-items:center;font:var(--type-body-sm);color:var(--text-faint)"
                a(href = "#") { onClick("goHome()"); +"Market" }
                span { +"→" }
                a(href = "#") { onClick("goSearch()"); attributes["x-text"] = "item.cat" }
                span { +"→" }
                span { style = "color:var(--text-muted)"; attributes["x-text"] = "item.name" }
            }

            div {
                style = "display:flex;flex-wrap:wrap;gap:var(--space-7);align-items:flex-start;justify-content:space-between;" +
                    "padding:var(--space-5) 0"
                div {
                    style = "display:flex;gap:var(--space-6);align-items:flex-start;min-width:0"
                    itemCodeTemplate("item.border", "item.code", 64)
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-4);min-width:0"
                        h1 {
                            style = "margin:0;font:var(--type-title);color:var(--text-strong)"
                            attributes["x-text"] = "item.name"
                        }
                        div {
                            style = "display:flex;gap:var(--space-3);flex-wrap:wrap"
                            span {
                                style = "font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
                                    "letter-spacing:var(--tracking-caps);text-transform:uppercase;padding:0 10px;height:20px;" +
                                    "display:inline-flex;align-items:center;background:var(--umber-700);color:var(--parch-200);" +
                                    "border:1px solid var(--border-strong);border-radius:var(--radius-pill)"
                                attributes["x-text"] = "item.cat"
                            }
                            span {
                                style = "display:inline-flex;align-items:center;gap:var(--space-3);padding:0 10px;height:20px;" +
                                    "border:1px solid var(--border-strong);border-radius:var(--radius-pill);" +
                                    "font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
                                    "letter-spacing:var(--tracking-caps);text-transform:uppercase;justify-self:start"
                                attributes["x-bind:style"] = "{ background: item.memberBg, color: item.memberColor, borderColor: item.memberBorder }"
                                attributes["x-text"] = "item.memberLabel"
                            }
                            ui.badge("Trading", tone = BadgeTone.Info, dot = true)
                        }
                        p {
                            style = "margin:0;max-width:62ch;font:var(--type-body);color:var(--text-muted);text-wrap:pretty"
                            attributes["x-text"] = "item.desc"
                        }
                        p {
                            style = "margin:0;font:var(--type-body-sm);font-style:italic;color:var(--text-faint)"
                            attributes["x-text"] = "'“' + item.examine + '”'"
                        }
                    }
                }
                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-3);align-items:flex-end"
                    span {
                        style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
                        +"Guide price"
                    }
                    span {
                        style = "font:var(--weight-bold) var(--text-4xl)/1 var(--font-display);color:var(--gold-300)"
                        attributes["x-text"] = "item.price"
                    }
                    span {
                        style = "font:var(--type-code)"
                        attributes["x-bind:style"] = "{ color: item.deltaColor }"
                        attributes["x-text"] = "item.delta + ' today'"
                    }
                }
            }

            ui.panel(title = "Price history", action = eyebrowText("chartData.eyebrow"), padded = false) {
                div {
                    style = "display:flex;flex-wrap:wrap;gap:var(--space-7);align-items:center;justify-content:space-between;padding:var(--space-6) var(--space-7);border-bottom:1px solid var(--umber-900)"
                    unsafe {
                        raw(
                            """
                            <div style="display:flex;flex-direction:column;gap:var(--space-2)">
                              <span style="font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)" x-text="chartData.stamp"></span>
                              <div style="display:flex;gap:18px;align-items:baseline">
                                <span style="display:flex;align-items:center;gap:var(--space-3);font:var(--type-code);color:var(--gold-300)"><span style="width:10px;height:2px;background:var(--gold-400);display:inline-block"></span>Buy <span x-text="chartData.buy"></span></span>
                                <span style="display:flex;align-items:center;gap:var(--space-3);font:var(--type-code);color:var(--steel-500)"><span style="width:10px;height:2px;background:var(--steel-500);display:inline-block"></span>Sell <span x-text="chartData.sell"></span></span>
                                <span style="font:var(--type-code);color:var(--text-muted)">Vol <span x-text="chartData.vol"></span></span>
                              </div>
                            </div>
                            """.trimIndent(),
                        )
                    }
                    div {
                        style = "display:flex;gap:var(--space-3)"
                        for (t in timeframes) {
                            chip(t, "tf === '$t'", "tf = '$t'; hover = null")
                        }
                    }
                }

                unsafe {
                    raw(
                        """
                        <div style="padding:var(--space-6) var(--space-7) var(--space-4)" @mousemove="onChartMove(${'$'}event)" @mouseleave="onChartLeave()">
                          <div style="position:relative">
                            <svg viewBox="0 0 920 300" width="100%" preserveAspectRatio="xMidYMid meet" style="display:block;overflow:visible">
                              <g x-html="chartData.gridSvg"></g>
                              <path :d="chartData.band" style="fill:rgba(224,174,60,.09)"></path>
                              <path :d="chartData.sellPath" style="fill:none;stroke:var(--steel-500);stroke-width:1.75"></path>
                              <path :d="chartData.buyPath" style="fill:none;stroke:var(--gold-400);stroke-width:2"></path>
                              <g x-show="chartData.hovering">
                                <line :x1="chartData.hx" :x2="chartData.hx" y1="10" y2="272" style="stroke:var(--gold-300);stroke-width:1;stroke-dasharray:3 4"></line>
                                <circle :cx="chartData.hx" :cy="chartData.hyBuy" r="3.5" style="fill:var(--gold-300)"></circle>
                                <circle :cx="chartData.hx" :cy="chartData.hySell" r="3.5" style="fill:var(--steel-500)"></circle>
                              </g>
                            </svg>
                            <template x-for="g in chartData.grid" :key="'lbl'+g.label+g.top">
                              <div style="position:absolute;left:0;width:6.3%;text-align:right;transform:translateY(-50%);font:var(--type-code);font-size:11px;color:var(--text-faint);pointer-events:none" :style="{ top: g.top }" x-text="g.label"></div>
                            </template>
                            <template x-for="l in chartData.xlabels" :key="'x'+l.label+l.left">
                              <div style="position:absolute;transform:translateX(-50%);font:var(--type-code);font-size:11px;color:var(--text-faint);pointer-events:none;white-space:nowrap;bottom:1%" :style="{ left: l.left }" x-text="l.label"></div>
                            </template>
                            <div x-show="chartData.hovering" style="position:absolute;transform:translate(-50%,calc(-100% - 10px));z-index:5;padding:var(--space-3) 10px;background:var(--umber-950);border:1px solid var(--border-gold);border-radius:var(--radius-xs);box-shadow:var(--shadow-md);font:var(--type-code);font-size:11px;white-space:nowrap;pointer-events:none" :style="{ left: chartData.hoverLeft, top: chartData.hoverTop }">
                              <div style="color:var(--text-faint);margin-bottom:var(--space-2)" x-text="chartData.stamp"></div>
                              <div style="display:flex;align-items:center;gap:var(--space-3);color:var(--gold-300)"><span style="width:8px;height:2px;background:var(--gold-400);display:inline-block"></span>Buy <span x-text="chartData.buy"></span></div>
                              <div style="display:flex;align-items:center;gap:var(--space-3);color:var(--steel-500)"><span style="width:8px;height:2px;background:var(--steel-500);display:inline-block"></span>Sell <span x-text="chartData.sell"></span></div>
                            </div>
                          </div>
                          <div>
                            <span style="display:block;font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint);padding:var(--space-4) 0 var(--space-2) 66px">Units traded</span>
                            <svg viewBox="0 0 920 74" width="100%" preserveAspectRatio="xMidYMid meet" style="display:block">
                              <g x-html="chartData.barsSvg"></g>
                              <line x1="66" x2="908" y1="66" y2="66" style="stroke:var(--umber-700);stroke-width:1"></line>
                            </svg>
                          </div>
                        </div>
                        """.trimIndent(),
                    )
                }
            }

            div {
                style = "display:grid;grid-template-columns:minmax(0,1fr) 300px;gap:var(--space-7);align-items:start"

                ui.panel(title = "Trade data", padded = false) {
                    div {
                        style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr))"
                        unsafe {
                            raw(
                                """
                                <template x-for="s in stats" :key="s.label">
                                  <div style="display:flex;flex-direction:column;gap:var(--space-3);padding:var(--space-6) var(--space-7);border-right:1px solid var(--umber-900);border-bottom:1px solid var(--umber-900)">
                                    <span style="font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)" x-text="s.label"></span>
                                    <span style="font:var(--type-code);font-size:var(--text-lg);color:var(--text-strong)" x-text="s.value"></span>
                                    <span style="font:var(--type-body-sm);font-size:var(--text-xs);color:var(--text-faint)" x-text="s.note"></span>
                                  </div>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                }

                ui.panel(title = "Related", action = eyebrowText("item.cat"), padded = false) {
                    unsafe {
                        raw(
                            """
                            <template x-for="r in related" :key="r.id">
                              <div @click="open(r.id)" style="display:grid;grid-template-columns:36px minmax(0,1fr) auto;gap:var(--space-5);align-items:center;padding:var(--space-5) var(--space-6);border-bottom:1px solid var(--umber-900);cursor:pointer">
                                <div :style="{ borderColor: r.border }" style="width:36px;height:36px;display:flex;align-items:center;justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down)">
                                  <span style="font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)" x-text="r.code"></span>
                                </div>
                                <span style="font:var(--type-body-sm);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="r.name"></span>
                                <span :style="{ color: r.deltaColor }" style="font:var(--type-code);font-size:var(--text-xs)" x-text="r.delta"></span>
                              </div>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
            }
        }
    }
}
