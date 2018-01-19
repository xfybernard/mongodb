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
 * ����spring��ԭ����mongoclient����
 * @author Administrator
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class MongoWithSpring2 {
	@Resource
	private MongoClient client;   //ͨ��AppCofig����spring�����д���һ��mongoclient����ע��
	
	private MongoCollection<Document> collections;
	
	private MongoDatabase db;
	
	@Before
	public void init(){
		db = client.getDatabase("bernard");
		collections = db.getCollection("users2");
	}
	
	/**����elemMatch�������������ж�������Ҫ���ϲ�ѯ�����������е��ֶ�
    	����lison5����Ϊ��lison�ǲ���ʦ��С�Եܡ�����
    	db.users.find({"comments":
    					{"$elemMatch":
    						{"author" : "lison5","content" : "lison�ǲ���ʦ��С�Ե�"}
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
				.append("content", "lison�ǲ���ʦ��С�Ե�");
		Bson elementMatch = Filters.elemMatch("comments", filter);
		
		FindIterable<Document> find = collections.find(elementMatch);
		find.forEach(printBlock);
	}
	
	/**
	 *��������ʱ��ʹ��$sort������������򣬲������ۺ��ٰ�������ʱ�併������
	 * 
	 *db.users.updateOne({"username":"lison",},
					{
					  "$push": {
						 "comments": {
						   $each: [
								{
									"author" : "bernard",
									"content" : "�����ˣ�",
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
		Bson filter = Filters.eq("username","lison");	//��������
		Document comment = new Document().append("author", "bernard")
				.append("content", "������....").append("commentTime", new Date());	//�����������
		Document sortDoc = new Document().append("commentTime", -1);	//�����ֶ�
		PushOptions options = new PushOptions().sortDocument(sortDoc);	
		Bson pushEach = Updates.pushEach("comments", Arrays.asList(comment), options);
		UpdateResult updateOne = collections.updateOne(filter,pushEach);
		System.out.println(updateOne.getModifiedCount());
	}
	
	
	/**
	 * �����һ�����¼�����������
	 */
	public void testReloadComments(){
		
	}
	
}
