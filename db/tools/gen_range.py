"""Build src/main/resources/static/data/range.json for the shooting range page.

Inputs (from the earlier data pipeline, see db/README.md):
  cargo.json        - dayz.wiki.gg Cargo tables (Ammunition, Clothing, Ranged)
  dataset.json      - the curated item set that is loaded into the database
  image_paths.json  - "section|English name" -> /img/items/... path

Usage: python gen_range.py <dir with the three json files> <output json>
"""
import json
import re
import sys
from pathlib import Path

src, out = Path(sys.argv[1]), Path(sys.argv[2])
cargo = json.loads((src / "cargo.json").read_text(encoding="utf-8"))
data = json.loads((src / "dataset.json").read_text(encoding="utf-8"))
images = json.loads((src / "image_paths.json").read_text(encoding="utf-8"))


def num(v, default=None):
    try:
        return float(str(v).strip())
    except (TypeError, ValueError):
        return default


def clean(s):
    return re.sub(r"&#39;", "'", s or "").strip()


# ---------- ammunition ----------
PELLETS = {"12ga 00 Buckshots": 8}  # 00 buck fires 8 pellets; damage values are per pellet
ammo = {}
for r in cargo["Ammunition"]:
    name = clean(r["Name"])
    h, b, s = num(r.get("healthDmg")), num(r.get("bloodDmg")), num(r.get("shockDmg"))
    if h is None or s is None or r.get("Category") in ("Grenade", "Planned", "Legacy"):
        continue
    ammo[name] = {
        "name": name,
        "health": h, "blood": b or 0, "shock": s,
        "speed": num(r.get("initSpeed"), 0), "friction": num(r.get("Friction"), 0),
        "pellets": PELLETS.get(name, 1),
        "image": images.get("ammunition|" + name),
    }

# ---------- weapons ----------
ranged = {clean(r["Name"]): r for r in cargo["Ranged"]}
weapons = []
for w in data["weapons"]:
    r = ranged.get(w["englishName"])
    if not r:
        continue
    names = [a.strip() for a in clean(r.get("Ammunition")).split(",")]
    names = [a for a in names if a in ammo]  # tracers share stats with the base round
    if not names:
        continue
    weapons.append({
        "name": w["englishName"], "type": clean(r.get("Category")) or w.get("type"),
        "modes": clean(r.get("Mode")).replace("<br />", ", "),
        "ammo": names, "image": images.get("weapons|" + w["englishName"]),
    })
weapons.sort(key=lambda w: w["name"].lower())

# ---------- clothing ----------
SLOT = {"Head": "head", "Headgear": "head", "Head, Face": "head", "Face": "face",
        "Top": "top", "Body": "top", "Vest": "vest", "Gloves": "gloves", "Legs": "legs", "Feet": "feet"}
current = {a["englishName"] for a in data["armors"]}
gear = {k: [] for k in ("head", "face", "vest", "top", "gloves", "legs", "feet")}
for r in cargo["Clothing"]:
    name = clean(r["Name"])
    slot = SLOT.get((r.get("inventorySlot") or "").strip())
    hp = num(r.get("Durability"))
    if not slot or name not in current or not hp:
        continue
    gear[slot].append({
        "name": name, "hp": hp,
        "h": num(r.get("projH"), 1), "b": num(r.get("projB"), 1), "s": num(r.get("projS"), 1),
        "image": images.get("armors|" + name),
    })
for items in gear.values():
    # armored items first, then alphabetical
    items.sort(key=lambda g: (g["h"] >= 1 and g["b"] >= 1, g["name"].lower()))

out.write_text(json.dumps({"ammo": ammo, "weapons": weapons, "gear": gear}, ensure_ascii=False, indent=1), encoding="utf-8")
print("ammo", len(ammo), "weapons", len(weapons), {k: len(v) for k, v in gear.items()})
