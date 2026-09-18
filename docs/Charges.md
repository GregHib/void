Sometimes charges can be associated with a players' item. There are three main ways of tracking item charges:

| Name | Description | Item effect |
|----|----|----|
| Item level | Charge is reflected in the items id and name e.g. `black_mask_8`. | Reducing a charge to zero replaces the item. |
| Player level | Charge is stored as a player variable e.g. `ring_of_recoil`. | No effect on the item. |
| Inventory level | Charge is stored per individual item in an inventories slot. | No effect on the item. |

An item charge being reduced to zero can:
  1. Destroy (remove) the item
  2. Replaced the item e.g. `chaotic_rapier_broken`
  3. Do nothing


```toml
[item]
id = 1234
charges = 12345         # maximum number of charges
charge_start = 50       # new item starts with 0 charges
deplete = "combat"      # what causes the item charges to degrade
degrade = "item_broken" # the item to degrade into once reached 0 charges
degrade_message = "Your item broke." # message to send after degraded
```