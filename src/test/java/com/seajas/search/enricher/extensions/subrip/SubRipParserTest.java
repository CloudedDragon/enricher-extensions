package com.seajas.search.enricher.extensions.subrip;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import nl.minbzk.dwr.zoeken.enricher.ProcessorResult;
import nl.minbzk.dwr.zoeken.enricher.aci.ImportEnvelope;
import nl.minbzk.dwr.zoeken.enricher.aci.ImportEnvelopeParser;
import nl.minbzk.dwr.zoeken.enricher.processor.PreProcessor;
import nl.minbzk.dwr.zoeken.enricher.processor.ProcessorContext;
import nl.minbzk.dwr.zoeken.enricher.processor.TikaProcessor;
import nl.minbzk.dwr.zoeken.enricher.processor.preprocessor.HTMLPreProcessor;
import nl.minbzk.dwr.zoeken.enricher.settings.EnricherSettings;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
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
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new InputStreamReader(new FileInputStream("support/examples/video-envelope.xml"), "UTF-8")));

		// Create a new envelope from the static definition

		ImportEnvelope envelope = ImportEnvelopeParser.parse(document).get(0);

		// Simple load the internal configuration file twice, since we don't need any jobs within these settings

		EnricherSettings settings = new EnricherSettings("support/examples/video-enricher.properties");

		settings.setLanguageDetectionProfiles("support" + File.separator + "profiles");

		// Create a new TikaProcessor to process through

		TikaProcessor processor = new TikaProcessor(settings);

		processor.setPreprocessors(new HashMap<String, List<PreProcessor>>());
		processor.getPreprocessors().put("html", Arrays.asList(new PreProcessor[] { new HTMLPreProcessor() }));

		// Process the envelope

		ProcessorResult result = processor.process(envelope, settings, settings.getJob("ProfilerWebsites"), new ProcessorContext());

		// Encoding

		assertEquals(result.getMimeType(), "text/srt");

		// Language

		assertEquals(result.getLanguage(), "nl");

		// Content

		assertTrue(result.getContent().get(0).getContent().startsWith("STRAATGELUIDEN"));

		// Metadata

		assertTrue(result.getMetadata().containsKey("original-content"));
		assertTrue(result.getMetadata().get("original-content").get(0).startsWith("1"));
	}
}