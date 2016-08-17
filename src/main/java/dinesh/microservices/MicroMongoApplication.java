/*
 * micro-mongo
 * ----------------------------------------------------------------
 * Version v0.1
 * Date Aug 16, 2016
 * Author Dinesh B
 * ----------------------------------------------------------------
 * List of Authors & Email:
 * Dinesh -> dinesh.bhavsar@siemens.com
 * Sid -> siddhartha.motghare@siemens.com
 * ----------------------------------------------------------------
 * 1. 1-Nov-2014 Dinesh B Initial version
 * ----------------------------------------------------------------
 * (C) 2014, Siemens Building Technologies, Inc.
 * ----------------------------------------------------------------
 */

package dinesh.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * @classDescription
 */
@Configuration
@SpringBootApplication
@ComponentScan(basePackageClasses = BasePackage.class)
@EnableAutoConfiguration(exclude={RabbitAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class, EmbeddedMongoProperties.class, DataSourceAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MicroMongoApplication {

	public static void main(String[] args) throws Exception {
		System.setProperty("spring.config.name", "micro-mongo");
		SpringApplication.run(MicroMongoApplication.class, args);
	}

  @Bean
  public MongoClient mongoClient() throws Exception {
	  //MongoCredential credential = MongoCredential.createCredential(userName, database, password);
	  //MongoClient mongoClient = new MongoClient(new ServerAddress("localhost", 27017));//, Arrays.asList(credential));
	  return new MongoClient(new ServerAddress("localhost", 27017));
  }
 
}