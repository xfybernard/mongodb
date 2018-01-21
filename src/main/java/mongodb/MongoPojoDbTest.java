package mongodb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;

import pojo.Address;
import pojo.Favorites;
import pojo.User;
/**
 * ʹ��ԭ����jar����mongodb(��ɾ�Ĳ�) ,����pojo
 * @author Administrator
 *
 */
public class MongoPojoDbTest {
	private MongoDatabase db;
	private MongoCollection<User> collections;
	private MongoClient client;
	
	@Before
	public void init() {
		//ע��pojo�������
		List<CodecRegistry>  codecRegistryList = new ArrayList<>();
		codecRegistryList.add(MongoClient.getDefaultCodecRegistry());
		CodecRegistry pojoProviders = CodecRegistries.fromProviders(
				PojoCodecProvider.builder().automatic(true).build());
		codecRegistryList.add(pojoProviders);
		CodecRegistry register = CodecRegistries.fromRegistries(codecRegistryList);
		ServerAddress addr = new ServerAddress("192.168.130.129", 27022);
		client = new MongoClient(addr,MongoClientOptions.builder()
				.codecRegistry(register).build());
		db = client.getDatabase("bernard");
		collections = db.getCollection("users", User.class);
	}
	
	@Test
	public void insert(){
		User user = new User();
    	user.setUsername("cang");
    	user.setCountry("USA");
    	user.setAge(20);
    	user.setLenght(1.77f);
    	user.setSalary(new BigDecimal("6265.22"));
    	Address address1 = new Address();
    	address1.setaCode("411222");
    	address1.setAdd("sdfsdf");
    	user.setAddress(address1);
    	Favorites favorites1 = new Favorites();
    	favorites1.setCites(Arrays.asList("��ݸ","����"));
    	favorites1.setMovies(Arrays.asList("���μ�","һ·����"));
    	user.setFavorites(favorites1);
    	
    	
    	User user1 = new User();
    	user1.setUsername("chen");
    	user1.setCountry("China");
    	user1.setAge(30);
    	user1.setLenght(1.77f);
    	user1.setSalary(new BigDecimal("6885.22"));
    	Address address2 = new Address();
    	address2.setaCode("411000");
    	address2.setAdd("�ҵĵ�ַ2");
    	user1.setAddress(address2);
    	Favorites favorites2 = new Favorites();
    	favorites2.setCites(Arrays.asList("�麣","����"));
    	favorites2.setMovies(Arrays.asList("���μ�","һ·��"));
    	user1.setFavorites(favorites2);
    	
    	this.collections.insertMany(Arrays.asList(user1,user));
	}
	
	@Test
	public void delete(){
		//delete from users where username = ��cang��
		DeleteResult result = this.collections.deleteMany(Filters.eq("username", "cang"));
		System.out.println("ɾ����¼��sql1:" + result.getDeletedCount());
		//delete from users where age >30 and age <35
		result = this.collections.deleteMany(Filters.and(
				Filters.gt("age", 30),Filters.lt("age", 35)
		));
		System.out.println("ɾ����¼��sql2:" + result.getDeletedCount());
	}
	
	@Test
	public void testUpdate(){
		//update  users  set age=35 where username = 'lison' 
		this.collections.updateMany(Filters.eq("username", "lison"),
		
			new Document("$set", new Document("age", 35)));
		//update users  set favorites.movies add "С��Ӱ2 ", "С��Ӱ3"
		//where favorites.cites  has "��ݸ"
		this.collections.updateMany(Filters.eq("favorites.cites", "��ݸ"),
				Updates.addEachToSet("favorites.movies", Arrays.asList("С��Ӱ2","С��Ӱ3")));
	}
	
	@Test
	public void find(){
		final List<User> list = new ArrayList<>();
		final Block<User> printBlock = new Block<User>(){
			@Override
			public void apply(User t) {
				System.out.println(t);
				list.add(t);
			}
		};
		
		//select * from users  where favorites.cites has "��ݸ"��"����"
		FindIterable<User> userList = this.collections.find(Filters.all("favorites.cites", "��ݸ","����"));
		userList.forEach(printBlock);
		list.removeAll(list);
		
		//select * from users  where username like '%s%' and 
		//(country= English or country = USA)
		String regexStr = ".*s.*";
		Bson c1 = Filters.regex("username", regexStr);
		Bson c2	= Filters.or(Filters.eq("country","English"),Filters.eq("country","USA"));
		userList = this.collections.find(Filters.and(c1,c2));
		userList.forEach(printBlock);
	}
}
