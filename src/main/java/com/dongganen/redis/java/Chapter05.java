package com.dongganen.redis.java;

import javafx.util.Pair;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;


import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;

public class Chapter05 {

    public static final String DEBUG = "debug";
    public static final String INFO = "info";
    public static final String WARNING = "warning";
    public static final String ERROR = "error";
    public static final String CRITICAL = "critical";

    public static final Collator COLLATOR = Collator.getInstance();

    public static final SimpleDateFormat TIMESTAMP =
            new SimpleDateFormat("EEE MMM dd HH:00:00 yyyy");
    private static final SimpleDateFormat ISO_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:00:00");
    static{
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static final void main(String[] args) throws InterruptedException {
        new Chapter05().run();
    }

    public void run() throws InterruptedException {
        Jedis conn = new Jedis("192.168.0.135");
        conn.select(15);

        //最新日志
//        testLogRecent(conn);
        //常见日志
//        testLogCommon(conn);
        //计数器
        testCounters(conn);
//        testStats(conn);
//        testAccessTime(conn);
//        testIpLookup(conn);
//        testIsUnderMaintenance(conn);
//        testConfig(conn);
    }

    /**
     * @author  dong
     * @create  2018/1/5 10:30
     * @desc 最新日志
     **/
    public void testLogRecent(Jedis conn) {
        System.out.println("\n----- testLogRecent -----");
        System.out.println("Let's write a few logs to the recent log");
        for (int i = 0; i < 5; i++) {
            //记录日志
            logRecent(conn, "test", "this is message " + i);
        }
        List<String> recent = conn.lrange("recent:test:info", 0, -1);
        System.out.println(
                "The current recent message log has this many messages: " +
                        recent.size());
        System.out.println("Those messages include:");
        for (String message : recent){
            System.out.println(message);
        }
        assert recent.size() >= 5;
    }

    /**
     * @author  dong
     * @create  2018/1/5 10:30
     * @desc 常见日志
     **/
    public void testLogCommon(Jedis conn) {
        System.out.println("\n----- testLogCommon -----");
        System.out.println("Let's write some items to the common log");
        for (int count = 1; count < 6; count++) {
            for (int i = 0; i < count; i ++) {
                logCommon(conn, "test", "message-" + count);
            }
        }
        Set<Tuple> common = conn.zrevrangeWithScores("common:test:info", 0, -1);
        System.out.println("The current number of common messages is: " + common.size());
        System.out.println("Those common messages are:");
        for (Tuple tuple : common){
            System.out.println("  " + tuple.getElement() + ", " + tuple.getScore());
        }
        assert common.size() >= 5;
    }

    /**
     * @author  dong
     * @create  2018/1/5 11:29
     * @desc 计数器
     **/
    public void testCounters(Jedis conn) throws InterruptedException
    {
        System.out.println("\n----- testCounters -----");
        System.out.println("Let's update some counters for now and a little in the future");
        long now = System.currentTimeMillis() / 1000;
        for (int i = 0; i < 10; i++) {
            int count = (int)(Math.random() * 5) + 1;
            updateCounter(conn, "test", count, now + i);
        }

        List<Pair<Integer,Integer>> counter = getCounter(conn, "test", 1);
        System.out.println("We have some per-second counters: " + counter.size());
        System.out.println("These counters include:");
        for (Pair<Integer,Integer> count : counter){
            System.out.println("  " + count);
        }
        assert counter.size() >= 10;

        counter = getCounter(conn, "test", 5);
        System.out.println("We have some per-5-second counters: " + counter.size());
        System.out.println("These counters include:");
        for (Pair<Integer,Integer> count : counter){
            System.out.println("  " + count);
        }
        assert counter.size() >= 2;
        System.out.println();

        System.out.println("Let's clean out some counters by setting our sample count to 0");
        CleanCountersThread thread = new CleanCountersThread(0, 2 * 86400000);
        thread.start();
        Thread.sleep(1000);
        thread.quit();
        thread.interrupt();
        counter = getCounter(conn, "test", 86400);
        System.out.println("Did we clean out all of the counters? " + (counter.size() == 0));
        assert counter.size() == 0;
    }

    public void logRecent(Jedis conn, String name, String message) {
        logRecent(conn, name, message, INFO);
    }

    public void logRecent(Jedis conn, String name, String message, String severity) {
        String destination = "recent:" + name + ':' + severity;
        Pipeline pipe = conn.pipelined();
        //记录日志
        pipe.lpush(destination, TIMESTAMP.format(new Date()) + ' ' + message);
        //修剪日记集合长度
        pipe.ltrim(destination, 0, 99);
        pipe.sync();
    }

    public void logCommon(Jedis conn, String name, String message) {
        logCommon(conn, name, message, INFO, 60000);
    }

    public void logCommon(Jedis conn, String name, String message, String severity, int timeout) {
        //创建zset key
        String commonDest = "common:" + name + ':' + severity;
        String startKey = commonDest + ":start";
        long end = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < end){
            conn.watch(startKey);
            String hourStart = ISO_FORMAT.format(new Date());
            String existing = conn.get(startKey);

            Transaction trans = conn.multi();
//            if (existing != null && COLLATOR.compare(existing, hourStart) < 0){
                trans.rename(commonDest, commonDest + ":last");
                trans.rename(startKey, commonDest + ":pstart");
                trans.set(startKey, hourStart);
//            }

            //第一次zset插入数据   初始得分为1
            trans.zincrby(commonDest, 1, message);

            //创建list key
            String recentDest = "recent:" + name + ':' + severity;
            //往列表插入数据
            trans.lpush(recentDest, TIMESTAMP.format(new Date()) + ' ' + message);
            //修剪列表长度
            trans.ltrim(recentDest, 0, 99);
            List<Object> results = trans.exec();
            // null response indicates that the transaction was aborted due to
            // the watched key changing.
            if (results == null){
                continue;
            }
            return;
        }
    }

    public void updateCounter(Jedis conn, String name, int count) {
        updateCounter(conn, name, count, System.currentTimeMillis() / 1000);
    }

    public static final int[] PRECISION = new int[]{1, 5, 60, 300, 3600, 18000, 86400};

    public void updateCounter(Jedis conn, String name, int count, long now){
        Transaction trans = conn.multi();
        for (int prec : PRECISION) {
            long pnow = (now / prec) * prec;
            System.out.println(pnow);
            String hash = String.valueOf(prec) + ':' + name;
            trans.zadd("known:", 0, hash);
            trans.hincrBy("count:" + hash, String.valueOf(pnow), count);
        }
        trans.exec();
    }

    public List<Pair<Integer,Integer>> getCounter(Jedis conn, String name, int precision) {
        String hash = String.valueOf(precision) + ':' + name;
        Map<String,String> data = conn.hgetAll("count:" + hash);
        ArrayList<Pair<Integer,Integer>> results =
                new ArrayList<Pair<Integer,Integer>>();
        for (Map.Entry<String,String> entry : data.entrySet()) {
            results.add(new Pair<Integer,Integer>(
                    Integer.parseInt(entry.getKey()),
                    Integer.parseInt(entry.getValue())));
        }
//        Collections.sort(results);
        return results;
    }

    public class CleanCountersThread extends Thread
    {
        private Jedis conn;
        private int sampleCount = 100;
        private boolean quit;
        private long timeOffset; // used to mimic a time in the future.

        public CleanCountersThread(int sampleCount, long timeOffset){
            this.conn = new Jedis("localhost");
            this.conn.select(15);
            this.sampleCount = sampleCount;
            this.timeOffset = timeOffset;
        }

        public void quit(){
            quit = true;
        }

        public void run(){
            int passes = 0;
            while (!quit){
                long start = System.currentTimeMillis() + timeOffset;
                int index = 0;
                while (index < conn.zcard("known:")){
                    Set<String> hashSet = conn.zrange("known:", index, index);
                    index++;
                    if (hashSet.size() == 0) {
                        break;
                    }
                    String hash = hashSet.iterator().next();
                    int prec = Integer.parseInt(hash.substring(0, hash.indexOf(':')));
                    int bprec = (int)Math.floor(prec / 60);
                    if (bprec == 0){
                        bprec = 1;
                    }
                    if ((passes % bprec) != 0){
                        continue;
                    }

                    String hkey = "count:" + hash;
                    String cutoff = String.valueOf(
                            ((System.currentTimeMillis() + timeOffset) / 1000) - sampleCount * prec);
                    ArrayList<String> samples = new ArrayList<String>(conn.hkeys(hkey));
                    Collections.sort(samples);
                    int remove = bisectRight(samples, cutoff);

                    if (remove != 0){
                        conn.hdel(hkey, samples.subList(0, remove).toArray(new String[0]));
                        if (remove == samples.size()){
                            conn.watch(hkey);
                            if (conn.hlen(hkey) == 0) {
                                Transaction trans = conn.multi();
                                trans.zrem("known:", hash);
                                trans.exec();
                                index--;
                            }else{
                                conn.unwatch();
                            }
                        }
                    }
                }

                passes++;
                long duration = Math.min(
                        (System.currentTimeMillis() + timeOffset) - start + 1000, 60000);
                try {
                    sleep(Math.max(60000 - duration, 1000));
                }catch(InterruptedException ie){
                    Thread.currentThread().interrupt();
                }
            }
        }

        // mimic python's bisect.bisect_right
        public int bisectRight(List<String> values, String key) {
            int index = Collections.binarySearch(values, key);
            return index < 0 ? Math.abs(index) - 1 : index + 1;
        }
    }
}
