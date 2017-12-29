package com.dongganen.redis.java;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author  dong
 * @create  2017/12/28 16:15
 * @desc 登录和cookie缓存    redis实现购物车
 **/
public class Chapter02 {

    public static void main(String[] args) throws InterruptedException{
        new Chapter02().run();
    }

    private void run() throws InterruptedException{
        //创建连接
        Jedis conn = new Jedis("192.168.0.135");
        conn.select(15);

        //登录和cookie缓存
//        testLoginCookies(conn);

        //redis实现购物车
//        String token = "a138ed4f-f2a4-4d25-a9fb-a14117d9e560";
//        testShopppingCartCookies(conn, token, "itemX", 5);

//        testCacheRows(conn);
        testCacheRequest(conn);
    }


    /**
     * @author  dong
     * @create  2017/12/28 14:18
     * @desc 测试登录cookies
     **/
    private void testLoginCookies(Jedis conn) throws InterruptedException{
        System.out.println("测试登录cookie");

        //用户登录
        System.out.println("=====用户开始登录");
        String token = UUID.randomUUID().toString();
        updateToken(conn, token, "dong");
        System.out.println(token + "=====登录成功");

        //用户浏览商品
        System.out.println("=====开始浏览商品");
        browseItem(conn, token, "itemX");
        System.out.println("=====浏览商品结束");


        String r = checkToken(conn, token);

        //执行清理操作
        System.out.println("=====清空最近登录的用户保留前limit名");
        CleanSessionsThread thread = new CleanSessionsThread(5);

        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);
//        if (thread.isAlive()){
//            throw new RuntimeException("The clean sessions thread is still alive?!?");
//        }
        System.out.println("=====获取缓存中最近登录的用户");
        long s = conn.hlen("login:");
        System.out.println("=====缓存中最近登录的用户为" + s);

    }



    /**
     * @author  dong
     * @create  2017/12/28 14:19
     * @desc 更新token
     **/
    private void updateToken(Jedis conn, String token, String user) {
        long timestamp = System.currentTimeMillis() / 1000;
        //散列   token -> user 关系
        conn.hset("login:", token, user);
        //有序集合   最近登录的人员列表
        conn.zadd("recent:", timestamp, token);
    }

    /**
     * @author  dong
     * @create  2017/12/29 10:35
     * @desc 用户浏览商品
     **/
    private void browseItem(Jedis conn, String token, String item) {
        long timestamp = System.currentTimeMillis() / 1000;
        conn.zadd("viewed:" + token, timestamp, item);
        //最多存储最新的25条数据
        conn.zremrangeByRank("viewed:" + token, 0, -26);
        conn.zincrby("viewed:", -1, item);
    }

    /**
     * @author  dong
     * @create  2017/12/28 14:22
     * @desc 根据token找user
     **/
    private String checkToken(Jedis conn, String token) {
        return conn.hget("login:", token);
    }

    /**
     * @author  dong
     * @create  2017/12/28 15:01
     * @desc 执行清理操作
     **/
    public class CleanSessionsThread extends Thread{

        private Jedis conn;
        private int limit;
        private boolean quit;

        public CleanSessionsThread(int limit){
            this.conn = new Jedis("192.168.0.135");
            this.conn.select(15);
            this.limit = limit;
        }

        public void quit(){
            quit = true;
        }

        public void run(){
            while (!quit){
                Long size = conn.zcard("recent:");
                if(size <= limit){
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                long endIndex = Math.min(size - limit, 100);
                Set<String> tokenSet = conn.zrange("recent:", 0, endIndex - 1);
                String[] tokens = tokenSet.toArray(new String[tokenSet.size()]);

                ArrayList<String> sessionKeys = new ArrayList<>();
                for (String token:tokens) {
                    sessionKeys.add("viewed:" + token);
                }

                conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
                conn.hdel("login:", tokens);
                conn.zrem("recent:", tokens);
            }
        }
    }

    /**
     * redis实现购物车
     * @param conn
     */
    private void testShopppingCartCookies(Jedis conn, String token, String item, Integer count) throws InterruptedException {
        System.out.println("=====将商品添加至购物车");
        addToCart(conn, token, item, count);
        Map<String, String> map = conn.hgetAll("car:" + token);
        for (Map.Entry<String, String> entry:map.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("=====结束将商品添加至购物车");

        CleanFullSessionsThread thread = new CleanFullSessionsThread(5);
        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);

        map = conn.hgetAll("cart:" + token);
        for (Map.Entry<String,String> entry : map.entrySet()){
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    /**
     * @author  dong
     * @create  2017/12/29 11:35
     * @desc 将商品添加至用户购物车
     **/
    private void addToCart(Jedis conn, String token, String item, int count) {
        if(count <= 0){
            conn.hdel("car:" + token, item);
        }else {
            conn.hset("car:" + token, item, String.valueOf(count));
        }
    }

    /**
     * 清理保留最近登录的人
     */
    public class CleanFullSessionsThread extends Thread{
        private Jedis conn;
        private int limit;
        private boolean quit;

        public CleanFullSessionsThread(int limit){
            this.conn = new Jedis("192.168.0.135");
            this.conn.select(15);
            this.limit = limit;
        }

        public void quit(){
            this.quit = true;
        }

        public void run() {
            while (!quit) {
                long size = conn.zcard("recent:");
                if (size <= limit){
                    try {
                        sleep(1000);
                    }catch(InterruptedException ie){
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                long endIndex = Math.min(size - limit, 100);
                Set<String> sessionSet = conn.zrange("recent:", 0, endIndex - 1);
                String[] sessions = sessionSet.toArray(new String[sessionSet.size()]);

                ArrayList<String> sessionKeys = new ArrayList<String>();
                for (String sess : sessions) {
                    sessionKeys.add("viewed:" + sess);
                    sessionKeys.add("cart:" + sess);
                }

                conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
                conn.hdel("login:", sessions);
                conn.zrem("recent:", sessions);
            }
        }
    }

    /**
     * 测试请购商品
     * @param conn
     * @throws InterruptedException
     */
    private void testCacheRows(Jedis conn) throws InterruptedException {
        scheduleRowCache(conn, "itemX", 5);
        Set<Tuple> tuples = conn.zrangeWithScores("schedule:", 0, -1);
        for (Tuple tuple:tuples){
            System.out.println("  " + tuple.getElement() + ", " + tuple.getScore());
        }

        CacheRowsThread thread = new CacheRowsThread();
        thread.start();

        Thread.sleep(1000);
        String r = conn.get("inv:itemX");
        System.out.println(r);
        Thread.sleep(5000);
        String r2 = conn.get("inv:itemX");
        System.out.println(r2);
        scheduleRowCache(conn, "itemX", -1);
        Thread.sleep(1000);
        r = conn.get("inv:itemX");
        System.out.println(r);
        thread.quit();
        Thread.sleep(2000);
    }

    /**
     * @author  dong
     * @create  2017/12/29 15:05
     * @desc delay和schedule
     **/
    private void scheduleRowCache(Jedis conn, String rowId, int delay) {
        conn.zadd("delay:", delay, rowId);
        conn.zadd("schedule:", System.currentTimeMillis() / 1000, rowId);
    }

    /**
     * @author  dong
     * @create  2017/12/29 15:06
     * @desc 抢商品的守护进程
     **/
    public class CacheRowsThread extends Thread{
        private Jedis conn;
        private boolean quit;

        CacheRowsThread(){
            this.conn = new Jedis("192.168.0.135");
            this.conn.select(15);
        }

        public void quit(){
            this.quit = true;
        }

        public void run(){
            System.out.println("---------------------------------");
            Gson gson = new Gson();
            while (!quit){
                System.out.println("===================================");
                Set<Tuple> range = conn.zrangeWithScores("schedule:", 0, 0);
                Tuple next = range.size() > 0 ? range.iterator().next() : null;
                long now = System.currentTimeMillis() / 1000;
                //判断是否更新商品
                if (next == null || next.getScore() > now){
                    try {
                        sleep(50);
                    }catch(InterruptedException ie){
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                String rowId  = next.getElement();
                double delay = conn.zscore("delay:", rowId);
                //判断是否已经被抢完
                if (delay <= 0) {
                    conn.zrem("delay:", rowId);
                    conn.zrem("schedule:", rowId);
                    conn.del("inv:" + rowId);
                    continue;
                }

                //更新商品信息
                Inventory row = Inventory.get(rowId);
                conn.zadd("schedule:", now + delay, rowId);
                conn.set("inv:" + rowId, gson.toJson(row));
            }
        }
    }

    /**
     * @author  dong
     * @create  2017/12/29 15:08
     * @desc 商品信息
     **/
    public static class Inventory {
        private String id;
        private String data;
        private long time;

        private Inventory (String id) {
            this.id = id;
            this.data = "data to cache...";
            this.time = System.currentTimeMillis() / 1000;
        }

        public static Inventory get(String id) {
            System.out.println("||");
            return new Inventory(id);
        }
    }

    /**
     * @author  dong
     * @create  2017/12/29 15:29
     * @desc 缓存前几名的页面
     **/
    public void testCacheRequest(Jedis conn) {
        System.out.println("\n----- testCacheRequest -----");
        String token = UUID.randomUUID().toString();

        Callback callback = new Callback(){
            public String call(String request){
                return "content for " + request;
            }
        };

//        updateToken(conn, token, "username", "itemX");
        String url = "http://test.com/?item=itemX";
        System.out.println("We are going to cache a simple request against " + url);
        String result = cacheRequest(conn, url, callback);
        System.out.println("We got initial content:\n" + result);
        System.out.println();

        assert result != null;

        System.out.println("To test that we've cached the request, we'll pass a bad callback");
        String result2 = cacheRequest(conn, url, null);
        System.out.println("We ended up getting the same response!\n" + result2);

        assert result.equals(result2);

        assert !canCache(conn, "http://test.com/");
        assert !canCache(conn, "http://test.com/?item=itemX&_=1234536");
    }

    /**
     * @author  dong
     * @create  2017/12/29 15:31
     * @desc 返回缓存/没缓存，设置缓存返回缓存
     **/
    public String cacheRequest(Jedis conn, String request, Callback callback) {
        if (!canCache(conn, request)){
            return callback != null ? callback.call(request) : null;
        }

        String pageKey = "cache:" + hashRequest(request);
        String content = conn.get(pageKey);

        if (content == null && callback != null){
            content = callback.call(request);
            conn.setex(pageKey, 300, content);
        }

        return content;
    }

    /**
     * @author  dong
     * @create  2017/12/29 15:30
     * @desc 判断该商品是否缓存
     **/
    public boolean canCache(Jedis conn, String request) {
        try {
            URL url = new URL(request);
            HashMap<String,String> params = new HashMap<String,String>();
            if (url.getQuery() != null){
                for (String param : url.getQuery().split("&")){
                    String[] pair = param.split("=", 2);
                    params.put(pair[0], pair.length == 2 ? pair[1] : null);
                }
            }

            String itemId = extractItemId(params);
            if (itemId == null || isDynamic(params)) {
                return false;
            }
            Long rank = conn.zrank("viewed:", itemId);
            return rank != null && rank < 10000;
        }catch(MalformedURLException mue){
            return false;
        }
    }

    public boolean isDynamic(Map<String,String> params) {
        return params.containsKey("_");
    }

    public String extractItemId(Map<String,String> params) {
        return params.get("item");
    }

    public String hashRequest(String request) {
        return String.valueOf(request.hashCode());
    }

    public interface Callback {
        public String call(String request);
    }

}


