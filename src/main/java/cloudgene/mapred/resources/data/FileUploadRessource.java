package cloudgene.mapred.resources.data;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.TaskJob;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.representations.JSONAnswer;
import cloudgene.mapred.tasks.AbstractTask;
import cloudgene.mapred.tasks.ImporterLocalFile;
import cloudgene.mapred.util.BaseResource;

/**
 * Resource which has only one representation.
 * 
 */
public class FileUploadRessource extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		try {

			User user = getUser(getRequest());

			File file = null;
			String path = "";
			if (entity != null && user != null) {

				if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(),
						true)) {
					List<FileItem> items = parseRequest();

					// FileItems are all members of the form
					for (final Iterator<FileItem> it = items.iterator(); it
							.hasNext();) {
						FileItem fileItem = it.next();

						if (fileItem.getFieldName().equals("path")) {
							path = fileItem.getString();
						}
					}

					// FileItems are all members of the form
					for (final Iterator<FileItem> it = items.iterator(); it
							.hasNext();) {
						FileItem fileItem = it.next();

						// uploaded file
						if (fileItem.getFieldName().equals("sampleFile")) {

							// Check for file size bigger than 200mb
							if (entity.getSize() > 1024 * 1024 * 500) {
								try {
									// Read and discard all data
									entity.exhaust();
									return new JSONAnswer(
											"Maximum file size of 200 mb exceeded!",
											false);
								} catch (IOException e) {
									e.printStackTrace();
								}

							}
							try {

								String tmpFile = getSettings().getTempFilename(
										fileItem.getName());
								file = new File(tmpFile);
								fileItem.write(file);

								AbstractTask task = new ImporterLocalFile(
										file.getAbsolutePath(), path);

								// Submit Job
								AbstractJob job = new TaskJob(task);
								job.setName(job.getId());
								job.setUser(user);

								WorkflowEngine engine = getWorkflowEngine();
								engine.submit(job);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					}

					return new JSONAnswer(
							"Files imported. Please submit a job now!", true);
				}
			} else {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new JSONAnswer("No user", false);
			}
		} catch (ArrayIndexOutOfBoundsException e) {

			return new JSONAnswer("Check if subdirectory is not empty", false);
		}

		return new JSONAnswer("Error.", false);

	}

	private List<FileItem> parseRequest() {
		List<FileItem> items = null;
		// 1/ Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1000240);

		// 2/ Create a new file upload handler based on the Restlet
		// FileUpload extension that will parse Restlet requests and
		// generates FileItems.
		RestletFileUpload upload = new RestletFileUpload(factory);

		try {
			items = upload.parseRequest(getRequest());
		} catch (FileUploadException e2) {
			e2.printStackTrace();
		}
		return items;
	}

}
