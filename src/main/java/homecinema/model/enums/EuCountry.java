package homecinema.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum EuCountry {
    NL("Netherlands"),
    BE("Belgium"),
    DE("Germany"),
    FR("France"),
    LU("Luxembourg"),
    ES("Spain"),
    IT("Italy"),
    AT("Austria"),
    PL("Poland"),
    SE("Sweden"),
    FI("Finland"),
    DK("Denmark"),
    IE("Ireland"),
    PT("Portugal"),
    CZ("Czech Republic"),
    HU("Hungary"),
    GR("Greece"),
    SK("Slovakia"),
    SI("Slovenia"),
    HR("Croatia"),
    EE("Estonia"),
    LV("Latvia"),
    LT("Lithuania"),
    RO("Romania"),
    BG("Bulgaria"),
    CY("Cyprus"),
    MT("Malta");

    private final String fullName;

    EuCountry(String fullName) {
        this.fullName = fullName;
    }

    public String getCode() {
        return name();
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Returns Optional.empty() if not found.
     */
    public static Optional<EuCountry> fromFullName(String countryName) {
        if (countryName == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(c -> c.fullName.equalsIgnoreCase(countryName.trim()))
                .findFirst();
    }
}
