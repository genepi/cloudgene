package cloudgene.mapred.jobs.workspace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import cloudgene.mapred.jobs.AbstractJob;
import genepi.hadoop.S3Util;

public class S3Workspace implements IExternalWorkspace {

	public static long EXPIRATION_MS = 1000 * 60 * 60;
	
	private static final Log log = LogFactory.getLog(S3Workspace.class);
	
	private String location;

	private AbstractJob job;
	
	public S3Workspace(String location) {
		this.location = location;
	}

	@Override
	public String getName() {
		return "Amazon S3";
	}

	@Override
	public void setup(AbstractJob job) throws IOException {

		this.job = job;

		if (location == null) {
			throw new IOException("No S3 Output Bucket specified.");
		}

		if (!S3Util.isValidS3Url(location)) {
			throw new IOException("Output Url '" + location + "' is not a valid S3 bucket.");
		}

		try {
			S3Util.copyToS3(job.getApplication(), location + "/" + job.getId() + "/version.txt");
		} catch (Exception e) {
			throw new IOException("Output Url '" + location + "' is not writable.");
		}

	}

	@Override
	public String upload(String id, File file) throws IOException {
		String target = location + "/" + job.getId() + "/" + id + "/" + file.getName();
		S3Util.copyToS3(file, target);
		return target;
	}

	@Override
	public InputStream download(String url) throws IOException {

		String bucket = S3Util.getBucket(url);
		String key = S3Util.getKey(url);

		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

		S3Object o = s3.getObject(bucket, key);
		S3ObjectInputStream s3is = o.getObjectContent();

		return s3is;
	}

	@Override
	public void delete(AbstractJob job) throws IOException {
		
	}
	
	@Override
	public String createPublicLink(String url) {
		
		String bucket = S3Util.getBucket(url);
		String key = S3Util.getKey(url);
		
		AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

		java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += EXPIRATION_MS;
        expiration.setTime(expTimeMillis);

        // Generate the presigned URL.
        log.debug("Generating pre-signed URL for "  + url + "...");
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        URL publicUrl = s3.generatePresignedUrl(generatePresignedUrlRequest);
        log.debug("Pre-signed URL for "  + url + " generated. Link: " + publicUrl.toString());
				return publicUrl.toString();
	}

}
