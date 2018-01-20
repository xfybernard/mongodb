package mongodb;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
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
	//	collections = db.getCollection("users");  ����dbrefʱ���ò�ע����һ��
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
	 * �鿴��Աʱ�������µ���������(������Ա��Ϣ����������)
	 *    //db.users.find({"username":"lison"},{"comments":{"$slice":[0,3]}}).pretty()
	 */
	@Test
	public void testReloadComments(){
		final List<Document> retList = new ArrayList<>();
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				System.out.println(t.toJson());
				retList.add(t);
			}
		};
		FindIterable<Document> find = collections.find(Filters.eq("username", "lison"))
				.projection(Projections.slice("comments", 0, 3));
		find.forEach(printBlock);
	}
	
	/**
	 * �鿴��Աʱ�������µ���������(����ѯ��������)��
	 * db.users.find({"username":"lison"},{"comments":{"$slice":[3,3]},"$id":1}).pretty();
	 */
	@Test
	public void testOnlyComments(){
		final List<Document> retList = new ArrayList<>();
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				System.out.println(t.toJson());
				retList.add(t);
			}
		};
		Bson filter = Filters.eq("username", "lison");
		Bson slice = Projections.slice("comments", 3, 3);
		Bson id = Projections.include("id");
		Bson projections = Projections.fields(slice,id);
		FindIterable<Document> find = collections.find(filter).projection(projections);
		find.forEach(printBlock);
	}
	
	/**
	 * ref
	 */
	@Test
	public void testDbRef(){
		final List<Document> retList = new ArrayList<>();
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				System.out.println(t.get("comments"));
				retList.add(t);
			}
		};
		FindIterable<Document> find = collections.find(Filters.eq("username", "lison"));
		find.forEach(printBlock);
	}
	
	
    //����ж�������������ô����,ʹ�þۺ�
	@Test
    public void demoStep4(){
    	final List<Document> retList = new ArrayList<>();
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				System.out.println(t.toJson());
				retList.add(t);
			}
		};
		List<Bson> aggregates = new ArrayList<>();
    	aggregates.add(match(eq("username","lison")));
    	aggregates.add(unwind("$comments"));
    	aggregates.add(sort(orderBy(ascending("comments.commentTime"))));
    	aggregates.add(project(fields(include("comments"))));
    	aggregates.add(skip(0));
    	aggregates.add(limit(3));
    	
    	AggregateIterable<Document> aggregate = collections.aggregate(aggregates);
    	aggregate.forEach(printBlock);
    }
}
