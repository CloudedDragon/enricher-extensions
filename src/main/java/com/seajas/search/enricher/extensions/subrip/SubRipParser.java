package com.seajas.search.enricher.extensions.subrip;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Simple SubRip parser which removes the index and time indicators and returns the actual subtitle text.
 * 
 * @author Jasper van Veghel <jasper@seajas.com>
 */
public class SubRipParser implements Parser {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private final static Logger logger = LoggerFactory.getLogger(SubRipParser.class);

	/**
	 * The index pattern.
	 */
	private final Pattern indexPattern = Pattern.compile("^\\d*$");

	/**
	 * The timestamp pattern.
	 */
	private final Pattern timestampPattern = Pattern.compile("^\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}$");

	/**
	 * Return the supported types.
	 * 
	 * @param context
	 * @return Set<MediaType>
	 */
	@Override
	public Set<MediaType> getSupportedTypes(final ParseContext context) {
		Set<MediaType> supportedTypes = new HashSet<MediaType>();

		supportedTypes.add(MediaType.text("srt"));

		return Collections.unmodifiableSet(supportedTypes);
	}

	/**
	 * Parse the given input stream.
	 * 
	 * @param inputStream
	 * @param handler
	 * @param metadata
	 * @param context
	 */
	@Override
	public void parse(final InputStream stream, final ContentHandler handler, final Metadata metadata, final ParseContext context) throws IOException, SAXException, TikaException {
		XHTMLContentHandler result = new XHTMLContentHandler(handler, metadata);

		String encoding = null;

		// Check the Content-Type for the encoding first

		if (metadata.get(Metadata.CONTENT_TYPE) != null) {
			MediaType mediaType = MediaType.parse(metadata.get(Metadata.CONTENT_TYPE));

			if (mediaType.getParameters().containsKey("charset"))
				encoding = mediaType.getParameters().get("charset");
		}

		// Then check the Content-Encoding, although it most likely contains the stream encoding (gzip, etc.)

		if (encoding == null && metadata.get(Metadata.CONTENT_ENCODING) != null && Charset.isSupported(metadata.get(Metadata.CONTENT_ENCODING)))
			encoding = metadata.get(Metadata.CONTENT_ENCODING);

		// Optionally ignore the encoding override if a BOM is specified

		BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);

		String detectedBOMEncoding = getBOMEncoding(bufferedInputStream);

		if (detectedBOMEncoding != null && encoding != null && !encoding.equals("UTF-8"))
			logger.warn("An overriding encoding (" + encoding + ") has been specified, yet the stream contains a BOM encoding of " + detectedBOMEncoding + " - override encoding will be ignored");

		// Keep the original content for reference (and in-browser parsing)

		String entireContent = IOUtils.toString(bufferedInputStream, detectedBOMEncoding != null ? detectedBOMEncoding : encoding != null ? encoding : "UTF-8");

		metadata.add("original-content", entireContent);

		// The SubRip standard specifies that any SubRip file should be UTF-8, but we accept 1) the BOM or 2) an override

		Scanner scanner = new Scanner(entireContent);

		result.startDocument();
		result.startElement("p");

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			if (!StringUtils.isEmpty(line) && !indexPattern.matcher(line).matches() && !timestampPattern.matcher(line).matches())
				result.characters(line + "\n");
		}

		result.endElement("p");
		result.endDocument();
	}

	/**
	 * Return the BOM in the given stream, returning null if not present.
	 * 
	 * XXX: If a BOM exists, it will be consumed.
	 * 
	 * @param inputStream
	 * @return String
	 * @throws IOException
	 */
	public static String getBOMEncoding(final BufferedInputStream inputStream) throws IOException {
		int[] bytes = new int[3];

		inputStream.mark(3);

		bytes[0] = inputStream.read();
		bytes[1] = inputStream.read();
		bytes[2] = inputStream.read();

		if (bytes[0] == 0xFE && bytes[1] == 0xFF) {
			inputStream.reset();
			inputStream.read();
			inputStream.read();

			return "UTF-16BE";
		} else if (bytes[0] == 0xFF && bytes[1] == 0xFE) {
			inputStream.reset();
			inputStream.read();
			inputStream.read();

			return "UTF-16LE";
		} else if (bytes[0] == 0xEF && bytes[1] == 0xBB && bytes[2] == 0xBF)
			return "UTF-8";
		else
			inputStream.reset();

		return null;
	}
}
