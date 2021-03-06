/**
 * 
 */
package generic.mongo.microservices.api.v1;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * @author Dinesh
 *
 */
@RestController
@RequestMapping("api/v1/*")
public class CollectionController {

	@Resource
	MongoClient mongoClient;

	/** OPERATES ON COLLECTION **/
	@RequestMapping(method = { RequestMethod.POST }, value = "/dbs/{db}/{collection}/create")
	public ResponseEntity<?> createCollection(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
		mongoDatabase.createCollection(collectionName);
		MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
		collection.createIndex(new Document("$**", "text"));// This is required for full test search

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST }, value = "/dbs/{db}/{collection}/update")
	public ResponseEntity<?> updateCollection(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName){
		//mongoClient.getDatabase(dbName).runCommand(collectionName);
		return new ResponseEntity<>("API NOT IMPLEMENTED", HttpStatus.NOT_IMPLEMENTED);
	}

	@RequestMapping(method = { RequestMethod.DELETE }, value = "/dbs/{db}/{collection}/delete")
	public ResponseEntity<?> removeCollection(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName){
		mongoClient.getDatabase(dbName).createCollection(collectionName);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/dbs/{db}/{collection}/view")
	public ResponseEntity<?> view(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName) {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		FindIterable<Document> iterable;

		iterable = collection.find();

		final List<Document> result = new ArrayList<>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				result.add(document);
			}
		});

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}
