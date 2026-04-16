# Cloudinary asset mappings

De backend leest Cloudinary-URLs uit deze resourcebestanden:

- `src/main/resources/covers_cloudinary.json`
- `src/main/resources/stills_cloudinary.json`
- `src/main/resources/directors_cloudinary.json`
- `src/main/resources/boxset_cloudinary.json`
- `src/main/resources/gifts_cloudinary.json`

Elk bestand is een eenvoudige key-value map:

```json
{
  "/assets/directors/akira_kurosawa.jpg": "https://res.cloudinary.com/<cloud-name>/image/upload/f_auto,q_auto/homecinema/directors/akira_kurosawa.jpg"
}
```

Regels:

- De `key` is het oude lokale pad of een stabiele asset-key uit `films.json`, `directors.json`, `boxsets.json` of frontend-only gift media.
- De `value` is de volledige publieke Cloudinary delivery URL.
- Een lege `value` betekent: asset is bewust nog niet beschikbaar in Cloudinary. De backend geeft dan een lege string terug in plaats van een kapot lokaal pad.
- Als een key helemaal geen mapping heeft, blijft de backend het originele pad teruggeven.

Waar de mappings gebruikt worden:

- `films.json`: `imageUrl` via `covers_cloudinary.json`, `stills` via `stills_cloudinary.json`
- `directors.json`: `image` via `directors_cloudinary.json`
- `boxsets.json`: `topImage`, `secondaryImage`, image media items, `product.imageUrl` en `product.stills` via `boxset_cloudinary.json`
- gift assets: frontend keys via `gifts_cloudinary.json`

Extra endpoint:

- `GET /api/media-assets`

Dat endpoint geeft de ingelezen mappings terug aan de frontend voor pagina's die nog losse hero-afbeeldingen gebruiken.
