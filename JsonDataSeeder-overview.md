# JsonDataSeeder Overview

Hieronder staat hoe de data uit `films.json` door de backend wordt verwerkt en in de database terechtkomt.

## Mappingtabel

| JSON-veld | Java target | DB-doel | Hoe verwerkt |
|---|---|---|---|
| `title` | `Film.title` | `film.title` | Seeder zoekt eerst bestaande films op titel. Bestaat die al, dan update hij die film; anders maakt hij een nieuwe `Film`. |
| `genre` | `Film.genre` | `film.genre` | Rechtstreeks overgenomen en opgeslagen op de `Film`. |
| `director` | `Film.director` | `film.director` | Rechtstreeks overgenomen en opgeslagen. |
| `country` | `Film.country` | `film.country` | Wordt als string opgeslagen; `EuCountry` wordt hier nu niet gebruikt. |
| `year` | `Film.year` | `film.year` | Rechtstreeks overgenomen en opgeslagen als `int`. |
| `runtime` | `Film.runtime` | `film.runtime` | Rechtstreeks overgenomen en opgeslagen als `int`. |
| `type` | `Film.type` | `film.type` | Rechtstreeks overgenomen en opgeslagen. |
| `price` | `Film.price` | `film.price` | Rechtstreeks overgenomen en opgeslagen als `double`. |
| `imageUrl` | `Film.imageUrl` | `film.image_url` of equivalente kolom | Rechtstreeks overgenomen en opgeslagen. |
| `trailerUrl` | `Film.trailerUrl` | `film.trailer_url` of equivalente kolom | Rechtstreeks overgenomen en opgeslagen. |
| `aspectRatio` | `Film.aspectRatio` | `film.aspect_ratio` of equivalente kolom | Rechtstreeks overgenomen en opgeslagen. |
| `colorOrBlackAndWhite` | `Film.colorOrBlackAndWhite` | `film.color_or_black_and_white` of equivalente kolom | Rechtstreeks overgenomen en opgeslagen. |
| `description` | `Film.description` | `film.description` | Rechtstreeks overgenomen en opgeslagen; veld heeft `@Column(length = 2000)`. |
| `weight` | `Film.weight` | `film.weight` | Rechtstreeks overgenomen en opgeslagen als `int`. |
| `silent` | `Film.silent` | `film.silent` | Als de JSON-waarde `null` is, zet de seeder deze expliciet op `false`. |
| `stills` | `Film.stills` | `film_stills.still_url` via `film_id` | Wordt als `@ElementCollection` opgeslagen in aparte tabel `film_stills`. |
| `brand.name` | `Brand.name` en `Film.brand` | `brand.name` + `film.brand_id` | Seeder zoekt eerst een bestaande `Brand` op naam. Als die niet bestaat, maakt hij er een. Daarna koppelt hij de film via `brand_id`. |
| `actors[].name` | `Actor.name` en `Film.actors` | `actor.name` + many-to-many join table | Seeder zoekt elke actor op naam. Bestaat actor niet, dan wordt die aangemaakt. Daarna koppelt hij alle actors aan de film via de many-to-many relatie. |

## Extra verwerkingslogica

| Gedrag | Wat gebeurt er |
|---|---|
| Bestand inladen | `films.json` wordt als resource gelezen uit `src/main/resources/films.json`. |
| JSON parsing | Jackson zet de JSON om naar `List<FilmJson>`. |
| Upsert-achtig gedrag | Films worden niet blind toegevoegd; de seeder zoekt bestaande films op `title` en update die. |
| Duplicaten opruimen | Als meerdere films dezelfde titel hebben, bewaart hij de eerste en verwijdert hij de rest. |
| Actors dedupliceren | Actors worden in een `Set<Actor>` gezet, dus dubbele actors binnen een film worden voorkomen. |
| Brands hergebruiken | Brands worden hergebruikt op naam, zodat je geen meerdere identieke brands krijgt. |

## Relevante bestanden

- `src/main/java/homecinema/seeders/JsonDataSeeder.java`
- `src/main/java/homecinema/model/Film.java`
- `src/main/java/homecinema/model/Brand.java`
- `src/main/java/homecinema/model/Actor.java`
- `src/main/resources/films.json`
