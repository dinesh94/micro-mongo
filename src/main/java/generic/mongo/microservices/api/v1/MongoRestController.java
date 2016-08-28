/**
 * 
 */
package generic.mongo.microservices.api.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Dinesh
 *
 */
@RestController
@RequestMapping("api/v1/*")
public class MongoRestController {

	/**
	 *  GET /dbs
	 *  GET /dbs/{db}
	 *  GET /dbs/{db}/{collection}
	 *  POST /dbs/{db}/{collection}
	 *  GET /dbs/{db}/{collection}/{id}
	 *  PUT /dbs/{db}/{collection}/{id}
	 *  PATHC /dbs/{db}/{collection}/{id}
	 *  DELETE /dbs/{db}/{collection}/{id}
	 *  
	 *  GET /dbs/{db}/{collection}/{keyword}/search
	 *  
	 *  PUT /dbs/{db}/{collection}/create
	 *  POST /dbs/{db}/{collection}/create
	 *  DELTE /dbs/{db}/{collection}/remove
	 *  
	 */
}