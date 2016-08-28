/**
 * 
 */
package generic.mongo.microservices.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

/**
 * @author Dinesh
 *
 */
@RestController
public class SearchController {

	@Resource
	MongoClient mongoClient;

	@RequestMapping("/dbs/{db}/{collection}")
	public synchronized ResponseEntity<?> search(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			HttpServletRequest request) {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		FindIterable<Document> iterable;

		Map<String, String[]> parameters = request.getParameterMap();
		BasicDBObject query = new BasicDBObject();

		if (!parameters.isEmpty()) {
			for (Entry<String, String[]> param : parameters.entrySet()) {
				BasicDBList docIds = new BasicDBList();
				String[] values = param.getValue();
				for (String value : values) {
					//docIds.add(Integer.parseInt(value));
					Pattern regex = Pattern.compile(value);
					docIds.add(regex);
				}

				DBObject inClause = new BasicDBObject("$in", docIds);
				query.put(param.getKey(), inClause);
			}
		}

		if (query.isEmpty())
			iterable = collection.find();
		else
			iterable = collection.find(query);

		final List<Document> result = new ArrayList<>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				result.add(document);
			}
		});

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping("/dbs/{db}/{collection}/{keyword}/search")
	public synchronized ResponseEntity<?> search(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("keyword") String keyword,
			@RequestParam("searchFields") List<String> searchFields) {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		FindIterable<Document> iterable;

		BasicDBList or = new BasicDBList();
		BasicDBObject query = new BasicDBObject();

		if (!searchFields.isEmpty()) {
			for (String searchField : searchFields) {
				BasicDBObject searchFor = new BasicDBObject();
				//docIds.add(Integer.parseInt(value));
				Pattern regex = Pattern.compile(keyword);
				searchFor.put(searchField, regex);

				//DBObject inClause = new BasicDBObject("$in", searchFor);
				or.add(searchFor);
				//or.add(inClause);
			}
		}
		query.put("$or", or);
		
		final List<Document> result = new ArrayList<>();
		if (!query.isEmpty()) {
			iterable = collection.find(query);
			iterable.forEach(new Block<Document>() {
				@Override
				public void apply(final Document document) {
					result.add(document);
				}
			});

		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
