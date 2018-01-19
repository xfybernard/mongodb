package util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

@Configuration
//@PropertySource("classpath:mongo.conf") ͨ��ע�������Դ�ļ�,Ҳ����ͨ��property-placeholder����
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
