/**
 * File:     OCRDJob.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core;

import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.communication.msa.job.State;
import de.uniwuerzburg.zpd.ocr4all.application.communication.msa.job.ThreadPool;
import de.uniwuerzburg.zpd.ocr4all.application.msa.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.msa.job.SystemJob;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

/**
 * Defines OCR-D jobs for scheduler.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class OCRDJob extends Job implements SystemJob {
	/**
	 * The system process.
	 */
	private final SystemProcess process;

	/**
	 * The arguments. Null if no arguments are required.
	 */
	private final List<String> arguments;

	/**
	 * True if add the process environment to the standard output.
	 */
	private final boolean isAddEnvironmentStandardOutput;

	/**
	 * The current message.
	 */
	private String message;

	/**
	 * Creates an OCR-D job.
	 *
	 * @param threadPool                     The thread pool.
	 * @param key                            The key.
	 * @param process                        The process.
	 * @param isAddEnvironmentStandardOutput True if add the process environment to
	 *                                       the standard output.
	 * @param arguments                      The arguments.
	 * @since 17
	 */
	public OCRDJob(ThreadPool threadPool, String key, SystemProcess process, boolean isAddEnvironmentStandardOutput,
			List<String> arguments) {
		super(threadPool, key,
				"Process: '" + process.getCommand() + "'" + (arguments == null ? "" : " with arguments " + arguments));

		this.process = process;
		this.isAddEnvironmentStandardOutput = isAddEnvironmentStandardOutput;
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
			process.execute(false, isAddEnvironmentStandardOutput, arguments);

			State state = process.getExitValue() == 0 ? State.completed : State.interrupted;

			message = state.name() + " - exit value " + process.getExitValue();

			return state;
		} catch (Exception e) {
			State state = State.interrupted;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.msa.job.SystemJob#getStandardOutput()
	 */
	@Override
	public String getStandardOutput() {
		return process.getStandardOutput();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.msa.job.SystemJob#getStandardError()
	 */
	@Override
	public String getStandardError() {
		return process.getStandardError();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.msa.job.SystemJob#getExitValue()
	 */
	@Override
	public int getExitValue() {
		return process.getExitValue();
	}

}
