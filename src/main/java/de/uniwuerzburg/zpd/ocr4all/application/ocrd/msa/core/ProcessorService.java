/**
 * File:     ProcessorService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     13.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.msa.job.SchedulerService;
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
	 * The ocr4all projects folder.
	 */
	private final Path projectsFolder;

	/**
	 * The json description parameter.
	 */
	private final String jsonDescriptionParameter;

	/**
	 * The input folder parameter.
	 */
	private final String inputParameter;

	/**
	 * The output folder parameter.
	 */
	private final String outputParameter;

	/**
	 * The processors to be run on the time-consuming thread pool.
	 */
	private final Set<String> timeConsuming = new HashSet<>();;

	/**
	 * The scheduler service.
	 */
	private final SchedulerService schedulerService;

	/**
	 * Creates a processor service.
	 * 
	 * @param projectsFolder           The ocr4all projects folder.
	 * @param jsonDescriptionParameter The json description parameter.
	 * @param timeConsuming            The processors to be run on the
	 *                                 time-consuming thread pool.
	 * @param schedulerService         The scheduler service.
	 * @since 17
	 */
	public ProcessorService(@Value("${ocr4all.projects.folder}") String projectsFolder,
			@Value("${ocr4all.ocrd.parameter.description.json}") String jsonDescriptionParameter, 
			@Value("${ocr4all.ocrd.parameter.folder.input}") String inputParameter,
			@Value("${ocr4all.ocrd.parameter.folder.output}") String outputParameter,
			@Value("#{'${ocr4all.ocrd.processors.time-consuming}'.split(',')}") List<String> timeConsuming,
			SchedulerService schedulerService) {
		super();

		this.projectsFolder = Paths.get(projectsFolder).normalize();

		this.jsonDescriptionParameter = jsonDescriptionParameter;
		this.inputParameter = inputParameter;
		this.outputParameter = outputParameter;

		this.schedulerService = schedulerService;

		for (String processor : timeConsuming)
			if (processor != null && !processor.isBlank())
				this.timeConsuming.add(processor.trim());
	}

	/**
	 * Returns the json processor description.
	 * 
	 * @param processor The processor.
	 * @return The json processor description.
	 * @throws IOException              Throws if an I/O exception of some sort has
	 *                                  occurred or the process is already running.
	 * @throws IllegalArgumentException Throws if the command is not defined.
	 * @since 17
	 */
	public String getDescriptionJson(String processor) throws IOException, IllegalArgumentException {
		SystemProcess process = new SystemProcess(processor);

		process.execute(jsonDescriptionParameter);

		if (process.getExitValue() == 0)
			return process.getStandardOutput();
		else
			throw new IOException(
					process.getStandardError().trim() + " (process exit code " + process.getExitValue() + ")");
	}

	/**
	 * Creates a job to perform the process and starts it on the scheduler.
	 * 
	 * @param key       The job key.
	 * @param folder    The working directory of the job. It is relative to the
	 *                  project folder.
	 * @param processor The OCR-D processor.
	 * @param input     The OCR-D input folder.
	 * @param output    The OCR-D output folder.
	 * @param arguments The OCR-D processor arguments.
	 * @return The scheduled job.
	 * @throws IllegalArgumentException Throws on folder troubles.
	 * @since 17
	 */
	public OCRDJob start(String key, String folder, String processor, String input, String output, List<String> arguments)
			throws IllegalArgumentException {
		if (folder == null || folder.isBlank())
			throw new IllegalArgumentException("the folder parameter is not defined");

		if (input == null || input.isBlank())
			throw new IllegalArgumentException("the input folder parameter is not defined");

		if (output == null || output.isBlank())
			throw new IllegalArgumentException("the output folder parameter is not defined");

		Path path = Paths.get(projectsFolder.toString(), folder.trim()).normalize();

		if (!path.startsWith(projectsFolder) || !Files.isDirectory(path))
			throw new IllegalArgumentException("the folder is not a valid directory");
		
		arguments.addAll(0, Arrays.asList(inputParameter, input, outputParameter, output));

		OCRDJob job = new OCRDJob(timeConsuming.contains(processor.trim()) ? SchedulerService.ThreadPool.timeConsuming
				: SchedulerService.ThreadPool.standard, key, new SystemProcess(path, processor), arguments);
		schedulerService.start(job);

		return job;
	}	

}
