package com.seajas.search.enricher.extensions.subrip;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.tika.detect.Detector;
import org.apache.tika.detect.NameDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 * A file extension based SubRip detector.
 * 
 * @author Jasper van Veghel <jasper@seajas.com>
 */
public class SubRipDetector implements Detector {
	/**
	 * The detector to delegate to.
	 */
	NameDetector delegate;

	/**
	 * Default constructor.
	 */
	public SubRipDetector() {
		Map<Pattern, MediaType> mediaTypes = new HashMap<Pattern, MediaType>();

		mediaTypes.put(Pattern.compile(".*\\.srt", Pattern.CASE_INSENSITIVE), MediaType.text("srt"));

		delegate = new NameDetector(mediaTypes);
	}

	/**
	 * Perform actual detection.
	 * 
	 * @param input
	 * @param metadata
	 * @return MediaType
	 */
	public MediaType detect(InputStream input, Metadata metadata) {
		return delegate.detect(input, metadata);
	}
}
