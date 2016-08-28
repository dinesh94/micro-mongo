/**
 * 
 */
package generic.mongo.microservices.api.v1;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClient;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;

/**
 * @author Dinesh
 *
 */
@RestController
@RequestMapping("api/v1/*")
public class ConstraintsController {

	@Resource
	MongoClient mongoClient;

	@RequestMapping(method = { RequestMethod.POST }, value = "/dbs/{db}/{collection}/{field}/unique")
	public synchronized ResponseEntity<?> unique(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("field") String field) {
		Document index = new Document(field, 1);
		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		collection.createIndex(index, new IndexOptions().unique(true));
		return listIndexes(dbName, collectionName);
	}
	
	@RequestMapping(method = { RequestMethod.POST }, value = "/dbs/{db}/{collection}/listindexes")
	public synchronized ResponseEntity<?> listIndexes(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName) {
		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		ListIndexesIterable<Document> result = collection.listIndexes();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
