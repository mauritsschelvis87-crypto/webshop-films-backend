# Movie Recommendation Algorithm

## Feature
The webshop includes a **collection feature** where users can track the movies they own at home.  
On this collection screen (/collection), users can also assign a **rating** to each movie.

This data (owned movies + ratings) is used to power a recommendation algorithm that suggests new movies for purchase.

---

## Goal
The goal of this algorithm is to:

- Recommend relevant movies based on a user’s existing collection
- Increase user engagement
- Encourage additional purchases by showing personalized suggestions
- Giving a better understanding of the (already) currently restored movies

---

## Target audience
Unlike mainstream audiences, this group is **less** focused;
- Actors (perhaps minor interest combined with a certain director)
- Release year
- Popularity
- Spoken language
- Country of origin

For this reason the algorithm will focus instead on;
- The movie director
- Film movement (e.g. Noir, Avant-garde, Technicolor)

---
## Important constraints
- Not all users can play all formats (DVD, Blu-ray, UHD 4K) therefore this algorithm has to take format into account
- Not all users can play all physical media due to region restrictions
- The algorithm will **not** take into account already owned movies

Therefore, the algorithm will only recommend;
- Formats that are supported by the user’s collection
- Prioritize formats that are most common within the user’s collection (if at all)
- Movies that are not (yet) in the user's current collection

The recommendation algorithm is designed to prioritize these aspects.

---
## Recommendation logic

After the constraints are implemented into the algorithm, we take into account these rules for the algorithm to work;

### Step 1
The algorithm first looks at the ratings a user has given to movies in their collection.

### Rule 1: Strongest positive rating
If a movie has received a rating of;
- Either 3.5 stars (out of 5)
- Or 4.5 / 5 stars
- Step 1 will randomize the output by randomly selecting a high rated movie out of the top 3 contestants.

Maximum output consists of **2 recommendations**


---
### Step 2
Then the algorithm generates a recommendation for the same director preferred by;
- A movie by the same director
- With a release year closest to the currently highest rated movie (5 star instead of the 4 star)
- Either earlier or later in time, depending on the nearest match

### Rule 2: Director consistency
If a user owns 2 or more movies by the same director, and those movies have an average rating of at least 3 stars, then the algorithm will also generate a recommendation for that director.

This recommendation should:

- Come from the same director (with medium to high rating)

Maximum output consists of **1 recommendation**

---
### Step 3
After the first two steps and considering the constraints, the algorithm adds a movie that the community has rated high.

### Rule 3: Randomizer
- 1 random movie recommendation
- The random movie is from the top 10 all-time rated movies (on this webshop)

If multiple candidates are very close in similarity score or year-distance, the algorithm may randomize between those nearest matches to keep the Scramble output broader while staying within the same recommendation logic.

Maximum output consists of **1 recommendation**

---
### Step 4
The algorithm will only be complete if there are a total of 4 movie recommendations.

### Rule 4: Fillers
If the algorithm produces fewer than 4 recommendations, the remaining slots will be filled by using the user’s collection as a reference point.

The filler recommendation should:

- Look at the country of origin most present in the user’s collection
- Calculate the average release year of movies from that country in the collection
- Recommend a movie from the same country of origin
- Select the movie with the closest release year to that average
- Exclude movies already owned by the user
- Respect format and region constraints
