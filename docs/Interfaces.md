Interfaces are used to display information to a player, whether that's a picture, rectangle, line of text, or a button to click, each interface is made up of a list of different types of components.

## Component types

| Name | Description |
|---|---|
| Container | Parent to a group of child components. |
| Rectangle | Filled or just the outline. |
| Text | Line or paragraph of text. |
| Sprite | Image, picture or icon. |
| Model | Rendered model of a character, object or item. |
| Line | Thin or thick with a colour. |

Due to the nested nature of containers, interfaces can be inserted into one another to create a hierarchy. The majority of interfaces are used inside one another, one that are not we will refer to as full-screen interfaces.

![Screenshot 2024-02-16 175117](https://github.com/GregHib/void/assets/5911414/df6e3a45-1bd3-403e-b716-df28430bde84)

## Fullscreen
Full-screen interfaces as the name suggests, take up the entire client screen, there can only be one full screen interface displayed at a time.

Examples of fullscreen interfaces would be:
* Login interface
* Fixed or resizable Gameframe
* World map

![Screenshot 2024-02-16 174825](https://github.com/GregHib/void/assets/5911414/64a58a52-c891-40e2-9263-ce854568568c)

## Gameframe interfaces

The majority of gameplay resolves around the fixed and resizable "gameframe" interfaces, known by jagex as `toplevel` and `toplevel_full` respectively.

The Gameframe interface is split up into multiple areas to place other interfaces into:

![Screenshot 2024-02-16 174118](https://github.com/GregHib/void/assets/5911414/c647b385-6228-4e3f-b35a-b590b495c602)

### Main screen & Menus (Blue)

The main game screen is used primarily for displaying large interfaces which block the players view.

* Settings
* Bank
* Equipment bonuses

![Screenshot 2024-02-17 172526](https://github.com/GregHib/void/assets/5911414/6dd25bed-91a7-497d-8b89-9691f9b5a09f)


#### Overlays

Overlays are smaller interfaces for displaying contextual information during activities and minigames.

* Godwars kills
* Bounty hunter info
* Wilderness level

![Screenshot 2024-02-17 173211](https://github.com/GregHib/void/assets/5911414/75b8878c-b01a-4324-acba-24b3ef96aaf7)

### Chat screen & Dialogues (Green)

The chat screen is where communication and input interfaces are displayed

* Chat
* Quick chat
* Text input

![Screenshot 2024-02-17 173310](https://github.com/GregHib/void/assets/5911414/64a24cfc-9ef5-4b2d-a404-e9d443fe30af)


### Tabs & Side interfaces (Orange)

Tab interfaces are always avaiable for players to use and interact with their player

* Inventory
* Spellbook
* Logout

![Screenshot 2024-02-17 173446](https://github.com/GregHib/void/assets/5911414/bc564d3e-48c0-45ee-845a-244573ffdd1d)

## Context menus

The pop-up menu displayed when right click on interfaces is known as the context or right-click menu, it is the list of potential actions the player can take.

![Screenshot 2024-02-17 175012](https://github.com/GregHib/void/assets/5911414/d51de257-fbdb-4a9e-b941-70bc82751f95)

# Implementation

Interfaces are defined in `*.ifaces.toml` files and types in `interface_types.toml`.

```toml
[price_checker]                # Name of the interface to be used as an id
id = 206                       # The interface id
type = "main_screen"           # The type of interface - location to attach the interface - interface_types.toml

[.overall]                     # Text component name
id = 20                        # Text component id

[.total]
id = 21

[.limit]
id = 22

[.items]                       # A container component
id = 18                        # Components id
inventory = "trade_offer"      # The id of the item container linked to this component
options = {                    # Item right click options
  Remove-1 = 0,                # Option and the index in the context menu
  Remove-5 = 1,
  Remove-10 = 2,
  Remove-All = 3,
  Remove-X = 4,
  Examine = 9
} 
```

## Clicking

Execute code when a player clicks on a certain components option

```kotlin
//               Option, component, interface
interfaceOption("Remove-5", "price_checker:items") {
  // ...
}
```

## Opening

Interfaces can be opened for a player using the interface id

```kotlin
player.open("price_checker")
```

You can also subscribe using [events](Events) in order to do things when interfaces are opened

```kotlin
interfaceOpen("price_checker") {
  // ...
}
```

## Closing

Closing an specific interface can be done, however it's normally done by type
```kotlin
player.close("price_checker")
```

```kotlin
player.closeMenu() // Close whatever menu is open (if any)
player.closeDialogue() // Close any dialogues open
player.closeInterfaces() // Both menu and dialogues
```

Code can also be executed on closing an interface
```kotlin
interfaceClose("price_checker") {
  // ...
}
```

## Modifying

* Model Animations
* Text
* Visibility (show/hide)
* Sprite
* Colour
* Item

```kotlin
// Interface, component, hidden
player.interfaces.sendVisibility("price_checker", "limit", false)
```

* Send inventory
* Unlock inventory options
* Lock inventory options

```kotlin
// Interface, component, item slot range
player.interfaceOptions.unlockAll("price_checker", "items", 0 until 28)
```