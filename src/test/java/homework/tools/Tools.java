package homework.tools;

import io.restassured.path.json.JsonPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Tools {

    Variables v;

    public Tools() throws IOException {
        v = new Variables();
        parseFile("src/test/resources/downloads.txt");
    }

    //    Source: https://www.baeldung.com/java-sort-map-descending
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDescending(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public void resetIterator() {
        v.setIndex(0);
        v.setCounter(0);
        v.setRecord(new HashMap());
        v.setList(new ArrayList<>());
    }

    void parseFile(String path) throws IOException {
        v.setPayload(new JSONArray());
        BufferedReader br = new BufferedReader(new FileReader(path));
        String str;

        while ((str = br.readLine()) != null) {
            JSONObject jsonObject = new JSONObject(str);
            v.getPayload().put(jsonObject);
        }
        Assert.assertTrue("JSON payload is not valid.", isValid(v.getPayload().toString()));

        v.setJp(new JsonPath(v.getPayload().toString()));
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

            default -> throw new IllegalStateException("Unexpected value: " + targetObject);
        }
    }

    Object getJsonObjectFromPath(String path) {
        return v.setTargetObject(v.getJp().getJsonObject(path));
    }

    void buildRecord() {
        if (!v.getRecord().containsKey(v.getTargetKey())) {
            v.setCounter(1);
            v.getRecord().put(v.getTargetKey(), v.getCounter());
        } else {
            v.setCounter((int) v.getRecord().get(v.getTargetKey()));
            v.getRecord().replace(v.getTargetKey(), v.getCounter(), v.getCounter() + 1);
        }
    }

    void mapIterator(Map<String, Integer> map) {
        v.setMaxEntry(null);
        v.setScoreBoard(new HashMap());
        map.entrySet().forEach(entry -> {
            v.getScoreBoard().put(entry.getKey(), entry.getValue());
            if (v.getMaxEntry() == null || entry.getValue().compareTo((Integer) v.getMaxEntry().getValue()) > 0) {
                v.setMaxEntry(entry);
            }
        });

        v.setScoreBoard(sortMapByValueDescending(v.getScoreBoard()));
    }

    void payloadIterator(String targetObject) {
        v.getPayload().forEach(o -> {
            v.setTargetKey(getJsonObjectFromPath(jsonPathFactory(targetObject, v.getIndex())).toString());
            if (!v.getList().contains(v.getTargetKey())) v.getList().add(v.getTargetKey());
            buildRecord();
            v.setIndex(v.getIndex() + 1);
        });
    }

    void payloadIterator(String targetObject, String filterCriteria, String filterValue) {
        v.getPayload().forEach(o -> {
            v.setFilterKey(getJsonObjectFromPath(jsonPathFactory(filterCriteria, v.getIndex())).toString());
            v.setTargetKey(getJsonObjectFromPath(jsonPathFactory(targetObject, v.getIndex())).toString());

            if (!v.getList().contains(v.getFilterKey())) v.getList().add(v.getFilterKey());

            if (v.getFilterKey().equalsIgnoreCase(filterValue) || v.getFilterKey().contains(filterValue)) buildRecord();

            v.setIndex(v.getIndex() + 1);
        });
    }


    public void getTopDldShowPerCity(String selectedCity) {
        System.out.println("""
                1.) Calculeaza si printeaza care este emisiunea de podcast (aceasta este identificata prin
                    showId) cea mai ascultata (cea cu cele mai multe download-uri) din San Francisco precum si
                    numarul de download-uri asociat acestei emisiuni.
                """);

        payloadIterator("showId", "city", selectedCity);

        Assert.assertTrue("""
                Selected city name "%1$s" is missing from the available data.
                Please select a city from the following list:
                %2$s
                """.formatted(selectedCity, v.getList()), v.getList().contains(selectedCity.toLowerCase()));

        mapIterator(v.getRecord());

        Assert.assertEquals("Who Trolled Amber", v.getMaxEntry().getKey().toString());
        Assert.assertEquals(24, v.getMaxEntry().getValue());

        System.out.println("""
                Solution:    
                    Most popular show in %1$s is: %2$s
                    Number of downloads is: %3$s
                """
                .formatted(
                        selectedCity,
                        v.getMaxEntry().getKey(),
                        v.getMaxEntry().getValue(),
                        v.getScoreBoard().toString()
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

        mapIterator(v.getRecord());

        Assert.assertEquals("mobiles & tablets", v.getMaxEntry().getKey().toString());
        Assert.assertEquals(60, v.getMaxEntry().getValue());

        System.out.println("""
                Solution:
                    Most popular device is: %1$s
                    Number of downloads is: %2$s
                """
                .formatted(
                        v.getMaxEntry().getKey(),
                        v.getMaxEntry().getValue(),
                        v.getScoreBoard().toString()
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

        v.getPayload().forEach(o -> {
            v.setFilterKey(getJsonObjectFromPath(jsonPathFactory("adBreakIndex", v.getIndex())).toString());
            v.setTargetKey(getJsonObjectFromPath(jsonPathFactory("showId", v.getIndex())).toString());
            v.setOppsArray(new JSONArray(v.getFilterKey()));

            if (!v.getRecord().containsKey(v.getTargetKey())) v.getRecord().put(v.getTargetKey(), 0);

            v.getOppsArray().forEach(p -> {
                if (p.toString().contains(oppType) || p.toString().equalsIgnoreCase(oppType)) {
                    v.setCounter((int) v.getRecord().get(v.getTargetKey()));
                    v.getRecord().replace(v.getTargetKey(), v.getCounter(), v.getCounter() + 1);
                }
            });

            v.setIndex(v.getIndex() + 1);
        });

        mapIterator(v.getRecord());

        v.getList().add(0, "Stuff You Should Know = 40");
        v.getList().add(1, "Who Trolled Amber = 40");
        v.getList().add(2, "Crime Junkie = 30");
        v.getList().add(3, "The Joe Rogan Experience = 10");

        v.setIndex(0);
        v.getScoreBoard().forEach((k, v) -> {
            Assert.assertEquals(this.v.getList().get(this.v.getIndex()), "%1$s = %2$s".formatted(k, v));
            this.v.setIndex(this.v.getIndex() + 1);
        });

        System.out.println("Solution:");
        v.getScoreBoard().forEach((key, value) -> {
            System.out.println("    Show Id: %1$s, Preroll Opportunity Number: %2$s".formatted(key, value));
        });
        System.out.println("");
    }
}
