This mod features a handful of Storage Boxes, each with different sized inventories as well as a handful of new upgradable backpacks.

# Storage Boxes
- Industrial Iron, Andesite, Copper, Brass and Hardened Storage Boxes
- Each has a filter slot on the front to filter which items can be added or taken from
- Features a screen on the front of each box showing how full the contents is
- Each box has an indicator light to show if the box is full or empty
- Boxes can be interacted with by the player to add or take items without having to open the GUI

# Simple Storage Boxes
- Common Wood Type Varieties (all have same attributes)
- Each can hold 2048* items as default
- Can only hold one type of item
- Void Ability
  - To apply void upgrade: interact with a Simple Storage Void Upgrade
  - To remove void upgrade: interact with a Simple Storage Void Upgrade
    - when box has Void Upgrade
    - or remove via the menu
- Capacity Upgrades
  - Capacity can be increased with Simple Storage Capacity Upgrades
  - Max 9 Capacity Upgrades can be added
  - Each capacity upgrade doubles current capacity
  - Capacity Upgrades can be removed using the menu
- Filter is set automatically when items are added
  - To remove Filter: interact with a Wrench (only when box is empty)
- Menu can be opened by interacting with an empty hand while sneaking

*Note: Capacity is 32x Max Stack Size of items contained.*  
*For Example: Logs will have a capacity of 2048, Ender Pearls will limit capacity to 512 and Buckets of Water will limit capacity to 32*

## Simple Storage Box Upgrades
### Void Upgrade:
- Once Simple Storage Box is full, additional items inserted will be voided (deleted)
### Capacity Upgrade:
- Doubles the current capacity of the Simple Storage Box

# Backpacks
- Industrial Iron, Andesite, Copper, Brass and Hardened Backpacks
- Each backpack can hold 6 upgrades which affect how the backpack works when worn
- Different variations of the backpack hold different amounts of items per slot
- Each has 3 storage compartments:
  - Main Storage your bulk items and can be interacted with by hoppers / chutes etc.
  - A safe Tool Storage compartment for your tools and precious items (cannot be interacted with by hoppers)
  - Upgrade Slots

## Backpack Upgrades
### Magnet Upgrade:
Works when worn on the player's back or placed on the floor
Pulls item into the backpack's main inventory compartment from up to 5 blocks away

### Item Pickup Upgrade:
Similar to the Magnet upgrade but only works when the back pack is worn
When the player touches an item entity, this puts the items into the backpack instead of the player inventory

### Pick Block Upgrade:
Pick block items from your backpack

### Refill Upgrade:
Refills the main hand or off-hand items from the backpack if they're available

### Tool Swap Upgrade:
Swaps out any tool held in the player's main hand for the best available tool or weapon when mining a block or hitting an entity

### Feeder Upgrade:
Automatically feeds the player fro the backpack when the player is hungry enough to eat

### Jetpack Upgrade:
Turns any backpack into a fully functional jetpack with flight and hovering abilities.
Uses Create Backtankss for fuel (must be inside the backpack)

### Fall Damage Upgrade:
Prevents the player taking fall damage while wearing the backpack

# Passer Blocks
## Basic Passer
- Passes items from one container to another
- Has no intermediary storage or inventory
- Can pass items horizontally and vertically depending on rotation
- Can be rotated in all directions using a Wrench
- Will transfer 1 item at a time (like a hopper)
## Smart Passer
- Can be filtered using Create filters
- Can transfer up to 64 items at a time
- Amount to pass is selectable using Create interface. (Hold filter slot and change amount)
