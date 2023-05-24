package cloudgene.mapred.server.controller;

import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller
public class ImportController {

	@Post("/api/v2/importer/files")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)

	public String validateImport(String input) {

		throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
				"URL-based uploads are no longer supported. Please use direct file uploads instead.");

	}

}
