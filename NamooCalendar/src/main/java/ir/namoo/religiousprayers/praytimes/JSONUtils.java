package ir.namoo.religiousprayers.praytimes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JSONUtils {

    public static class City {
        public String name;
        public double lat;
        public double lng;

        @NotNull
        @Override
        public String toString() {
            return "{\"name\":" + "\"" + name + "\"," +
                    "\"lat\":" + lat + "," +
                    "\"lng\":" + lng + "}";
        }
    }

    public static class PrayTime {
        public int dayNum;
        public String fajr;
        public String sunrise;
        public String dhuhr;
        public String asr;
        public String maghrib;
        public String isha;

        @NotNull
        @Override
        public String toString() {
            return "{\"dayNum\":" + dayNum +
                    ",\"fajr\":\"" + fajr + "\"," +
                    "\"sunrise\":\"" + sunrise + "\"," +
                    "\"dhuhr\":\"" + dhuhr + "\"," +
                    "\"asr\":\"" + asr + "\"," +
                    "\"maghrib\":\"" + maghrib + "\"," +
                    "\"isha\":\"" + isha + "\"" +
                    "}";
        }
    }

    public static JSONObject toJson(City city, List<PrayTime> prayTimes) {
        if (city == null || prayTimes == null)
            return null;
        JSONObject res = new JSONObject();
        res.put("city", city);
        JSONArray plist = new JSONArray();
        plist.addAll(prayTimes);
        res.put("times", plist);
        return res;
    }

    @Nullable
    public static City getCity(String strJson) {
        try {
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(strJson);
            JSONObject jsonObject = (JSONObject) obj;
            Object jsonCity = jsonObject.get("city");
            JSONObject jCity = (JSONObject) jsonCity;
            if (jCity == null) return null;
            City res = new City();
            res.name = Objects.requireNonNull(jCity.get("name")).toString();
            res.lat = Double.parseDouble(Objects.requireNonNull(jCity.get("lat")).toString());
            res.lng = Double.parseDouble(Objects.requireNonNull(jCity.get("lng")).toString());
            return res;
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static List<PrayTime> getPrayTimes(String strJson) {
        try {
            List<PrayTime> res = new ArrayList<>();
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(strJson);
            JSONObject jsonObject = (JSONObject) obj;
            Object jsonTimes = jsonObject.get("times");
            JSONArray times = (JSONArray) jsonTimes;
            if (times == null) return null;
            for (Object j : times) {
                JSONObject t = (JSONObject) j;
                PrayTime temp = new PrayTime();
                temp.dayNum = Integer.parseInt(Objects.requireNonNull(t.get("dayNum")).toString());
                temp.fajr = (String) t.get("fajr");
                temp.sunrise = (String) t.get("sunrise");
                temp.dhuhr = (String) t.get("dhuhr");
                temp.asr = (String) t.get("asr");
                temp.maghrib = (String) t.get("maghrib");
                temp.isha = (String) t.get("isha");
                res.add(temp);
            }
            return res;
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
            ex.printStackTrace();
            return null;
        }
    }

}//end of JSONUtils
