package com.seajas.search.enricher.extensions.subrip;

import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * SubRipParser unit test.
 * 
 * @author Jasper van Veghel <jasper@seajas.com>
 */
public class SubRipParserTest {
	/**
	 * Test extracting proper subtitles from an .srt file.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSubRipParser() throws Exception {
		InputStream inputStream = new FileInputStream("support/examples/video-content.srt");

		SubRipParser parser = new SubRipParser();

		Metadata metadata = new Metadata();
		ParseContext context = new ParseContext();
		BodyContentHandler handler = new BodyContentHandler();

		// Support .srt

		assertTrue("Parser supports .srt", parser.getSupportedTypes(context).contains(MediaType.text("srt")));

		// Parse an srt file

		parser.parse(inputStream, handler, metadata, context);

		assertTrue(new ContentHandlerDecorator(handler).toString().startsWith("STRAATGELUIDEN"));

		// Metadata

		assertTrue(metadata.get("original-content") != null);
		assertTrue(metadata.get("original-content").startsWith("1"));
	}
}