/**
 * 
 */
package generic.mongo.microservices.api.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import generic.mongo.microservices.model.Condition;
import generic.mongo.microservices.model.Query;

/**
 * @author Dinesh
 *
 */
@RestController
@RequestMapping("api/v1/*")
public class SearchController {
	@Resource
	MongoClient mongoClient;

	@RequestMapping(method = { RequestMethod.POST }, value = "/dbs/{db}/{collection}/search")
	public synchronized ResponseEntity<?> search(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@RequestParam(required = false, value = "likesearch") Boolean likesearch,
			@RequestBody Query query) {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);

		final List<Document> result = new ArrayList<>();
		FindIterable<Document> iterable;

		BasicDBObject joinQuery = new BasicDBObject();
		List<BasicDBObject> orConditions = new ArrayList<BasicDBObject>();
		List<BasicDBObject> andConditions = new ArrayList<BasicDBObject>();

		List<Condition> conditions = query.getCondition();
		for (Condition condition : conditions) {
			BasicDBObject aCondition = new BasicDBObject();
			BasicDBList valuesToSearch = new BasicDBList();
			for (String value : condition.getValues()) {
				//docIds.add(Integer.parseInt(value));
				if (likesearch != null && likesearch) {
					Pattern regex = Pattern.compile(value);
					valuesToSearch.add(regex);
				} else {
					valuesToSearch.add(value);
				}
			}

			DBObject inClause = new BasicDBObject("$in", valuesToSearch);
			aCondition.put(condition.getSearchpath(), inClause);

			if (condition.getIsOr())
				orConditions.add(aCondition);
			else
				andConditions.add(aCondition);
		}

		if (!orConditions.isEmpty())
			joinQuery.put("$or", orConditions);
		if (!andConditions.isEmpty())
			joinQuery.put("$and", andConditions);

		System.out.println("Query = " + joinQuery);

		if (joinQuery.isEmpty())
			iterable = collection.find().sort(new BasicDBObject(query.getSortOn(), query.isSortAscending() ? 1 : -1));
		else
			iterable = collection.find(joinQuery);

		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				result.add(document);
			}
		});

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/dbs/{db}/{collection}/{keyword}/search")
	public synchronized ResponseEntity<?> search(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("keyword") String keyword,
			@RequestParam(required = false, value = "likesearch") Boolean likesearch,
			@RequestParam(required = false, value = "searchFields") List<String> searchPaths) {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		FindIterable<Document> iterable;

		BasicDBList orCondition = new BasicDBList();
		BasicDBObject query = new BasicDBObject();

		Pattern regexKeyword = Pattern.compile(keyword);
		
		if (searchPaths != null && !searchPaths.isEmpty()) {
			for (String searchField : searchPaths) {
				BasicDBObject searchFor = new BasicDBObject();
				//docIds.add(Integer.parseInt(value));
				if (likesearch != null && likesearch) {
					searchFor.put(searchField, regexKeyword);
				} else {
					searchFor.put(searchField, keyword);
				}
				//DBObject inClause = new BasicDBObject("$in", searchFor);
				orCondition.add(searchFor);
				//or.add(inClause);
			}
		} 
		
		if(searchPaths != null && !searchPaths.isEmpty()){
			query.put("$or", orCondition);
		}
		else{
			BasicDBObject search;
			if (likesearch != null && likesearch) {
				search = new BasicDBObject("$text", new BasicDBObject("$search", regexKeyword));
			}else{
				search = new BasicDBObject("$text", new BasicDBObject("$search", keyword));
			}
			query = search;
		}

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
