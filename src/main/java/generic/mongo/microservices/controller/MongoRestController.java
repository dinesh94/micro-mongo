/**
 * 
 */
package generic.mongo.microservices.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

/**
 * @author Dinesh
 *
 */
@RestController
public class MongoRestController {

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
		
		List<Document> result = new ArrayList<>();
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

	@RequestMapping("/dbs/{db}/{collection}")
	public synchronized ResponseEntity<?> collection(@PathVariable("db") String dbName,
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

		List<Document> result = new ArrayList<>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				result.add(document);
			}
		});

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

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

	@RequestMapping(method = RequestMethod.GET, value = "/dbs/{db}/{collection}/{id}")
	public synchronized ResponseEntity<?> object(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String idParam) {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		List<Document> documents = findObjects(collection, idParam);

		return new ResponseEntity<>(documents, HttpStatus.FOUND);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/dbs/{db}/{collection}/{id}")
	public synchronized ResponseEntity<?> deleteObject(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String idParam) {
		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		Long deleteCount = doDelete(collection, idParam);
		return new ResponseEntity<>(deleteCount, HttpStatus.FOUND);
	}

	/**
	 * POST WILL CREATE A NEW OBJECT IN THE DATABSE, WITH GENERATED ID
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 **/
	@RequestMapping(method = RequestMethod.POST, value = "/dbs/{db}/{collection}")
	public synchronized ResponseEntity<?> post(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@RequestBody String jsonString) throws JsonParseException, JsonMappingException, IOException {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		Document doc = Document.parse(jsonString);
		Object objectId = doc.get("_id");

		if (objectId != null) {
			Document existingDocument = findOneObject(collection, objectId.toString());
			if (existingDocument != null) {
				/* if object exist in database with given id --> Merge */
				doc = doPatch(collection, existingDocument, doc);
			} else {
				insertNew(collection, objectId.toString(), doc);
			}
		} else {
			/* Use has not provided id --> Create new entry */
			objectId = ObjectId.get().toHexString();
			insertNew(collection, objectId.toString(), doc);
		}

		return new ResponseEntity<>(doc, HttpStatus.OK);
	}

	/** PUT WILL REPLACE WHOLE OBJECT, WHICH IS ALREADY PRESENT IN THE DATABASE **/
	/**
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 **/
	@RequestMapping(method = { RequestMethod.PUT }, value = "/dbs/{db}/{collection}/{id}")
	public synchronized ResponseEntity<?> put(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String idParam,
			@RequestBody String jsonString) throws JsonParseException, JsonMappingException, IOException {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		doDelete(collection, idParam);

		Document doc = Document.parse(jsonString);
		doc.put("_id", idParam);

		insertNew(collection, idParam, doc);

		return new ResponseEntity<>(doc, HttpStatus.OK);
	}

	/**
	 * PATHC WILL MERGE EXISTING OBJECT, IF OBJECT DOES NOT EXIST IT WILL PERFORM PUT
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 **/
	@RequestMapping(method = { RequestMethod.PATCH }, value = "/dbs/{db}/{collection}/{id}")
	public synchronized ResponseEntity<?> patch(@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String idParam,
			@RequestBody String jsonString) throws JsonParseException, JsonMappingException, IOException {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		Document existingDocument = findOneObject(collection, idParam);
		Document doc = Document.parse(jsonString);
		if (existingDocument != null) {
			// Merge both the document, override existing properties & add newly added properties
			doc = doPatch(collection, existingDocument, doc);
		} else {
			insertNew(collection, idParam, doc);
		}

		return new ResponseEntity<>(doc, HttpStatus.OK);
	}

	private Document doPatch(MongoCollection<Document> collection, Document existingDocument, Document docFromUI) throws JsonParseException, JsonMappingException, IOException {
		Document mergedDoc = merge(existingDocument, docFromUI);
		mergedDoc.put("_id", existingDocument.get("_id").toString());

		collection.findOneAndReplace(existingDocument, mergedDoc);
		return mergedDoc;
	}

	@SuppressWarnings("unchecked")
	private Document merge(Document existingDocument, Document docFromUI) throws JsonParseException, JsonMappingException, IOException {
		String mergedJson = "";
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map1 = mapper.readValue(existingDocument.toJson(), Map.class);
		Map<String, Object> map2 = mapper.readValue(docFromUI.toJson(), Map.class);
		Map<String, Object> merged = new HashMap<String, Object>(map1);
		merged.putAll(map2);
		mergedJson = mapper.writeValueAsString(merged);

		Document mergedDoc = Document.parse(mergedJson);
		return mergedDoc;
	}

	private void insertNew(MongoCollection<Document> collection, String objectId, Document doc) {
		doc.append("_id", objectId);
		collection.insertOne(doc);
	}

	private long doDelete(MongoCollection<Document> collection, String idParam) {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", idParam);

		DeleteResult deleteResult = collection.deleteOne(query);
		return deleteResult.getDeletedCount();
	}

	private Document findOneObject(MongoCollection<Document> collection, String objectId) {
		List<Document> result = findObjects(collection, objectId);
		return result.get(0);
	}

	private List<Document> findObjects(MongoCollection<Document> collection, String objectId) {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", objectId);
		FindIterable<Document> iterable = collection.find(query);

		List<Document> result = new ArrayList<>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				result.add(document);
			}
		});
		return result;
	}
}