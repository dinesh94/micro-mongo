/**
 * 
 */
package generic.mongo.microservices.api.v1.aop;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

/**
 * @author Dinesh
 *
 */
@Aspect
@Component
public class CollectionDBCreatorAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionDBCreatorAspect.class);

	@Resource
	MongoClient mongoClient;

	@Before("execution(* generic.mongo.microservices.api.v1.*.*(..))")
	public void logRestAPICall(JoinPoint joinPoint) {
		LOGGER.trace("Start logRestAPICall : " + joinPoint.getSignature().getName());
		Object[] methodParam = joinPoint.getArgs();

		if (methodParam != null && methodParam.length > 1) {
			String dbName = methodParam[0].toString();
			String collectionName = methodParam[1].toString();

			boolean isDatabaseExist = isDatabaseExist(dbName);
			if (isDatabaseExist == false) {
				LOGGER.trace("Database {} does not exis & hence created new", dbName);
				mongoClient.getDatabase(dbName);
			}

			boolean isCollectionExist = isCollectionExist(dbName, collectionName);

			if (isCollectionExist == false) {
				MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
				mongoDatabase.createCollection(collectionName);
				MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
				collection.createIndex(new Document("$**", "text"));// This is required for full test search
				LOGGER.trace("Cellection {} does not exis & hence created new inside database {}", collectionName, dbName);
			}

		}
		LOGGER.trace("End logRestAPICall : " + joinPoint.getSignature().getName());
	}

	/**
	 * @param dbName
	 * @param collectionName
	 * @return boolean
	 */
	private boolean isCollectionExist(String dbName, String collectionName) {
		final Set<String> existingCollectionsInDB = new HashSet<>();
		MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
		MongoIterable<String> iterator = mongoDatabase.listCollectionNames();
		iterator.forEach(new Block<String>() {
			@Override
			public void apply(final String collectionName) {
				existingCollectionsInDB.add(collectionName);
			}
		});

		return existingCollectionsInDB.contains(collectionName);
	}

	/**
	 * @param dbName
	 * @return boolean
	 */
	private boolean isDatabaseExist(String dbName) {
		final Set<String> existingDatabases = new HashSet<>();
		MongoIterable<String> iterator = mongoClient.listDatabaseNames();
		iterator.forEach(new Block<String>() {
			@Override
			public void apply(final String collectionName) {
				existingDatabases.add(collectionName);
			}
		});

		return existingDatabases.contains(dbName);
	}

}
