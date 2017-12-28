package com.dongganen.redis.java;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        testShopppingCartCookies(conn);
    }


    /**
     * @author  dong
     * @create  2017/12/28 14:18
     * @desc 测试登录cookies
     **/
    private void testLoginCookies(Jedis conn) throws InterruptedException{
        System.out.println("--------------testLoginCookies--------------");

//        for (int i = 0; i < 10; i++){
            String token = UUID.randomUUID().toString();

            updateToken(conn, token, "username", "itemX");
//        }

        System.out.println("We just logged-in/updated token: " + token);
        System.out.println("For user: 'username'");
        System.out.println();

        System.out.println("What username do we get when we look-up that token?");
        String r = checkToken(conn, token);
        System.out.println(r);
        System.out.println();
        assert r != null;

        System.out.println("Let's drop the maximum number of cookies to 0 to clean them out");
        System.out.println("We will start a thread to do the cleaning, while we stop it later");

        CleanSessionsThread thread = new CleanSessionsThread(5);

        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()){
            throw new RuntimeException("The clean sessions thread is still alive?!?");
        }

        long s = conn.hlen("login:");
        System.out.println("The current number of sessions still available is: " + s);
        assert s == 0;
    }

    /**
     * @author  dong
     * @create  2017/12/28 14:19
     * @desc 更新token
     **/
    private void updateToken(Jedis conn, String token, String user, String item) {
        long timestamp = System.currentTimeMillis() / 1000;

        //散列   token -> user 关系
        conn.hset("login:", token, user);
        //有序集合   最近登录的人员列表
        conn.zadd("recent:", timestamp, token);
        if(item != null){
            //记录当前用户访问的页面
            for(int i = 0 ; i < 30; i++){
                conn.zadd("viewed:" + token, timestamp, item + i);
            }
            //最多存储最新的25条数据
            conn.zremrangeByRank("viewed:" + token, 0, -26);
            conn.zincrby("viewed:", -1, item);
        }
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
            this.conn = new Jedis("localhost");
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
    private void testShopppingCartCookies(Jedis conn) {
        String token = UUID.randomUUID().toString();
        updateToken(conn, token, "username", "itemX");
        addToCart(conn, token, "itemY", 0);
        Map<String, String> map = conn.hgetAll("car:" + token);
        for (Map.Entry<String, String> entry:map.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private void addToCart(Jedis conn, String session, String item, int count) {
        if(count <= 0){
            conn.hdel("car:" + session, item);
        }else {
            conn.hset("car:" + session, item, String.valueOf(count));
        }
    }

}


