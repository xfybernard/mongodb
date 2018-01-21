package mongodb;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * 使用原生的jar操作mongodb(增删改查) ,基于document
 * @author Administrator
 *
 */
public class MongoDocumentDbTest {

	private MongoDatabase db;

	private MongoCollection<Document> collections;

	private MongoClient client;

	@Before
	public void init() {
		client = new MongoClient("192.168.130.129", 27022);
		db = client.getDatabase("bernard");
		collections = db.getCollection("users");
		//连接需要受权的 数据库 createCredential(用户名,数据库名,密码)
		/*MongoCredential credential = MongoCredential.
				createCredential("bernard","bernard","bernard".toCharArray());
		client = new MongoClient(new ServerAddress("192.168.130.129", 27022),
				Arrays.asList(credential));
		*/
	}

	/**
	 * select * from users where favorites.cites has "东莞"、"东京" sql1
		select * from users where username like '%s%' 
		and (country= English or country = USA) sql2
	 */
	@Test
	public void query() {
		final List<Document> retList = new ArrayList<>();
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				System.out.println(t.toJson());
				retList.add(t);
			}
		};
		FindIterable<Document> find = collections.find(Filters.all("favorites.cites", Arrays.asList("东莞","东京")));
		find.forEach(printBlock);
		System.out.println("query sql1 结果数:" + retList.size());
		retList.removeAll(retList);
		
		String regexStr = ".*s.*";
		Bson c1 = Filters.regex("username", regexStr);
		Bson c2	= Filters.or(Filters.eq("country","English"),Filters.eq("country","USA"));
		find = collections.find(Filters.and(c1,c2));
		find.forEach(printBlock);
		System.out.println("query sql2 结果数:" + retList.size());
	}

	/**
	 * 往mongodb插入一条记录
	 */
	@Test
	public void insert() {
		Map<String, Object> docMap = new HashMap<>();
		docMap.put("username", "bernard");
		docMap.put("country", "china");
		docMap.put("lenght", 18);
		Map<String, Object> addressMap = new HashMap<>(); // 地址 子文档
		addressMap.put("aCode", "5100800");
		addressMap.put("add", "深圳");
		Map<String, Object> favoritesMap = new HashMap<>();
		favoritesMap.put("movies", Arrays.asList("小电影1", "小电影2", "小电影3")); // 数组集合元素
		favoritesMap.put("cites", Arrays.asList("深圳", "荆州", "长沙")); // 数据集合元素
		Document document = new Document(docMap); // 通过map初始化文档
		document.put("age", 32);
		document.put("address", addressMap);
		document.put("favorites", favoritesMap);
		collections.insertMany(Arrays.asList(document));
	}

	/**
	 * 删除mongodb的一条记录
	 */
	@Test
	public void delete() {
		DeleteResult result = collections.deleteOne(Filters.eq("username", "123"));
		System.out.println("删除username='123'影响记录数:" + result.getDeletedCount());

		result = collections.deleteMany(Filters.and(Filters.gt("age", 40), Filters.lte("age", 50)));
		System.out.println("删除age>40 and age<=50 影响记录数:" + result.getDeletedCount());

		result = collections.deleteMany(Filters.in("username", Arrays.asList("133", "456")));
		System.out.println("删除username in (133,456) 影响记录数:" + result.getDeletedCount());
	}

	/**
	 * 更新mongodb中的记录 
	 * update users set age=6 where username = 'lison'   语句1 
	 * update users set favorites.movies add "小电影2 ", "小电影3" where favorites.cites has
	 * "东莞"  语句2 
	 */
	@Test
	public void update() {
		UpdateResult result =  collections.updateMany(Filters.eq("username", "lison"), 
				new Document("$set", new Document("age", 6)));
		System.out.println("语句1 影响记录数" + result.getMatchedCount()+"\t" + result.getModifiedCount());
		
		result = collections.updateMany(Filters.eq("favorites.cites", "东莞"),
		Updates.addEachToSet("favorites.movies", Arrays.asList("小电影2","小电影3")));
		System.out.println("语句2 影响记录数" + result.getMatchedCount()+"\t" + result.getModifiedCount());
	}
	
}
