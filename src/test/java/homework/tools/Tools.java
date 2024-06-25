package homework.tools;

import io.restassured.path.json.JsonPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Tools {

    SimpleDateFormat jdf;
    JsonPath jp;
    JSONArray payload;
    Map scoreBoard;
    Map.Entry maxEntry;
    Map record;
    int index;
    int counter;
    List list;
    List eventTimes;
    String filterKey;
    String targetKey;
    Object targetObject;
    JSONArray oppsArray;
    Date date;
    String formattedDate;

    public Tools() throws IOException {
        parseFile("src/test/resources/downloads.txt");
        jdf = new SimpleDateFormat("EEE HH:mm");
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    //    Source: https://www.baeldung.com/java-sort-map-descending
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDescending(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public void resetIterator() {
        index = 0;
        counter = 0;
        record = new HashMap();
        list = new ArrayList();
    }

    void parseFile(String path) throws IOException {
        payload = new JSONArray();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String str;

        while ((str = br.readLine()) != null) {
            JSONObject jsonObject = new JSONObject(str);
            payload.put(jsonObject);
        }
        Assert.assertTrue("JSON payload is not valid.", isValid(payload.toString()));

        jp = (new JsonPath(payload.toString()));
    }

    boolean isValid(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            try {
                new JSONArray(json);
            } catch (JSONException ne) {
                return false;
            }
        }
        return true;
    }

    String jsonPathFactory(String targetObject, int index) {
        switch (targetObject) {
            case "city" -> {
                return "city[%1$s]".formatted(index);
            }
            case "showId" -> {
                return "downloadIdentifier[%1$s].showId".formatted(index);
            }
            case "deviceType" -> {
                return "deviceType[%1$s]".formatted(index);
            }
            case "adBreakIndex" -> {
                return "opportunities[%1$s].positionUrlSegments[\"aw_0_ais.adBreakIndex\"]".formatted(index);
            }
            case "originalEventTime" -> {
                return "opportunities[%1$s].originalEventTime".formatted(index);
            }

            default -> throw new IllegalStateException("Unexpected value: " + targetObject);
        }
    }

    Object getJsonObjectFromPath(String path) {
        return targetObject = jp.getJsonObject(path);
    }

    void buildRecord() {
        if (!record.containsKey(targetKey)) {
            counter = 1;
            record.put(targetKey, counter);
        } else {
            counter = (int) record.get(targetKey);
            record.replace(targetKey, counter, counter + 1);
        }
    }

    void mapIterator(Map<String, Integer> map) {
        maxEntry = null;
        scoreBoard = new HashMap();
        map.entrySet().forEach(entry -> {
            scoreBoard.put(entry.getKey(), entry.getValue());

            if (maxEntry == null || entry.getValue().compareTo((Integer) maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        });

        scoreBoard = sortMapByValueDescending(scoreBoard);
    }

    void payloadIterator(String targetObject) {
        payload.forEach(o -> {
            targetKey = getJsonObjectFromPath(jsonPathFactory(targetObject, index)).toString();
            if (!list.contains(targetKey)) list.add(targetKey);
            buildRecord();
            index++;
        });
    }

    void payloadIterator(String targetObject, String filterCriteria, String filterValue) {
        payload.forEach(o -> {
            filterKey = getJsonObjectFromPath(jsonPathFactory(filterCriteria, index)).toString();
            targetKey = getJsonObjectFromPath(jsonPathFactory(targetObject, index)).toString();

            if (!list.contains(filterKey)) list.add(filterKey);

            if (filterKey.equalsIgnoreCase(filterValue) || filterKey.contains(filterValue.toLowerCase())) buildRecord();

            index++;
        });
    }

    //    Source: https://www.w3resource.com/java-exercises/datetime/java-datetime-exercise-36.php
    String unixToDateConverter(Long unix) {
        //convert seconds to milliseconds
        date = new Date(unix);
        // format of the date
        String java_date = jdf.format(date);

        return java_date;
    }


    public void getTopDldShowPerCity(String selectedCity) {
        System.out.println("""
                1.) Calculeaza si printeaza care este emisiunea de podcast (aceasta este identificata prin
                    showId) cea mai ascultata (cea cu cele mai multe download-uri) din San Francisco precum si
                    numarul de download-uri asociat acestei emisiuni.
                """);

        payloadIterator("showId", "city", selectedCity);
        mapIterator(record);

        Assert.assertTrue("""
                Selected city name "%1$s" is missing from the available data.
                Please select a city from the following list:
                %2$s
                """.formatted(selectedCity, list), list.contains(selectedCity.toLowerCase()));


        Assert.assertEquals("Who Trolled Amber", maxEntry.getKey().toString());
        Assert.assertEquals(24, maxEntry.getValue());

        System.out.println("""
                Solution:    \033[3m
                    Most popular show in %1$s is: %2$s
                    Number of downloads is: %3$s \033[0m
                """
                .formatted(
                        selectedCity,
                        maxEntry.getKey(),
                        maxEntry.getValue(),
                        scoreBoard.toString()
                                .replace("{", "")
                                .replace("}", "")
                                .replace(", ", "\n")
                                .replace("=", " -> ")
                )
        );
    }

    public void getMostUsedDeviceType() {
        System.out.println("""
                2.) Calculeaza si printeaza care este device-ul (aceasta este identificat prin deviceType; eg.
                    desktops & laptops, mobiles & tablets, smart speakers, digital appliances) cel mai folosit
                    pentru a asculta podcast-uri precum si numarul de download-uri asociat acestui device.
                """);

        payloadIterator("deviceType");

        mapIterator(record);

        Assert.assertEquals("mobiles & tablets", maxEntry.getKey().toString());
        Assert.assertEquals(60, maxEntry.getValue());

        System.out.println("""
                Solution:\033[3m
                    Most popular device is: %1$s
                    Number of downloads is: %2$s\033[0m
                """
                .formatted(
                        maxEntry.getKey(),
                        maxEntry.getValue(),
                        scoreBoard.toString()
                                .replace("{", "")
                                .replace("}", "")
                                .replace(", ", "\n")
                                .replace("=", " -> ")
                )
        );
    }

    public void getPreRollOpportunities(String oppType) {
        System.out.println("""
                3.) Calculeaza si printeaza cate oportunitati de a insera o reclama in preroll au existat pentru
                    fiecare emisiune de podcast. Printeaza aceasta informatie in ordinea descrescatoare a
                    numarului de oportunitati. Asta se traduce tehnic in: pentru fiecare showId, care este
                    numarul de oportunitati care contin in lista aw_0_ais.adBreakIndex valoarea preroll.
                """);

        payload.forEach(o -> {
            filterKey = getJsonObjectFromPath(jsonPathFactory("adBreakIndex", index)).toString();
            targetKey = getJsonObjectFromPath(jsonPathFactory("showId", index)).toString();
            oppsArray = new JSONArray(filterKey);

            if (!record.containsKey(targetKey)) record.put(targetKey, 0);

            oppsArray.forEach(p -> {
                if (p.toString().contains(oppType) || p.toString().equalsIgnoreCase(oppType)) {
                    counter = (int) record.get(targetKey);
                    record.replace(targetKey, counter, counter + 1);
                }
            });
            index++;
        });

        mapIterator(record);

        list.add(0, "Stuff You Should Know = 40");
        list.add(1, "Who Trolled Amber = 40");
        list.add(2, "Crime Junkie = 30");
        list.add(3, "The Joe Rogan Experience = 10");

        index = 0;
        scoreBoard.forEach((k, v) -> {
            Assert.assertEquals(list.get(index), "%1$s = %2$s".formatted(k, v));
            index++;
        });

        System.out.println("Solution:");
        scoreBoard.forEach((key, value) -> {
            System.out.println("    \033[3mShow Id: %1$s, Preroll Opportunity Number: %2$s\033[0m".formatted(key, value));
        });
        System.out.println("");
    }

    public void getOppByEventTime() {
        System.out.println("""
                4.) Cerinta bonus: Folosind event time-ul (originalEventTime) la care au aparut oportunitatile
                    de a insera reclame calculeaza si printeaza doar emisiunile de podcast difuzate saptamanal
                    precum si ziua si ora la care aceste emisiuni sunt difuzate.
                """);
        record = new HashMap();

        payload.forEach(o -> {
            filterKey = getJsonObjectFromPath(jsonPathFactory("originalEventTime", index)).toString();
            targetKey = getJsonObjectFromPath(jsonPathFactory("showId", index)).toString();
            oppsArray = new JSONArray(filterKey);

            if (!record.containsKey(targetKey)) eventTimes = new ArrayList<>();

            oppsArray.forEach(e -> {
                if (record.containsKey(targetKey) && !record.get(targetKey).toString().contains(e.toString()))
                    eventTimes.add(e);
            });

            record.put(targetKey, eventTimes);
            index++;
        });

        Map bonusMap = new HashMap();

        record.forEach((k, v) -> {
            Set set = new HashSet();
            oppsArray = new JSONArray(v.toString());
            oppsArray.forEach(e -> {
                formattedDate = unixToDateConverter((Long) e);
                set.add(formattedDate);
            });
            if (set.size() == 1) bonusMap.put(k, formattedDate);
            else bonusMap.remove(k, formattedDate);
        });

        list.add("Crime Junkie - Wed 22:00");
        list.add("Who Trolled Amber - Mon 20:00");

        index = 0;
        bonusMap.forEach((k, v) -> {
            Assert.assertEquals(list.get(index), "%1$s - %2$s".formatted(k, v));
            index++;
        });

        System.out.println("""
                Solution: \033[3m
                    Weekly shows are:
                """);

        System.out.println(bonusMap.toString()
                .replace("{", "    ")
                .replace("}", "\033[0m\n")
                .replace(", ","\n    ")
                .replace("=", " - ")
        );
    }
}
