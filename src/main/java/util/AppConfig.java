package util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

@Configuration
//@PropertySource("classpath:mongo.conf") 通过注解加载资源文件,也可以通过property-placeholder配置
public class AppConfig {
	@Value("${ip}")
	private String ip;
	@Value("${port}")
	private Integer port;
	
	@Bean
	public MongoClient mongoClient(){
		MongoClientOptions options = MongoClientOptions.builder()
			.writeConcern(WriteConcern.ACKNOWLEDGED)
			.connectionsPerHost(100)
			.threadsAllowedToBlockForConnectionMultiplier(5)
			.maxWaitTime(12000)
			.connectTimeout(10000)
			.build();
		ServerAddress server = new ServerAddress(ip, port);
		MongoClient client = new MongoClient(server,options);
		return client;
	}
}
