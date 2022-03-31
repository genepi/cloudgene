package cloudgene.mapred.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.CompletedPart;
import io.micronaut.http.server.multipart.MultipartBody;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class FormUtil {

	@Inject
	protected cloudgene.mapred.server.Application application;

	public Publisher<Object> processMultipartBody(MultipartBody body, Function<List<Parameter>, Object> callback) {

		return Mono.<Object>create(emitter -> {

			body.subscribe(new Subscriber<CompletedPart>() {

				List<Parameter> form = new Vector<Parameter>();

				private Subscription s;

				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(1);
				}

				@Override
				public void onNext(CompletedPart completedPart) {
					Parameter formParameter = proessCompletedPart(completedPart);
					if (formParameter != null) {
						form.add(formParameter);
					}
					s.request(1);
				}

				@Override
				public void onError(Throwable t) {
					emitter.error(t);
				}

				@Override
				public void onComplete() {
					Object result = callback.apply(form);
					emitter.success(result);
				}
			});
		});

	}

	public Parameter proessCompletedPart(CompletedPart completedPart) {

		String partName = completedPart.getName();

		if (completedPart instanceof CompletedFileUpload) {

			String originalFileName = ((CompletedFileUpload) completedPart).getFilename();
			String tmpFile = application.getSettings().getTempFilename(originalFileName);
			File file = new File(tmpFile);

			try {
				InputStream stream = completedPart.getInputStream();
				FileUtils.copyInputStreamToFile(stream, file);
				stream.close();
				return new Parameter(partName, file);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {

			try {
				String value = Streams.asString(completedPart.getInputStream());
				return new Parameter(partName, value);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;

	}

	public static class Parameter {

		public String name;

		public Object value;

		public Parameter(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

}
