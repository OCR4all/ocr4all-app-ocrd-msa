/**
 * File:     ProcessorService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     13.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

/**
 * Defines processor services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class ProcessorService {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProcessorService.class);

	/**
	 * The json description parameter.
	 */
	private final String jsonDescriptionParameter;

	/**
	 * Creates a processor service.
	 * 
	 * @param jsonDescriptionParameter The json description parameter.
	 * @since 17
	 */
	public ProcessorService(@Value("${ocr4all.ocrd.parameter.description.json}") String jsonDescriptionParameter) {
		super();

		this.jsonDescriptionParameter = jsonDescriptionParameter;
	}

	/**
	 * Returns the json processor description.
	 * 
	 * @param processor The processor.
	 * @return The json processor description.
	 * @throws IOException Throws if an I/O exception of some sort has occurred or
	 *                     the process is already running.
	 * @since 17
	 */
	public String getDescriptionJson(String processor) throws IOException {
		SystemProcess process = new SystemProcess(processor);

		process.execute(jsonDescriptionParameter);

		if (process.getExitValue() == 0)
			return process.getStandardOutput();
		else
			throw new IOException(
					process.getStandardError().trim() + " (process exit code " + process.getExitValue() + ")");
	}
}
