package mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
/**
 * 集用spring用原生的mongoclient开发
 * @author Administrator
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class MongoWithSpring2 {
	@Resource
	private MongoClient client;   //通过AppCofig类在spring容器中创建一个mongoclient对象并注入
	
	private MongoCollection<Document> collections;
	
	private MongoDatabase db;
	
	@Before
	public void init(){
		db = client.getDatabase("bernard");
		collections = db.getCollection("users2");
	}
	
	/**测试elemMatch操作符，数组中对象数据要符合查询对象里面所有的字段
    	查找lison5评语为“lison是苍老师的小迷弟”的人
    	db.users.find({"comments":
    					{"$elemMatch":
    						{"author" : "lison5","content" : "lison是苍老师的小迷弟"}
    						}
    				}).pretty()
   */
	@Test
	public void testElemMatch(){
		final List<Document> retList = new ArrayList<>();
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				System.out.println(t.toJson());
				retList.add(t);
			}
		};
		
		Document filter = new Document().append("author", "lison5")
				.append("content", "lison是苍老师的小迷弟");
		Bson elementMatch = Filters.elemMatch("comments", filter);
		
		FindIterable<Document> find = collections.find(elementMatch);
		find.forEach(printBlock);
	}
	
	/**
	 *新增评论时，使用$sort运算符进行排序，插入评论后，再按照评论时间降序排序
	 * 
	 *db.users.updateOne({"username":"lison",},
					{
					  "$push": {
						 "comments": {
						   $each: [
								{
									"author" : "bernard",
									"content" : "我来了！",
									"commentTime" : ISODate("2018-01-06T04:26:18.354Z")
								}
							],
						   $sort: {"commentTime":-1}
						 }
					  }
					}
				);
	 * 
	 */
	@Test
	public void testAddCommentsAndSort(){
		Bson filter = Filters.eq("username","lison");	//过滤条件
		Document comment = new Document().append("author", "bernard")
				.append("content", "我来了....").append("commentTime", new Date());	//待插入的评论
		Document sortDoc = new Document().append("commentTime", -1);	//排序字段
		PushOptions options = new PushOptions().sortDocument(sortDoc);	
		Bson pushEach = Updates.pushEach("comments", Arrays.asList(comment), options);
		UpdateResult updateOne = collections.updateOne(filter,pushEach);
		System.out.println(updateOne.getModifiedCount());
	}
	
	
	/**
	 * 点击下一次重新加载评论数据
	 */
	public void testReloadComments(){
		
	}
	
}
