/**
 * File:     OCRDJob.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core;

import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.msa.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.msa.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

/**
 * Defines OCR-D jobs for scheduler.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class OCRDJob extends Job {
	/**
	 * The system process.
	 */
	private final SystemProcess process;

	/**
	 * The arguments. Null if no arguments are required.
	 */
	private final List<String> arguments;

	/**
	 * The current message.
	 */
	private String message;

	/**
	 * Creates an OCR-D job.
	 *
	 * @param threadPool  The thread pool.
	 * @param key         The key.
	 * @param description The description.
	 * @since 17
	 */
	public OCRDJob(SchedulerService.ThreadPool threadPool, String key, SystemProcess process, List<String> arguments) {
		super(threadPool, key,
				"Process: '" + process.getCommand() + "'" + (arguments == null ? "" : " with arguments " + arguments));

		this.process = process;
		this.arguments = arguments;

		message = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.msa.job.Job#getMessage()
	 */
	@Override
	public String getMessage() {
		return message == null ? getState().name() : message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.msa.job.Job#execute()
	 */
	@Override
	protected State execute() {
		try {
			process.execute(arguments);

			State state = process.getExitValue() == 0 ? State.completed : State.interrupted;

			message = state.name() + " - exit value " + process.getExitValue();

			return state;
		} catch (Exception e) {
			State state =  State.interrupted;
			
			message = state.name() + " - " + e.getMessage();

			return state;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.msa.job.Job#kill()
	 */
	@Override
	protected void kill() {
		process.cancel();
	}

	/**
	 * Returns the system process standard output.
	 *
	 * @return The system process standard output.
	 * @since 1.8
	 */
	public String getStandardOutput() {
		return process.getStandardOutput();
	}

	/**
	 * Returns the system process standard error.
	 *
	 * @return The system process standard error.
	 * @since 1.8
	 */
	public String getStandardError() {
		return process.getStandardError();
	}

	/**
	 * Returns the exit value. By convention, the value 0 indicates normal
	 * termination. -1 if the exit value is not set.
	 *
	 * @return The exit value.
	 * @since 1.8
	 */
	public int getExitValue() {
		return process.getExitValue();
	}

}
