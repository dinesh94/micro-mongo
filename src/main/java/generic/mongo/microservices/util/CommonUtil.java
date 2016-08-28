package generic.mongo.microservices.util;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class CommonUtil {

	public static String getId() {
		return ObjectId.get().toHexString();
	}

	public static Document findOneObject(MongoCollection<Document> collection, String objectId) {
		List<Document> result = findObjects(collection, objectId);
		if(result != null && !result.isEmpty())
			return result.get(0);
		return null;
	}
	
	public static List<Document> findObjects(MongoCollection<Document> collection, String objectId) {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", objectId);
		FindIterable<Document> iterable = collection.find(query);

		final List<Document> result = new ArrayList<>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				result.add(document);
			}
		});
		return result;
	}
}
