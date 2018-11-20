package cloudgene.mapred.api.v2.data;

import genepi.hadoop.importer.FileItem;
import genepi.hadoop.importer.IImporter;
import genepi.hadoop.importer.ImporterFactory;

import java.util.List;
import java.util.Vector;

import net.sf.json.JSONArray;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class ImporterFileList extends ServerResource {

	@Post
	public Representation validateImport(Representation entity) {

		Form form = new Form(entity);

		String input = form.getFirstValue("input").trim();

		List<FileItem> results = new Vector<FileItem>();

		try {

			if (ImporterFactory.needsImport(input)) {

				for (String url : ImporterFactory.parseImportString(input)) {

					try {

						IImporter importer = ImporterFactory.createImporter(
								url, null);

						if (importer != null) {

							List<FileItem> items = importer.getFiles();

							if (items != null) {

								// add files to list
								results.addAll(items);

							} else {
								setStatus(Status.CLIENT_ERROR_NOT_FOUND);
								return new StringRepresentation(
										importer.getErrorMessage());

							}

						} else {

							setStatus(Status.CLIENT_ERROR_NOT_FOUND);
							return new StringRepresentation(
									"Protocol not supported");

						}

					} catch (Exception e) {
						setStatus(Status.CLIENT_ERROR_NOT_FOUND);
						return new StringRepresentation(e.toString());

					}

				}

			}

			JSONArray jsonArray = JSONArray.fromObject(results);

			return new StringRepresentation(jsonArray.toString());

		} catch (Exception e) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation(e.toString());
		}

	}

}
