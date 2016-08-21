/**
 * 
 */
package generic.mongo.microservices.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * @author Dinesh
 *
 */
@RestController
public class DBController {

	@Resource
	MongoClient mongoClient;

	@RequestMapping("/dbs")
	public synchronized ResponseEntity<?> dbs() {
		List<String> dbs = new ArrayList<>();
		MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
		while (dbsCursor.hasNext()) {
			dbs.add(dbsCursor.next());
		}

		return new ResponseEntity<>(dbs, HttpStatus.OK);
	}

	@RequestMapping("/dbs/{db}")
	public synchronized ResponseEntity<?> db(@PathVariable("db") String dbName) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
		List<String> collections = new ArrayList<>();
		for (String name : mongoDatabase.listCollectionNames()) {
			collections.add(name);
		}
		return new ResponseEntity<>(collections, HttpStatus.OK);
	}
}
