/**
 * 
 */
package generic.mongo.microservices.controller;

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

/**
 * @author Dinesh
 *
 */
@RestController
public class CollectionController {

	@Resource
	MongoClient mongoClient;

	@RequestMapping("/dbs/{db}/{collection}/view")
	public synchronized ResponseEntity<?> view(@PathVariable("db") String dbName,
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

	/** OPERATES ON COLLECTION **/
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, value = "/dbs/{db}/{collection}/create")
	public synchronized ResponseEntity<?> createCollection(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName) {
		mongoClient.getDatabase(dbName).createCollection(collectionName);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.DELETE }, value = "/dbs/{db}/{collection}/remove")
	public synchronized ResponseEntity<?> removeCollection(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName) {
		mongoClient.getDatabase(dbName).createCollection(collectionName);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
