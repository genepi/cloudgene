package cloudgene.mapred.server.controller;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import genepi.hadoop.importer.FileItem;
import genepi.hadoop.importer.IImporter;
import genepi.hadoop.importer.ImporterFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import net.sf.json.JSONArray;

@Controller
public class ImportController {


	@Post("/api/v2/importer/files")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)

	public JSONArray validateImport(String input) {

		List<FileItem> results = new Vector<FileItem>();

		try {

			if (ImporterFactory.needsImport(input)) {

				for (String url : ImporterFactory.parseImportString(input)) {

					try {

						IImporter importer = ImporterFactory.createImporter(url, null);

						if (importer != null) {

							List<FileItem> items = importer.getFiles();

							if (items != null) {

								// add files to list
								results.addAll(items);

							} else {
								

							}

						} else {
							throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Protocol not supported");

						}

					} catch (Exception e) {
						throw new JsonHttpStatusException(HttpStatus.NOT_FOUND,e.toString());

					}

				}

			}

			JSONArray jsonArray = JSONArray.fromObject(results);
			System.out.println(jsonArray);
			return jsonArray;

		} catch (Exception e) {
			throw new JsonHttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.toString());
		}

	}

}
