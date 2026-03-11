# k6 scripts

Deze map bevat eenvoudige k6-tests voor de backend.

## Scripts

- `smoke-films.js`
  - Basale smoke test voor `GET /api/films`
- `login-flow.js`
  - Registreert een gebruiker en test daarna `POST /api/auth/login`
- `basic-order-flow.js`
  - Haalt films op, registreert een gebruiker en plaatst een order
- `wishlist-flow.js`
  - Registreert een gebruiker, haalt het account op en test wishlist add/get
- `staged-films-load.js`
  - Oplopende load op `GET /api/films` met extra metriekoutput

## Voorwaarden

- Backend draait lokaal, standaard op `http://localhost:8080`
- PostgreSQL draait
- Seeddata is aanwezig zodat `/api/films` minimaal 1 film teruggeeft
- `k6` is geïnstalleerd

## Run

```powershell
k6 run k6/smoke-films.js
```

```powershell
k6 run k6/login-flow.js
```

```powershell
k6 run k6/basic-order-flow.js
```

```powershell
k6 run k6/wishlist-flow.js
```

```powershell
k6 run k6/staged-films-load.js
```

## Andere base URL

```powershell
$env:BASE_URL="http://localhost:8080"
k6 run k6/staged-films-load.js
```

## Meer duidelijkheid in resultaten

Gebruik eerst `staged-films-load.js`. Die geeft een compacte samenvatting met:

- check pass rate
- failed request rate
- `p(95)` responstijd
- `p(95)` voor alleen de film-call

Handige opties:

```powershell
k6 run --verbose k6/staged-films-load.js
```

```powershell
k6 run --summary-trend-stats "avg,min,med,max,p(90),p(95),p(99)" k6/staged-films-load.js
```

```powershell
k6 run --out json=results.json k6/staged-films-load.js
```

Wat je vooral moet bekijken:

- `http_req_failed`
  - laat zien hoeveel requests mislukken
- `http_req_duration`
  - totale responstijd
- `p(95)`
  - 95% van de requests zit onder deze waarde
- `checks`
  - functionele validaties, zoals status `200`

Praktische vuistregel:

- eerst smoke test
- daarna kleine staged load
- daarna pas hogere aantallen VUs of langere duur
