package cloudgene.mapred.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Util {

	public static void copyS3ToFile(String bucket, File file) throws IOException {

		String temp = bucket.replaceAll("s3://", "");

		String name = temp.split("/", 2)[0];
		String key = temp.split("/", 2)[1];

		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

		S3Object o = s3.getObject(name, key);
		S3ObjectInputStream s3is = o.getObjectContent();
		FileOutputStream fos = new FileOutputStream(file);
		byte[] read_buf = new byte[1024];
		int read_len = 0;
		while ((read_len = s3is.read(read_buf)) > 0) {
			fos.write(read_buf, 0, read_len);
		}
		s3is.close();
		fos.close();
	}

}
