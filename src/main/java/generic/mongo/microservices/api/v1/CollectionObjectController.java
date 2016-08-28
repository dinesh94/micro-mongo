/**
 * 
 */
package generic.mongo.microservices.api.v1;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;

import generic.mongo.microservices.util.CommonUtil;

/**
 * @author Dinesh
 *
 */
@RestController
@RequestMapping("api/v1/*")
public class CollectionObjectController {

	@Resource
	MongoClient mongoClient;

	@RequestMapping(method = RequestMethod.GET, value = "/dbs/{db}/{collection}/{id}")
	public synchronized ResponseEntity<?> getObject(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String idParam) {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		List<Document> documents = CommonUtil.findObjects(collection, idParam);

		return new ResponseEntity<>(documents, HttpStatus.FOUND);
	}
	
	/**
	 * POST WILL CREATE A NEW OBJECT IN THE DATABSE, WITH GENERATED ID
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 **/
	@RequestMapping(method = RequestMethod.POST, value = "/dbs/{db}/{collection}")
	public synchronized ResponseEntity<?> saveObject(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@RequestBody String jsonString) throws JsonParseException, JsonMappingException, IOException {
	
		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		Document doc = Document.parse(jsonString);
		Object objectId = doc.get("_id");
	
		if (objectId != null) {
			Document existingDocument = CommonUtil.findOneObject(collection, objectId.toString());
			if (existingDocument != null) {
				/* if object exist in database with given id --> Merge */
				doc = doPatch(collection, existingDocument, doc);
			} else {
				insertNew(collection, objectId.toString(), doc);
			}
		} else {
			/* Use has not provided id --> Create new entry */
			objectId = CommonUtil.getId();
			insertNew(collection, objectId.toString(), doc);
		}
	
		return new ResponseEntity<>(doc, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/dbs/{db}/{collection}/{id}")
	public synchronized ResponseEntity<?> deleteObject(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String idParam) {
		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		Long deleteCount = doDelete(collection, idParam);
		return new ResponseEntity<>(deleteCount, HttpStatus.FOUND);
	}
	
	/** PUT WILL REPLACE WHOLE OBJECT, WHICH IS ALREADY PRESENT IN THE DATABASE **/
	/**
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 **/
	@RequestMapping(method = { RequestMethod.PUT }, value = "/dbs/{db}/{collection}/{id}")
	public synchronized ResponseEntity<?> put(
			@PathVariable("db") String dbName,
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
	public synchronized ResponseEntity<?> patch(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String idParam,
			@RequestBody String jsonString) throws JsonParseException, JsonMappingException, IOException {

		MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
		Document existingDocument = CommonUtil.findOneObject(collection, idParam);
		Document doc = Document.parse(jsonString);
		if (existingDocument != null) {
			// Merge both the document, override existing properties & add newly added properties
			doc = doPatch(collection, existingDocument, doc);
		} else {
			insertNew(collection, idParam, doc);
		}

		return new ResponseEntity<>(doc, HttpStatus.OK);
	}


	private void insertNew(MongoCollection<Document> collection, String objectId, Document doc) {
		doc.append("_id", objectId);
		collection.insertOne(doc);
	}

	private Document doPatch(MongoCollection<Document> collection, Document existingDocument, Document docFromUI) throws JsonParseException, JsonMappingException, IOException {
		Document mergedDoc = merge(existingDocument, docFromUI);
		mergedDoc.put("_id", existingDocument.get("_id").toString());

		collection.findOneAndReplace(existingDocument, mergedDoc);
		return mergedDoc;
	}
	
	private long doDelete(MongoCollection<Document> collection, String idParam) {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", idParam);

		DeleteResult deleteResult = collection.deleteOne(query);
		return deleteResult.getDeletedCount();
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
}
