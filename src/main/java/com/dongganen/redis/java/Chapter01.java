package com.dongganen.redis.java;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

import java.util.*;

public class Chapter01 {

    private static final int ONE_WEEK_IN_SECONDS = 7 * 86400;
    private static final int VOTE_SCORE = 432;
    private static final int ARTICLES_PER_PAGE = 25;

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
                conn, "dong", "A title", "http://www.google.com");

        System.out.println("We posted a new article with id:"  + articleId);
        System.out.println("Its HASH looks like:");

        //hash 获取文章基本信息
        Map<String, String> articleData  = conn.hgetAll("article:" + articleId);
        for(Map.Entry<String, String> entry : articleData.entrySet()){
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("-------------------------------------------");

        // 给文章投票
        articleVote(conn, "feng", "article:" + articleId);
        articleVote(conn, "feng", "article:" + articleId);

        // 获取文章投票数
        String votes = conn.hget("article:" + articleId, "votes");
        System.out.println("We voted for the article, it now has votes: " + votes);
        assert Integer.parseInt(votes) > 1;

        System.out.println("The currently highest-scoring articles are:");
        List<Map<String,String>> articles = getArticles(conn, 1);
        printArticles(articles);
        assert articles.size() >= 1;

        //对文章进行分组
        addGroups(conn, articleId, new String[]{"new-group"});
        System.out.println("We added the article to a new group, other articles include:");
        articles = getGroupArticles(conn, "new-group", 1);
        printArticles(articles);
        assert articles.size() >= 1;

    }

    public List<Map<String,String>> getGroupArticles(Jedis conn, String group, int page) {
        return getGroupArticles(conn, group, page, "score");
    }

    /**
     * set（group:new-group）文章分组 && sset(score)文章得分 取交集排序
     * @param conn
     * @param group
     * @param page
     * @param order
     * @return
     */
    public List<Map<String,String>> getGroupArticles(Jedis conn, String group, int page, String order) {
        String key = order + ":" + group;
        if (!conn.exists(key)) {
            ZParams params = new ZParams().aggregate(ZParams.Aggregate.MAX);
            conn.zinterstore(key, params, "group:" + group, order);
            conn.expire(key, 60);
        }
        return getArticles(conn, page, key);
    }

    /**
     * 把文章添加到相应的组
     * @param conn
     * @param articleId
     * @param toAdd
     */
    private void addGroups(Jedis conn, String articleId, String[] toAdd) {
        String article = "article:" + articleId;
        for (String group : toAdd) {
            conn.sadd("group:" + group, article);
        }
    }

    /**
     * 打印文章基本信息
     * @param articles
     */
    private void printArticles(List<Map<String, String>> articles) {
        for (Map<String,String> article : articles){
            System.out.println("  id: " + article.get("id"));
            for (Map.Entry<String,String> entry : article.entrySet()){
                if (entry.getKey().equals("id")){
                    continue;
                }
                System.out.println("    " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    private List<Map<String,String>> getArticles(Jedis conn, int page) {
        return getArticles(conn, page, "score");
    }

    /**
     * 获取文章score从高到底排名（分页）
     * @param conn
     * @param page
     * @param order
     * @return
     */
    public List<Map<String,String>> getArticles(Jedis conn, int page, String order) {
        int start = (page - 1) * ARTICLES_PER_PAGE;
        int end = start + ARTICLES_PER_PAGE - 1;
        Set<String> ids = conn.zrevrange(order, start, end);
        List<Map<String,String>> articles = new ArrayList<Map<String,String>>();
        for(String id: ids){
            Map<String, String> articleData = conn.hgetAll(id);
            articleData.put("id", id);
            articles.add(articleData);
        }
        return articles;
    }

    /**
     * 给文章投票
     * @param conn
     * @param user
     * @param article
     */
    private void articleVote(Jedis conn, String user, String article) {
        // 计算时间和文章发布时间做比对，看是否过期投票
        long cutoff = (System.currentTimeMillis() / 1000) - ONE_WEEK_IN_SECONDS;
        if(conn.zscore("time", article) < cutoff){
            return;
        }
        String articleId = article.substring(article.indexOf(":") + 1);
        //判断是否重复投票
        if(conn.sadd("voted:" + articleId, user) == 1){
            //更新szet score文章得分
            conn.zincrby("score", VOTE_SCORE, article);
            //更新hash 增加文章votes属性votes
            conn.hincrBy(article, "votes", 1l);
        }
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
