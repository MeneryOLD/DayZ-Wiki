# DayZ Wiki

A fan-made encyclopedia for **DayZ**, kept up to date with **update 1.30 (Badlands)**.

**Live site: [thmirk.com](https://thmirk.com)**

Look up weapons, ammunition, attachments, clothing, food, medicine, diseases and vehicles, explore all four official maps, and test damage in an interactive shooting range.

> DayZ-Wiki — сайт, посвящённый игре DayZ: снаряжение, оружие, транспорт, медицина, еда, карты и механики выживания. Актуально для обновления 1.30. Сайт доступен на русском языке: [thmirk.com](https://thmirk.com).

## Features

- **Item catalog.** 617 entries in 9 sections, each with an in-game picture, stats, loot tier, rarity and the maps it spawns on. Only items that exist in the current game are included. Filter, sort, switch between grid and table, and open any item in a detail drawer with a shareable link.
- **Maps.** Pages for Chernarus, Livonia, Sakhal and Nasdara:
  - history and lore, and the features that make each map different
  - a screenshot gallery, the map image and key locations
  - vehicle spawn limits, and the items that spawn only on that map
- **Shooting range** (`/range`). Dress a survivor, pick any of 63 firearms and an ammo type, set the distance, and click on the body to fire. Each hit calculates:
  - health, shock and blood damage
  - armor protection and gear wear (Pristine to Ruined)
  - bleeding wounds and broken legs, and when the survivor passes out or dies
- **Search** across every section, with live suggestions in the top bar.
- **Accounts.** Register, sign in, write community posts and save bookmarks. Moderators can manage the featured YouTube videos.
- **English and Russian.** Switch with the EN/RU button in the top bar. The choice is saved in a cookie; first-time Russian-language browsers get Russian automatically. The interface, map pages and shooting range are translated; item names and descriptions stay in English, as in the game data.
- **Light and dark themes**, and a layout that works on phones.

## Tech stack

| Layer | Used |
|---|---|
| Backend | Java 17, Spring Boot 3.2 (Web, Security with JWT, Data JPA, Mail), Lombok |
| Database | MySQL 8 |
| Frontend | Thymeleaf templates, vanilla JavaScript, CSS (no build step) |
| Data tools | Python 3 scripts in `db/tools` (Pillow for images) |

## Getting started

### 1. Configure

```bash
cp .env.example .env    # then edit the values
```

| Variable | Meaning |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | MySQL connection |
| `DB_USER`, `DB_PASSWORD` | MySQL credentials |
| `MAIL_PASSWORD` | SMTP password for account emails (confirmation, password reset) |
| `JWTSecretKey` | Secret used to sign login tokens |

### 2. Run

**With Docker:**

```bash
./mvnw -DskipTests package
docker compose up --build
```

**Without Docker** (local MySQL 8 with an empty `dayzwiki` database):

```bash
./mvnw -DskipTests package
./run-local.sh            # loads .env and starts the jar
```

The site runs at <http://localhost:8080>.

**Preview without MySQL** (in-memory database with sample accounts, posts and videos; every demo account's password is `survivor1`, e.g. `ghost@example.com` is a moderator):

```bash
./mvnw -q dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=cp.txt
./mvnw -q test-compile
java -cp "target/test-classes:target/classes:$(cat cp.txt)" com.dayzwiki.portal.DayzWikiApplication --spring.profiles.active=h2,demo
```

(On Windows use `;` instead of `:` between the classpath parts.)

### 3. Load the wiki data

Start the app once so Hibernate creates the tables, then load the item data:

```bash
mysql -u root -p --default-character-set=utf8mb4 dayzwiki < db/seed.sql
```

## Project structure

```
src/main/java/com/dayzwiki/portal/
  controller/      pages and REST API (/api/v1/...)
  model/item/      item entities (shared fields in BaseItem)
  repository/      JPA repositories and the full-text search query
  config/          security and JWT
src/main/resources/
  templates/       Thymeleaf pages (layout.html holds the shared shell)
  messages*.properties  page text in English and Russian
  static/css|js/   site styles and scripts
  static/js/i18n.js  Russian text for the scripts
  static/data/     maps.json / maps.ru.json (map content) and range.json (shooting range data)
  static/img/      item and map images
db/
  seed.sql         item data for DayZ 1.30
  tools/           scripts that rebuild the data from the official sources
  README.md        how the data is built and updated after a game patch
```

## Data sources

- **Loot tiers, spawn maps and vehicle limits:** Bohemia Interactive's official [DayZ Central Economy](https://github.com/BohemiaInteractive/DayZ-Central-Economy) files.
- **Item stats, descriptions, images, ballistics and armor values:** the community [DayZ Wiki](https://dayz.wiki.gg) (CC BY-SA).
- **1.30 content not yet on the wiki:** the official patch notes (`db/tools/manual.json`).

The shooting range uses the game's own damage, armor and bleeding values. Body-part damage multipliers are not published by Bohemia Interactive, so those are estimates.

See [db/README.md](db/README.md) to refresh the data after a new game update.

## Disclaimer

This is an unofficial fan project and is not affiliated with Bohemia Interactive. DayZ, its images and its game assets are © Bohemia Interactive a.s.
