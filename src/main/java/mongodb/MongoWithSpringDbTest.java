package mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.WriteResult;

import pojo.Address;
import pojo.Favorites;
import pojo.User;
/**
 * mongodb集成spring开发
 * @author Administrator
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class MongoWithSpringDbTest {
	
	@Resource
	private MongoOperations template;
	
	 @Test
    public void testInsert(){
    	User user = new User();
    	user.setUsername("cang");
    	user.setCountry("USA");
    	user.setAge(20);
    	user.setLenght(1.77);
    	user.setSalary(new BigDecimal("6265.22"));
    	Address address1 = new Address();
    	address1.setaCode("411222");
    	address1.setAdd("sdfsdf");
    	user.setAddress(address1);
    	Favorites favorites1 = new Favorites();
    	favorites1.setCites(Arrays.asList("东莞","东京"));
    	favorites1.setMovies(Arrays.asList("西游记","一路向西"));
    	user.setFavorites(favorites1);
    	
    	
    	User user1 = new User();
    	user1.setUsername("chen");
    	user1.setCountry("China");
    	user1.setAge(30);
    	user1.setLenght(1.77);
    	user1.setSalary(new BigDecimal("6885.22"));
    	Address address2 = new Address();
    	address2.setaCode("411000");
    	address2.setAdd("我的地址2");
    	user1.setAddress(address2);
    	Favorites favorites2 = new Favorites();
    	favorites2.setCites(Arrays.asList("珠海","东京"));
    	favorites2.setMovies(Arrays.asList("东游记","一路向东"));
    	user1.setFavorites(favorites2);
    	
    	template.insertAll(Arrays.asList(user,user1));
    }
	 
	@Test
	public void testDelete(){
		//delete from users where username = ‘lison’
		WriteResult remove = template.remove(query(where("username").is("lison")), User.class) ;
		System.out.println(remove.getN());
		//delete from users where >30 and age <35
		remove = template.remove(query(new Criteria().andOperator(where("age").gt(30),where("age").lt(35))
				), User.class);
		System.out.println(remove.getN());
	}
	
	@Test
	public void testUpdate(){
		//update  users  set age=6 where username = 'lison' 
		WriteResult update = template.updateMulti(query(where("username").is("lison")),update("age","6") , User.class);
		System.out.println(update.getN());
		//update users  set favorites.movies add "小电影2", "小电影3" where favorites.cites  has "东莞"
		Query query = query(where("favorites.cites").is("东莞"));
		Update upe = new Update().addToSet("favorites.movies").each("a片1","a片2");
		update = template.updateMulti(query, upe, User.class);
		System.out.println(update.getN());
	}
	
	@Test
	public void find(){
		//select * from users  where favorites.cites has "东莞"、"东京"
		List<User> result = this.template.find(query(where("favorites.cites").all("东莞","东京")), User.class);
		System.out.println(result.size());
		//select * from users  where username like '%s%' and (country= EngLish or country = USA)
		String regexStr = ".*s.*";
		Criteria queryLike = where("username").regex(regexStr);
		Criteria queryCountry = new Criteria().orOperator(
					where("country").is("english"),where("country").is("USA")
				);
		Query condition = query(new Criteria().andOperator(queryLike,queryCountry));
		result = this.template.find(condition, User.class);
		System.out.println(result.size());
	}
}