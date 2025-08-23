package org.braun.cookbook.backend.model.recipe;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mbraun
 *
 */
public class Units {

    private final HashMap<String, String> hm;
    private final Map<String, String> mh;

    private static final Units units = new Units();

    public static Units getInstance() {
        return units;
    }
    
    private Units() {
        mh = new HashMap<>();
        hm = new HashMap<>();
        mh.put("sl", "Scheibe");
        mh.put("ts", "TL");
        mh.put("tb", "EL");
        mh.put("bn", "Bund");
        mh.put("g ", "g");
        mh.put("c", "Tasse");
        mh.put("lg", "groß");
        mh.put("sm", "klein");
        mh.put("cn", "Dose");
        mh.put("ct", "Karton");
        mh.put("pn", "Prise");
        mh.put("md", "mittel");
        mh.put("pk", "Packung");
        mh.put("kg", "kg");
        mh.put("l ", "l");
        mh.put("ml", "ml");
        mh.put("cl", "cl");
        mh.put("dl", "dl");
        mh.put("ds", "Spur");
        mh.put("dr", "Tropfen");
        mh.put("pn", "Prise");
        mh.put("s", "Schuss");
        mh.put("zw", "Zweige");
        mh.put("bl", "Blätter");
        mh.put("ds", "Msp.");
        mh.put("hv", "handvoll");
        mh.put("zw", "Zweige");

        hm.put("cl", "cl");
        hm.put("Msp.", "ds");
        hm.put("Msp", "ds");
        hm.put("Messerspitze", "ds");
        hm.put("Messerspitzen", "ds");
        hm.put("Sc", "sl");
        hm.put("Scheib.", "sl");
        hm.put("Scheibe", "sl");
        hm.put("Scheiben", "sl");
        hm.put("Schb", "sl");
        hm.put("Teel.", "ts");
        hm.put("Tl", "ts");
        hm.put("TL", "ts");
        hm.put("Teelöffel", "ts");
        hm.put("teasp.", "ts");
        hm.put("Essl.", "tb");
        hm.put("El", "tb");
        hm.put("EL", "tb");
        hm.put("Esslöffel", "tb");
        hm.put("Eßlöffel", "tb");
        hm.put("Eßl.", "tb");
        hm.put("tablesp.", "tb");
        hm.put("Bund", "bn");
        hm.put("Gramm", "g");
        hm.put("gramm", "g");
        hm.put("g", "g");
        hm.put("gr.", "g");
        hm.put("Sk", "");
        hm.put("St", "");
        hm.put("St.", "");
        hm.put("st", "");
        hm.put("Stk", "");
        hm.put("Stück", "");
        hm.put("Stange", "Stange");
        hm.put("Stangen", "Stange");
        hm.put("Stengel", "Stängel");
        hm.put("Stängel", "Stängel");
        hm.put("Kopf", "");
        hm.put("Tr", "dr");
        hm.put("Tropfen", "dr");
        hm.put("Tasse/n", "c");
        hm.put("Tassen", "c");
        hm.put("Tasse", "c");
        hm.put("cup", "c");
        hm.put("gr", "lg");
        hm.put("groß.", "lg");
        hm.put("groß", "lg");
        hm.put("große", "lg");
        hm.put("großes", "lg");
        hm.put("großer", "lg");
        hm.put("gross", "lg");
        hm.put("gross.", "lg");
        hm.put("kl", "sm");
        hm.put("klein", "sm");
        hm.put("klein.", "sm");
        hm.put("kleine", "sm");
        hm.put("kleiner", "sm");
        hm.put("kleines", "sm");
        hm.put("Dose/n", "cn");
        hm.put("Dose", "cn");
        hm.put("Dosen", "cn");
        hm.put("Döschen", "cn");
        hm.put("Karton", "ct");
        hm.put("Prise/n", "pn");
        hm.put("Prise", "pn");
        hm.put("Prisen", "pn");
        hm.put("mi.", "md");
        hm.put("mittelgroß", "");
        hm.put("mittel.", "");
        hm.put("mittl.", "");
        hm.put("mittlere", "");
        hm.put("Pack.", "pk");
        hm.put("Packung", "pk");
        hm.put("Pck", "pk");
        hm.put("Kilo", "kg");
        hm.put("Kilogramm", "kg");
        hm.put("kg", "kg");
        hm.put("Kg", "kg");
        hm.put("Liter", "l");
        hm.put("L", "l");
        hm.put("l", "l");
        hm.put("Ltr.", "l");
        hm.put("qb", "ml");
        hm.put("ml", "ml");
        hm.put("Milliliter", "ml");
        hm.put("Schuss", "s");
        hm.put("Zweig", "zw");
        hm.put("Zweige", "zw");
        hm.put("Blätter", "bl");
        hm.put("Bl", "bl");
        hm.put("bl", "bl");
        hm.put("handvoll", "hv");
        hm.put("Handvoll", "hv");
        hm.put("hv", "hv");
        hm.put("Zweige", "zw");
        hm.put("Zweig", "zw");
        hm.put("Zweig.", "zw");
        hm.put("zw", "zw");
    }

    public String getDescription(String unit) {
        if (unit == null) {
            return "";
        }
        if (units.mh.containsKey(unit)) {
            return units.mh.get(unit);
        }
        return unit;
    }

    public boolean containsUnit(String unit) {
        return units.hm.containsKey(unit);
    }

    public String getUnit(String unit) {
        if (unit == null) {
            return "";
        }
        if (units.hm.containsKey(unit)) {
            return units.hm.get(unit);
        }
        return unit;
    }
}
