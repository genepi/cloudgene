package cloudgene.mapred.util;

import java.io.File;
import java.io.IOException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

public class S3Util {

	private static AmazonS3 s3;

	private static TransferManager tm;

	public static AmazonS3 getAmazonS3() {
		if (s3 == null) {
			s3 = AmazonS3ClientBuilder.defaultClient();
		}
		return s3;
	}

	public static TransferManager getTransferManager() {
		if (tm == null) {
			s3 = getAmazonS3();
			tm = TransferManagerBuilder.standard().withS3Client(s3).build();
		}
		return tm;
	}

	public static boolean isValidS3Url(String url) {
		if (url.startsWith("s3://")) {
			String temp = url.replaceAll("s3://", "");
			String[] tiles = temp.split("/", 2);
			if (tiles.length == 2) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	public static String getBucket(String url) {
		if (url.startsWith("s3://")) {
			String temp = url.replaceAll("s3://", "");
			String[] tiles = temp.split("/", 2);
			if (tiles.length == 2) {
				return tiles[0];
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static String getKey(String url) {
		if (url.startsWith("s3://")) {
			String temp = url.replaceAll("s3://", "");
			String[] tiles = temp.split("/", 2);
			if (tiles.length == 2) {
				return tiles[1];
			} else {
				return null;
			}
		} else {
			return null;
		}

	}

	public static void copyToFile(String url, File file) throws IOException {
		if (isValidS3Url(url)) {
			String bucket = getBucket(url);
			String key = getKey(url);
			copyToFile(bucket, key, file);
		} else {
			throw new IOException("Url '" + url + "' is not a valid S3 bucket.");
		}
	}

	public static void copyToFile(String bucket, String key, File file) throws IOException {

		TransferManager tm = getTransferManager();
		Download download = tm.download(bucket, key, file);
		try {
			download.waitForCompletion();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}

	}

	public static void copyToS3(File file, String url) throws IOException {
		if (isValidS3Url(url)) {
			String bucket = getBucket(url);
			String key = getKey(url);
			copyToS3(file, bucket, key);
		} else {
			throw new IOException("Url '" + url + "' is not a valid S3 bucket.");
		}
	}

	public static void copyToS3(String content, String url) throws IOException {
		if (isValidS3Url(url)) {
			String bucket = getBucket(url);
			String key = getKey(url);
			copyToS3(content, bucket, key);
		} else {
			throw new IOException("Url '" + url + "' is not a valid S3 bucket.");
		}
	}

	public static void copyToS3(File file, String bucket, String key) throws IOException {

		TransferManager tm = getTransferManager();
		Upload upload = tm.upload(bucket, key, file);
		try {
			upload.waitForCompletion();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}

	}

	public static void copyToS3(String content, String bucket, String key) throws IOException {

		AmazonS3 s3 = getAmazonS3();
		s3.putObject(bucket, key, content);

	}

	public static ObjectListing listObjects(String url) throws IOException {

		AmazonS3 s3 = getAmazonS3();

		if (isValidS3Url(url)) {

			String bucket = getBucket(url);
			String key = getKey(url);

			ObjectListing objects = s3.listObjects(bucket, key);

			return objects;
		} else {
			throw new IOException("Url '" + url + "' is not a valid S3 bucket.");
		}

	}
	
	public static void deleteFolder(String url) {

		String bucket = S3Util.getBucket(url);
		String key = S3Util.getKey(url);

		AmazonS3 s3 = S3Util.getAmazonS3();

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(key);

		ObjectListing objectListing = s3.listObjects(listObjectsRequest);

		while (true) {
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				s3.deleteObject(bucket, objectSummary.getKey());
			}
			if (objectListing.isTruncated()) {
				objectListing = s3.listNextBatchOfObjects(objectListing);
			} else {
				break;
			}
		}

	}

}