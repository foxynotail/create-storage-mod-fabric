# Mod Features: Storage Boxes and Backpacks

This mod introduces a variety of **Storage Boxes** and **Backpacks**, each with unique functionalities and upgrade options.

---

## 📦 Storage Boxes
### Industrial Storage Boxes
- **Types:** Industrial Iron, Andesite, Copper, Brass, and Hardened Storage Boxes.
- **Key Features:**
  - Filter slot on the front for controlling item addition and removal.
  - Screen displaying the current fill level.
  - Indicator lights signal full or empty status.
  - Player-interactive; add or remove items without opening the GUI.

---

### Simple Storage Boxes
- **Types:** Common wood varieties (same attributes across types).
- **Base Capacity:** Holds up to **2048 items** by default (one item type only).
- **Special Abilities:**
  - **Void Upgrade:** Deletes excess items when box is full.
    - Apply/remove using the **Void Upgrade item** or the menu.
  - **Capacity Upgrades:**
    - Add up to 9 upgrades; each doubles current capacity.
    - Apply/remove via the menu.
  - **Filter Functionality:**
    - Automatically set upon item addition.
    - Remove filter with a **Wrench** (box must be empty).
  - **Menu Access:** Sneak + empty hand to open.

*Note: Capacity depends on item stack size.*
- **Examples:** Logs (2048), Ender Pearls (512), Water Buckets (32).

---

### 🔧 Simple Storage Box Upgrades
- **Void Upgrade:** Deletes excess items upon reaching full capacity.
- **Capacity Upgrade:** Doubles the box’s current capacity.

---

## 🏗️ Storage Trim / Casing
- **Purpose:** Connects **Simple Storage Boxes** with controllers and interface blocks.
- **Design:** Matches Simple Storage Box variations with connected textures.

---

## 🌐 Storage Networks
### Simple Storage Controller
- **Functionality:**
  - Master input/output for a **Storage Network**.
  - Connects via **Storage Trim** to create networks.
- **Interactions:**
  - Players: Insert/extract items manually.
  - Devices: Hoppers, chutes, funnels, etc.

### Simple Storage Interface
- **Key Differences:**
  - No direct player interaction.
  - Doesn't create a network (requires a Controller).
  - Supports item transfer via devices like hoppers.

---

## 🎒 Backpacks
### Variants
- **Types:** Industrial Iron, Andesite, Copper, Brass, and Hardened Backpacks.
- **Storage:**
  - **Main Storage:** Bulk items (hopper-compatible).
  - **Tool Storage:** Safe storage for tools (not hopper-compatible).
  - **Upgrade Slots:** Up to 6 upgrades per backpack.

---

### 🚀 Backpack Upgrades
1. **Magnet Upgrade:** Pulls items into the main compartment from 5 blocks away.
2. **Item Pickup Upgrade:** Transfers items directly into the backpack when worn.
3. **Pick Block Upgrade:** Allows pick-blocking from the backpack.
4. **Refill Upgrade:** Refills main/off-hand items from the backpack.
5. **Tool Swap Upgrade:** Automatically equips the best tool or weapon.
6. **Feeder Upgrade:** Automatically feeds the player when hungry.
7. **Jetpack Upgrade:** Converts backpack into a jetpack (uses Create Backtanks for fuel).
8. **Fall Damage Upgrade:** Prevents fall damage while wearing the backpack.

---

## 🔄 Passer Blocks
### Basic Passer
- Passes items between containers (no storage).
- Transfers items horizontally or vertically (rotatable with a Wrench).
- Moves 1 item per transfer (like a hopper).

### Smart Passer
- **Features:**
  - Filterable using **Create filters**.
  - Adjustable transfer amounts (up to 64 items).
- **Configuration:** Set amounts via the Create interface.

---
