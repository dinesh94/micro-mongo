package generic.mongo.microservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 
 * @author Dinesh B
 * 
 * http://localhost:7742/mongo/rest/v2/api-docs
 * http://localhost:7742/mongo/rest/swagger-ui.html
 *
 *
* @Api
* @ApiClass
* @ApiError
* @ApiErrors
* @ApiOperation
* @ApiParam
* @ApiParamImplicit
* @ApiParamsImplicit
* @ApiProperty
* @ApiResponse
* @ApiResponses
* @ApiModel
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.build()
				.apiInfo(apiInfo());
	}

	private ApiInfo apiInfo() {

		return new ApiInfoBuilder()
				.title("REST Services Powered by Dinesh")
				.description("This is the REST API Documentation, developed by dinesh")
				.termsOfServiceUrl("dineshbhavsar.com")
				.contact("Dinesh")
				.license("Apache License Version 2.0")
				.licenseUrl("dineshbhavsar.com/LICENSE")
				.version("2.0")
				.build();
	}

}
