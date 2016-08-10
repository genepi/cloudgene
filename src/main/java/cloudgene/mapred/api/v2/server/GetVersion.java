package cloudgene.mapred.api.v2.server;

import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.Main;
import cloudgene.mapred.util.BaseResource;

public class GetVersion extends BaseResource {

	public static final String IMAGE_DATA = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"96\" height=\"20\">"
			+ "	<linearGradient id=\"b\" x2=\"0\" y2=\"100%\"><stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/><stop offset=\"1\" stop-opacity=\".1\"/></linearGradient>"
			+ "	<mask id=\"a\"><rect width=\"96\" height=\"20\" rx=\"3\" fill=\"#fff\"/></mask>"
			+ "	<g mask=\"url(#a)\"><path fill=\"#555\" d=\"M0 0h55v20H0z\"/><path fill=\"#97CA00\" d=\"M55 0h41v20H55z\"/><path fill=\"url(#b)\" d=\"M0 0h96v20H0z\"/></g>"
			+ "	<g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\">"
			+ "		<text x=\"27.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">version</text>"
			+ "		<text x=\"27.5\" y=\"14\">version</text>"
			+ "		<text x=\"74.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">"
			+ Main.VERSION
			+ "</text>"
			+ "		<text x=\"74.5\" y=\"14\">"
			+ Main.VERSION + "</text>" + "	</g>" + "</svg>";

	@Get
	public Representation getVersion() {
		getResponse().getCacheDirectives().add(CacheDirective.noCache());
		return new StringRepresentation(IMAGE_DATA, MediaType.IMAGE_SVG);

	}
}
