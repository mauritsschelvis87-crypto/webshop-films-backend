require("dotenv").config();
const fs = require("fs");
const path = require("path");
const cloudinary = require("cloudinary").v2;

cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
    api_key: process.env.CLOUDINARY_API_KEY,
    api_secret: process.env.CLOUDINARY_API_SECRET,
});

const jsonPath = path.join(__dirname, "stills_cloudinary.json");

const aliases = {
    "4b1": "241",
    "4b2": "242",
    "4b3": "243",

    "ci1": "city_lights",
    "ci2": "city_lights",
    "ci3": "city_lights",

    "ikwim2": "ikwig2",
    "ioaw1": "ioaq1",

    "totg1": "trotg1",
    "totg2": "trotg2",
    "totg3": "trotg3",

    "se2": "s02",

    "witd2": "woman_in_the_dunes",
    "witd3": "WomanInTheDunes"
};

function baseName(value) {
    return path
        .basename(value)
        .replace(/\.[^.]+$/, "")
        .toLowerCase();
}

function findByPrefix(images, searchKey) {
    const normalizedSearchKey = searchKey.toLowerCase();

    const found = images.find((image) => {
        const cloudName = baseName(image.public_id);
        return (
            cloudName === normalizedSearchKey ||
            cloudName.startsWith(normalizedSearchKey + "_")
        );
    });

    return found ? found.secure_url : null;
}

async function getAllImages() {
    let all = [];
    let nextCursor = undefined;

    do {
        const result = await cloudinary.search
            .expression("resource_type:image")
            .max_results(500)
            .next_cursor(nextCursor)
            .execute();

        all.push(...result.resources);
        nextCursor = result.next_cursor;
    } while (nextCursor);

    return all;
}

async function main() {
    const json = JSON.parse(fs.readFileSync(jsonPath, "utf8"));
    const images = await getAllImages();

    console.log("Aantal Cloudinary images:", images.length);

    const imageMap = new Map();

    for (const image of images) {
        const key = baseName(image.public_id);
        imageMap.set(key, image.secure_url);
    }

    let filled = 0;
    const notFound = [];

    for (const localPath of Object.keys(json)) {
        if (json[localPath]) continue;

        const key = baseName(localPath);

        const aliasKey = aliases[key];
        let url = aliasKey ? findByPrefix(images, aliasKey) : null;

        if (!url) {
            url = imageMap.get(key);
        }

        if (!url) {
            url = findByPrefix(images, key);
        }

        if (!url) {
            const found = images.find((image) => {
                const cloudName = baseName(image.public_id);
                return cloudName.includes(key);
            });

            if (found) {
                url = found.secure_url;
            }
        }

        if (url) {
            json[localPath] = url;
            filled++;
        } else {
            notFound.push(localPath);
        }
    }

    fs.writeFileSync(jsonPath, JSON.stringify(json, null, 2));

    console.log(`\nGevuld: ${filled}`);
    console.log(`Niet gevonden: ${notFound.length}`);

    if (notFound.length) {
        console.log("\nNiet gevonden:");
        notFound.forEach((item) => console.log(item));
    }

    const debugPath = path.join(__dirname, "missing_stills.txt");
    fs.writeFileSync(debugPath, notFound.join("\n"), "utf8");

    console.log(`\nMissing lijst opgeslagen in: ${debugPath}`);
}

main().catch(console.error);