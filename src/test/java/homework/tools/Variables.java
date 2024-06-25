package homework.tools;

import io.restassured.path.json.JsonPath;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;

public class Variables {
    private JsonPath jp;
    private JSONArray payload;
    private Map scoreBoard;
    private Map.Entry maxEntry;
    private Map record;
    private int index;
    private int counter;
    private List list;
    private String filterKey;
    private String targetKey;
    private Object targetObject;
    private JSONArray oppsArray;

    public Variables() {}

    public JsonPath getJp() {
        return jp;
    }

    public void setJp(JsonPath jp) {
        this.jp = jp;
    }


    public JSONArray getPayload() {
        return payload;
    }

    public void setPayload(JSONArray payload) {
        this.payload = payload;
    }


    public Map getScoreBoard() {
        return scoreBoard;
    }

    public void setScoreBoard(Map scoreBoard) {
        this.scoreBoard = scoreBoard;
    }


    public Map.Entry getMaxEntry() {
        return maxEntry;
    }

    public void setMaxEntry(Map.Entry maxEntry) {
        this.maxEntry = maxEntry;
    }


    public Map getRecord() {
        return record;
    }

    public void setRecord(Map record) {
        this.record = record;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }


    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }


    public String getFilterKey() {
        return filterKey;
    }

    public void setFilterKey(String filterKey) {
        this.filterKey = filterKey;
    }


    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }


    public Object getTargetObject() {
        return targetObject;
    }

    public Object setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
        return targetObject;
    }


    public JSONArray getOppsArray() {
        return oppsArray;
    }

    public void setOppsArray(JSONArray oppsArray) {
        this.oppsArray = oppsArray;
    }
}