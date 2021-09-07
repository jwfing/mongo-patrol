package cn.leancloud.utils;

import cn.leancloud.LCLogger;
import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.core.LeanCloud;

import java.util.*;

public class Reporter {
  public static void main( String[] args ) {
    LeanCloud.initialize("RGM6sPnzVjLxva3WBR5CBrRo-gzGzoHsz", "hU4UT7ornEsiyyj9GCdVRJ8D",
            "https://rgm6spnz.lc-cn-n1-shared.com");
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    Date now = new Date();
    Date startOfThisWeek = new Date(now.getTime() - 7 * 86400000);
    Date startOfPrevWeek = new Date(now.getTime() - 14 * 86400000);
    LCQuery<LCObject> queryThisWeek = new LCQuery<>("Suspect");
    queryThisWeek.whereGreaterThan("createdAt", startOfThisWeek);
    queryThisWeek.selectKeys(Arrays.asList("cluster", "database", "collection"));
    queryThisWeek.addAscendingOrder("createdAt");
    queryThisWeek.setLimit(1000);
    List<LCObject> resultOfThisWeekly = queryThisWeek.find();
    System.out.println("============ this week suspect count: " + resultOfThisWeekly.size());
    Map<String, Integer> thisWeekStats = new HashMap<>(1000);
    Map<String, Set<String>> thisWeekDetails = new HashMap<>(1000);
    System.out.println("============ this week suspect list:");
    for (LCObject rst: resultOfThisWeekly) {
      String appId = rst.getString("database");
      if (null == appId || appId.trim().length() < 1) {
        continue;
      }
      int count = 1;
      Set<String> collSet = new HashSet<>();
      if (thisWeekStats.containsKey(appId)) {
        count = thisWeekStats.get(appId) + 1;
        collSet = thisWeekDetails.get(appId);
      }
      collSet.add(rst.getString("collection"));
      thisWeekStats.put(appId, count);
      thisWeekDetails.put(appId, collSet);
    }
    for (Map.Entry entry: thisWeekStats.entrySet()) {
      System.out.println(entry.getKey() + ", " + entry.getValue());
    }

    LCQuery<LCObject> queryPrevWeek = new LCQuery<>("Suspect");
    queryPrevWeek.whereGreaterThan("createdAt", startOfPrevWeek);
    queryPrevWeek.whereLessThan("createdAt", startOfThisWeek);
    queryPrevWeek.selectKeys(Arrays.asList("cluster", "database", "collection"));
    queryPrevWeek.addAscendingOrder("createdAt");
    queryPrevWeek.setLimit(1000);
    List<LCObject> resultOfPrevWeekly = queryPrevWeek.find();
    System.out.println("============ prev week suspect count: " + resultOfPrevWeekly.size());
    Map<String, Integer> prevWeekStats = new HashMap<>(1000);
    for (LCObject rst: resultOfPrevWeekly) {
      String appId = rst.getString("database");
      if (null == appId || appId.trim().length() < 1) {
        continue;
      }
      int count = 1;
      if (prevWeekStats.containsKey(appId)) {
        count = prevWeekStats.get(appId) + 1;
      }
      prevWeekStats.put(appId, count);
    }
    System.out.println("============ prev week suspect list:");
    for (Map.Entry entry: prevWeekStats.entrySet()) {
      System.out.println(entry.getKey() + ", " + entry.getValue());
    }

    System.out.println("============ new suspect list:");
    for (Map.Entry entry: thisWeekStats.entrySet()) {
      if (prevWeekStats.containsKey(entry.getKey())) {
        continue;
      }
      System.out.println(entry.getKey() + ", " + thisWeekDetails.get(entry.getKey()).toString());
    }

  }
}
