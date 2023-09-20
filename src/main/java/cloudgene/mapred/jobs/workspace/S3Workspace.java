package cloudgene.mapred.jobs.workspace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.S3Util;
import genepi.io.FileUtil;

public class S3Workspace implements IWorkspace {

	private static final String OUTPUT_DIRECTORY = "outputs";

	private static final String INPUT_DIRECTORY = "input";

	private static final String LOGS_DIRECTORY = "logs";

	private static final String TEMP_DIRECTORY = "temp";

	public static long EXPIRATION_MS = 1000 * 60 * 60;

	private static final Logger log = LoggerFactory.getLogger(S3Workspace.class);

	private String location;

	private String job;

	public S3Workspace(String location) {
		this.location = location;
	}

	@Override
	public String getName() {
		return "Amazon S3";
	}

	@Override
	public void setJob(String job) {
	this.job = job;
	}
	
	@Override
	public void setup() throws IOException {

		if (job == null) {
			throw new IOException("No job id provided.");
		}

		if (location == null) {
			throw new IOException("No S3 Output Bucket specified.");
		}

		if (!S3Util.isValidS3Url(location)) {
			throw new IOException("Output Url '" + location + "' is not a valid S3 bucket.");
		}

		try {
			S3Util.copyToS3(job, location + "/" + job + "/version.txt");
		} catch (Exception e) {
			log.error("Copy file to '" + location + "/" + job + "/version.txt' failed.", e);
			throw new IOException("Output Url '" + location + "' is not writable.", e);
		}

	}

	@Override
	public String upload(String id, File file) throws IOException {
		String target = location + "/" + job + "/" + id + "/" + file.getName();
		log.info("Copy file " + file.getAbsolutePath() + " to " + target);
		S3Util.copyToS3(file, target);
		return target;
	}

	@Override
	public String uploadInput(String id, File file) throws IOException {
		return upload(FileUtil.path(INPUT_DIRECTORY, id), file);
	}

	@Override
	public String uploadLog(File file) throws IOException {
		return upload(LOGS_DIRECTORY, file);
	}

	@Override
	public InputStream download(String url) throws IOException {

		String bucket = S3Util.getBucket(url);
		String key = S3Util.getKey(url);

		AmazonS3 s3 = S3Util.getAmazonS3();

		S3Object o = s3.getObject(bucket, key);
		S3ObjectInputStream s3is = o.getObjectContent();

		return s3is;
	}
	
	@Override
	public String downloadLog(String name) throws IOException {
		return FileUtil.readFileAsString(download(FileUtil.path(LOGS_DIRECTORY, name)));
	}

	public boolean exists(String url) {
		String bucket = S3Util.getBucket(url);
		String key = S3Util.getKey(url);
		AmazonS3 s3 = S3Util.getAmazonS3();
		return s3.doesObjectExist(bucket, key);
	}

	@Override
	public void delete(String job) throws IOException {

		if (!S3Util.isValidS3Url(location)) {
			throw new IOException("Output Url '" + location + "' is not a valid S3 bucket.");
		}

		String url = location + "/" + job;

		try {

			log.info("Deleting " + job + " on S3 workspace: '" + url + "'...");

			S3Util.deleteFolder(url);

			log.info("Deleted all files on S3 for job " + job + ".");

		} catch (Exception e) {
			throw new IOException("Folder '" + url + "' could not be deleted.", e);
		}

	}

	@Override
	public void cleanup(String job) throws IOException {
		if (!S3Util.isValidS3Url(location)) {
			throw new IOException("Output Url '" + location + "' is not a valid S3 bucket.");
		}

		String temp = location + "/" + job + "/" + TEMP_DIRECTORY;
		try {
			log.info("Deleting temp directory for " + job + " on S3 workspace: '" + temp + "'...");
			S3Util.deleteFolder(temp);
			log.info("Deleted all files on S3 for job " + job + ".");
		} catch (Exception e) {
			throw new IOException("Folder '" + temp + "' could not be deleted.", e);
		}

		String input = location + "/" + job + "/" + INPUT_DIRECTORY;
		try {
			log.info("Deleting input directory for " + input + " on S3 workspace: '" + input + "'...");
			S3Util.deleteFolder(input);
			log.info("Deleted all files on S3 for job " + job + ".");
		} catch (Exception e) {
			throw new IOException("Folder '" + input + "' could not be deleted.", e);
		}

	}

	@Override
	public String createPublicLink(String url) {

		String bucket = S3Util.getBucket(url);
		String key = S3Util.getKey(url);

		AmazonS3 s3 = S3Util.getAmazonS3();

		java.util.Date expiration = new java.util.Date();
		long expTimeMillis = expiration.getTime();
		expTimeMillis += EXPIRATION_MS;
		expiration.setTime(expTimeMillis);

		// Generate the presigned URL.
		log.debug("Generating pre-signed URL for " + url + "...");
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, key)
				.withMethod(HttpMethod.GET).withExpiration(expiration);
		URL publicUrl = s3.generatePresignedUrl(generatePresignedUrlRequest);
		log.debug("Pre-signed URL for " + url + " generated. Link: " + publicUrl.toString());
		return publicUrl.toString();
	}

	@Override
	public String getParent(String url) {
		if (url.startsWith("s3://")) {
			int index = url.lastIndexOf('/');
			if (index > 0) {
				return url.substring(0, index);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String createFolder(String id) {
		return location + "/" + job + "/" + OUTPUT_DIRECTORY + "/" + id;
	}

	@Override
	public String createFile(String folder, String id) {
		return location + "/" + job + "/" + OUTPUT_DIRECTORY + "/" + folder + "/" + id;
	}

	@Override
	public String createLogFile(String id) {
		return location + "/" + job + "/" + LOGS_DIRECTORY + "/" + id;
	}

	@Override
	public String createTempFolder(String id) {
		return location + "/" + job + "/" + TEMP_DIRECTORY + "/" + id;
	}

	@Override
	public List<Download> getDownloads(String url) {
		List<Download> downloads = new Vector<Download>();
		ObjectListing listing;
		try {
			listing = S3Util.listObjects(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return downloads;
		}

		String baseKey = S3Util.getKey(url);

		for (S3ObjectSummary summary : listing.getObjectSummaries()) {

			if (summary.getKey().endsWith("/")) {
				continue;
			}

			String filename = summary.getKey().replaceAll(baseKey + "/", "");
			String size = FileUtils.byteCountToDisplaySize(summary.getSize());
			String hash = HashUtil.getSha256(filename + size + (Math.random() * 100000));
			Download download = new Download();
			download.setName(filename);
			download.setPath("s3://" + summary.getBucketName() + "/" + summary.getKey());
			download.setSize(size);
			download.setHash(hash);
			downloads.add(download);

		}

		return downloads;
	}

	@Override
	public List<Download> getLogs() {
		String url = location + "/" + job + "/" + LOGS_DIRECTORY;
		return getDownloads(url);
	}

}
