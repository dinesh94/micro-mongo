/**
 * 
 */
package generic.mongo.microservices.api.v1;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import generic.mongo.microservices.util.CommonUtil;

/**
 * @author Dinesh
 *
 */
@RestController
@RequestMapping("api/v1/*")
public class FileController {

	private static final String ROOT = null;

	@Resource
	private ResourceLoader resourceLoader;

	@Resource
	MongoClient mongoClient;

	/**
	 * @throws IOException
	 * 
	 **/
	@RequestMapping(method = { RequestMethod.GET }, value = "/dbs/{db}/{collection}/files/{id}")
	//headers = "Accept=image/jpeg, image/jpg, image/png, image/gif", 
	public synchronized HttpEntity<byte[]> getFile(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@PathVariable("id") String objectId) throws IOException {

		GridFS gfsPhoto = new GridFS(mongoClient.getDB(dbName), collectionName);

		BasicDBObject query = new BasicDBObject();
		query.put("_id", objectId);
		GridFSDBFile imageForOutput = gfsPhoto.findOne(query);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		imageForOutput.writeTo(bos);

		HttpHeaders header = new HttpHeaders();
		header.set("Content-Disposition", "attachment; filename=" + imageForOutput.getFilename());
		header.setContentLength(bos.size());

		return new HttpEntity<byte[]>(bos.toByteArray(), header);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/dbs/{db}/{collection}/savefile")
	public ResponseEntity<?> handleFileUpload(
			@PathVariable("db") String dbName,
			@PathVariable("collection") String collectionName,
			@RequestParam("file") MultipartFile fileData) {

		String fileID = null;
		if (!fileData.isEmpty()) {
			try {
				GridFS gfsPhoto = new GridFS(mongoClient.getDB(dbName), collectionName);
				GridFSInputFile gfsFile = gfsPhoto.createFile(fileData.getInputStream());

				fileID = CommonUtil.getId();

				gfsFile.setFilename(fileData.getOriginalFilename());
				gfsFile.setId(fileID);

				gfsFile.save();

			} catch (IOException | RuntimeException e) {
			}
		}
		return new ResponseEntity<>(fileID, HttpStatus.OK);
	}

}
