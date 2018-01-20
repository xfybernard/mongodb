package mongodb;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.WriteResult;

import pojo.Comments;
import pojo.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class MongoWithSpringDbTest2 {

	@Resource
	private MongoOperations tempelate;

	@Test
	// db.users.find({"comments":{"$elemMatch":{"author" : "lison5","content" :
	// "lison�ǲ���ʦ��С�Ե�"}}}) .pretty()
	public void testElemMatch() {
		Query query = query(where("comments").elemMatch(where("author").is("lison5").and("content").is("lison�ǲ���ʦ��С�Ե�")));
		List<User> find = tempelate.find(query, User.class);
		System.out.println(find.size());

	}

    /**
     * 			db.users.updateOne({"username":"lison",},
					{"$push": {
						 "comments": {
						   $each: [{
									"author" : "james",
									"content" : "lison�Ǹ�����ʦ��",
									"commentTime" : ISODate("2018-01-06T04:26:18.354Z")
								}
							],
						   $sort: {"commentTime":-1}
						 }}});
     */
	@Test
	// ��������ʱ��ʹ��$sort������������򣬲������ۺ��ٰ�������ʱ�併������
	public void demoStep1() {
		Query query = query(where("username").is("lison"));
		Comments comment = new Comments();
		comment.setAuthor("cang");
		comment.setCommentTime(new Date());
		comment.setContent("lison���ҵķ�˿");

		Update update = new Update();
		PushOperatorBuilder pob = update.push("comments");
		pob.each(comment);
		pob.sort(new Sort(new Sort.Order(Direction.DESC, "commentTime")));
		
		System.out.println("---------------");
		WriteResult updateFirst = tempelate.updateFirst(query, update,User.class);
		System.out.println("---------------");
		System.out.println(updateFirst.getN());
	}

	@Test
	// �鿴��Աʱ�������µ��������ۣ�
	// db.users.find({"username":"lison"},{"comments":{"$slice":[0,3]}}).pretty()
	public void demoStep2() {
		//{"username":"lison"}
		Query query = query(where("username").is("lison"));
		//{"comments":{"$slice":[0,3]}
		query.fields().include("comments").slice("comments", 0, 3);
		System.out.println("---------------");
		List<User> find = tempelate.find(query, User.class);
		System.out.println("---------------");
		System.out.println(find);
	}

	@Test
	// ������۵���һҳ��ť���¼�����������
	// db.users.find({"username":"lison"},{"comments":{"$slice":[3,3]},"$id":1}).pretty();
	public void demoStep3() {
		Query query = query(where("username").is("lison"));
		query.fields().include("comments").slice("comments", 3, 3)
				.include("id");
		System.out.println("---------------");
		List<User> find = tempelate.find(query, User.class);
		System.out.println("---------------");
		System.out.println(find);
	}

	
	/**
	 * db.users.aggregate([{"$match":{"username":"lison"}},
	                       {"$unwind":"$comments"},
	                       {$sort:{"comments.commentTime":-1}},
	                       {"$project":{"comments":1}},
	                       {"$skip":6},
	                       {"$limit":3}])
	                       
	 */
	// ����ж�������������ô����,ʹ�þۺ�
	@Test
	public void demoStep4() {
		Aggregation aggs = newAggregation(
				match(where("username").is("lison")),
				unwind("comments"),
				sort(Direction.ASC, "comments.commentTime"),
				project("comments"), 
				skip(6l), 
				limit(3));
		System.out.println("---------------");
		AggregationResults<Object> aggregate = tempelate.aggregate(aggs, "users",	Object.class);
		System.out.println("---------------");
		List<Object> mappedResults = aggregate.getMappedResults();
		System.out.println(mappedResults.size());

	}
	
	@Test
	//(1)ע����ص�ʵ��beanҪ����ע��@document��@dbRef
	//(2)spring��dbRef�����˷�װ�����������β�ѯ����
	public void dbRefTest(){
		System.out.println("----------------------------");
		List<User> users = tempelate.findAll(User.class);
		System.out.println("----------------------------");
		System.out.println(users);
//		System.out.println(users.get(0).getComments());
	}

}