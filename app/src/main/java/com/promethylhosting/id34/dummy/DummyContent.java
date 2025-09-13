package com.promethylhosting.id34.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyContent {

    public static class DummyItem {

        public String id;
        public boolean deleted = false;
        public String content;

        public DummyItem(String id, String content, String deleted) {
            this.id = id;
            this.content = content;
            if (deleted == "1") { this.deleted = true; }else {  this.deleted=false; }  
        }

        @Override
        public String toString() {
            return content;
        }
    }

    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    static {
        //addItem(new DummyItem("1", "Item 1"));
        //addItem(new DummyItem("2", "Item 2"));
        //addItem(new DummyItem("3", "Item 3"));
    }

    public static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
}
