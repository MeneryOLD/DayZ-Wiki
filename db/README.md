# Wiki data

`seed.sql` fills the item tables and creates the `full_text_search` view used by search.
It is current for **DayZ 1.30 (Badlands)**: only items that spawn in the official loot economy,
can be crafted, or come from animals/nature are included.

Load it after the app has started once (so Hibernate has created the tables):

    mysql -uroot -p --default-character-set=utf8mb4 dayzwiki < db/seed.sql

## Sources
- Spawn maps, loot tiers, class names: official Bohemia Interactive repo
  `BohemiaInteractive/DayZ-Central-Economy` (types.xml / events.xml for all four maps).
- Stats and in-game descriptions: dayz.wiki.gg (Cargo tables + item infoboxes), CC BY-SA.
- 1.30 content not yet on the wiki (motorbikes, Silicosis, Irritated Eyes, some Nasdara food and clothing):
  `tools/manual.json`, written from the 1.30 patch notes.

## Regenerating after a game update
Run from an empty work folder (W):

    git clone --depth 1 https://github.com/BohemiaInteractive/DayZ-Central-Economy.git W/ce
    python tools/parse_types.py W/types.json chernarus=W/ce/dayzOffline.chernarusplus/db/types.xml livonia=W/ce/dayzOffline.enoch/db/types.xml sakhal=W/ce/dayzOffline.sakhal/db/types.xml nasdara=W/ce/dayzOffline.nasdara/db/types.xml
    python tools/cargodump.py W/cargo.json Ammunition Clothing Equipment Food Melee Ranged
    python tools/wikifetch.py W/wiki "List of explosives" "Medical Supplies" "Weapon Attachments" "Character Status"
    #   (save the list pages as W/wiki/<Title_with_underscores>.txt, and item pages into W/pages via wikifetch.py)
    cp tools/manual.json W/
    python tools/build.py W
    python tools/gen_sql.py W/dataset.json db/seed.sql

Update `manual.json` (and the vehicle spawn counts in it) from the new patch notes / events.xml.

## Item images
Pictures live in `src/main/resources/static/img/items/` (WebP thumbnails, max 256 px) and are referenced
by the `image` column. They are in-game item renders from dayz.wiki.gg (game assets © Bohemia Interactive).
`tools/images.py` finds each entry's wiki image and `tools/fetch_images.py` downloads and converts them
(needs Pillow). Entries without a published image (some brand-new 1.30 items) fall back to the section icon.

## Maps section
Map pages (`/maps`, `/maps/{chernarus|livonia|sakhal|nasdara}`) read their text from
`src/main/resources/static/data/maps.json` (history, features, locations, vehicle limits from events.xml).
Per-map loot counts and map-exclusive items come live from `/api/v1/wiki/maps/{map}` (uses the `maps`
column of the `full_text_search` view). Map images are in `static/img/maps/` (`tools/fetch_map_images.py`).

## Shooting range

`/range` is a client-side damage calculator (`templates/range.html`, `static/js/range.js`, `static/css/range.css`).
Its data file `static/data/range.json` is built from the same wiki dumps as the item data:

    python db/tools/gen_range.py <dir with cargo.json, dataset.json, image_paths.json> src/main/resources/static/data/range.json

- Ammunition: health/blood/shock damage, muzzle speed and air friction from the wiki's Ammunition Cargo table.
- Clothing: durability and projectile protection coefficients (health, blood, shock) from the Clothing Cargo table.
- Bleeding rates (36 / 216 / 360 ml per minute, 5 minute clot) and blood/shock thresholds from the wiki's Bleeding, Blood and Unconsciousness pages.
- Body-part multipliers (`ZONES` at the top of range.js) are estimates; Bohemia does not publish them. Head damage is doubled, matching the 1.22 change.
