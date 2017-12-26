package com.dongganen.redis.java;

import redis.clients.jedis.Jedis;

import java.util.HashMap;

public class Chapter01 {

    private static final int ONE_WEEK_IN_SECONDS = 7 * 86400;
    private static final int VOTE_SCORE = 432;

    public static void main(String[] args) {
        new Chapter01().run();
    }

    public void run(){
        //创建连接
        Jedis conn = new Jedis("192.168.0.135");
        //选择数据库
        conn.select(15);

        //发布文章
        String articleId = postArticle(
                conn, "username", "A title", "http://www.google.com");
    }

    /**
     * 发布文章
     * @param conn
     * @param user
     * @param title
     * @param link
     * @return
     */
    public String postArticle(Jedis conn, String user, String title, String link){
        //新增文章, 文章编号递增
        String articleId = String.valueOf(conn.incr("article:"));

        //创建文章对应的投票人数(set)
        String voted = "voted:" + articleId;
        conn.sadd(voted, user);
        //设置过期时间，7天
        conn.expire(voted, ONE_WEEK_IN_SECONDS);

        //存储文章基本信息（hash）
        long now = System.currentTimeMillis() / 1000;
        String article = "article:" + articleId;
        HashMap<String, String> articleData = new HashMap<>();
        articleData.put("title", title);
        articleData.put("link", link);
        articleData.put("user", user);
        articleData.put("now", String.valueOf(now));
        articleData.put("votes", "1");
        conn.hmset(article, articleData);

        //文章发布时间, 文章总得分（zset）
        conn.zadd("score", now + VOTE_SCORE, article);
        conn.zadd("time", now, article);

        return articleId;
    }
}
