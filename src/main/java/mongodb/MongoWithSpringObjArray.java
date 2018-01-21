package mongodb;


import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.WriteResult;

import pojo.Comment;
import pojo.MyOrder;
import pojo.User;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class MongoWithSpringObjArray {

	@Resource
	private MongoOperations tempelate;
    
    //--------------------------------------insert demo--------------------------------------------------------------
    
    //��jack��ʦ����һ�����ۣ�$push��
    //db.users.updateOne({"username":"jack"},
//                       {"$push":{"comments":{"author":"lison23","content":"ydddyyytttt"}}})
    @Test
    public void addOneComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	Comment comment = new Comment();
    	comment.setAuthor("lison23");
    	comment.setContent("ydddyyytttt");
		Update push = new Update().push("comments", comment);
		WriteResult updateFirst = tempelate.updateFirst(query, push, User.class);
		System.out.println(updateFirst.getN());
    }
    
    
//    ��jack��ʦ���������������ۣ�$push,$each��
//    db.users.updateOne({"username":"jack"},     
//           {"$push":{"comments":
//                      {"$each":[{"author":"lison33","content":"lison33lison33"},
//                                      {"author":"lison44","content":"lison44lison44"}]}}})
    @Test
    public void addManyComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison55");
    	comment1.setContent("lison55lison55");
    	Comment comment2 = new Comment();
    	comment2.setAuthor("lison66");
    	comment2.setContent("lison66lison66");
		Update push = new Update().pushAll("comments", new Comment[]{comment1,comment2});
		WriteResult updateFirst = tempelate.updateFirst(query, push, User.class);
		System.out.println(updateFirst.getN());   	
    }
    
//    ��jack��ʦ���������������۲��������������$push,$eachm,$sort��
//    db.users.updateOne({"username":"jack"}, 
//          {"$push": {"comments":
//                    {"$each":[ {"author":"lison22","content":"yyyytttt"},
//                                    {"author":"lison23","content":"ydddyyytttt"} ], 
//                      $sort: {"author":1} } } })
    @Test
    public void addManySortComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison77");
    	comment1.setContent("lison55lison55");
    	Comment comment2 = new Comment();
    	comment2.setAuthor("lison88");
    	comment2.setContent("lison66lison66");
    	
    	
		Update update = new Update();
		PushOperatorBuilder pob = update.push("comments");
		pob.each(comment1,comment2);
		pob.sort(new Sort(new Sort.Order(Direction.DESC, "author")));
		
		System.out.println("---------------");
		WriteResult updateFirst = tempelate.updateFirst(query, update,User.class);
		System.out.println(updateFirst.getN());   
    }
 
    //--------------------------------------delete demo--------------------------------------------------------------
 
//    ɾ��lison1��jack���������� ������ɾ����
//    db.users.update({"username":��jack"},
//                               {"$pull":{"comments":{"author":"lison23"}}})

    @Test
    public void deleteByAuthorComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison55");
		Update pull = new Update().pull("comments",comment1);
		WriteResult updateFirst = tempelate.updateFirst(query, pull, User.class);
		System.out.println(updateFirst.getN());   	
    }
    
    
//    ɾ��lison5��lison����Ϊ��lison�ǲ���ʦ��С�Եܡ������ۣ���ȷɾ����
//    db.users.update({"username":"lison"},
//            {"$pull":{"comments":{"author":"lison5",
//                                  "content":"lison�ǲ���ʦ��С�Ե�"}}})
    @Test
    public void deleteByAuthorContentComment(){
    	Query query = query(Criteria.where("username").is("lison"));
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison5");
    	comment1.setContent("lison�ǲ���ʦ��С�Ե�");
		Update pull = new Update().pull("comments",comment1);
		WriteResult updateFirst = tempelate.updateFirst(query, pull, User.class);
		System.out.println(updateFirst.getN());  
    }
    
    //--------------------------------------update demo--------------------------------------------------------------
//    db.users.updateMany({"username":"jack","comments.author":"lison1"},
//            {"$set":{"comments.$.content":"xxoo",
//                        "comments.$.author":"lison10" }})
//    	���壺��ȷ�޸�ĳ��ĳһ����ȷ�����ۣ�����ж���������������ݣ����޸����һ�����ݡ��޷������޸�����Ԫ��
  @Test
  public void updateOneComment(){
  		Query query = query(where("username").is("lison").and("comments.author").is("lison4"));
  		Update update = update("comments.$.content","xxoo").set("comments.$.author","lison11");
		WriteResult updateFirst = tempelate.updateFirst(query, update, User.class);
		System.out.println(updateFirst.getN());  
  }

//--------------------------------------findandModify demo--------------------------------------------------------------
//ʹ��findandModify�������޸�����ͬʱ���ظ���ǰ�����ݻ���º������
//db.num.findAndModify({
//      	"query":{"_id":ObjectId("5a58cef99506c50abaeb4384")},
//		  	"update":{"$inc":{"saleOrder":1}},
//		  	"new":true
//	     })
	@Test
	public void findAndModifyTest(){
		Query query = query(where("_id").is(new ObjectId("5a5a3a87d7598152855d758e")));
		Update update = new Update().inc("saleOrder", 1);
		FindAndModifyOptions famo = FindAndModifyOptions.options().returnNew(true);
		
		MyOrder ret = tempelate.findAndModify(query, update,famo, MyOrder.class);
		System.out.println(ret.getSaleOrder());
	}
}
