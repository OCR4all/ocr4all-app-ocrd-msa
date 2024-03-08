/**
 * File:     Job.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.01.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.job;

import java.util.Date;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Defines jobs for scheduler.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class Job {
	/**
	 * Defines job states.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum State {
		/**
		 * The initialized state.
		 */
		initialized,
		/**
		 * The scheduled state.
		 */
		scheduled,
		/**
		 * The running state.
		 */
		running,
		/**
		 * The completed state.
		 */
		completed,
		/**
		 * The canceled state.
		 */
		canceled,
		/**
		 * The interrupted state.
		 */
		interrupted;
	}

	/**
	 * The logger.
	 */
	protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Job.class);

	/**
	 * The id. 0 if not set, this means, it is not under the control of the
	 * scheduler.
	 */
	private int id = 0;

	/**
	 * The state. The initial state is initialized.
	 */
	private State state = State.initialized;

	/**
	 * The created time.
	 */
	private final Date created = new Date();

	/**
	 * The start time.
	 */
	private Date start = null;

	/**
	 * The end time.
	 */
	private Date end = null;

	/**
	 * Creates a job
	 *
	 * @since 1.8
	 */
	Job() throws IllegalArgumentException {
		super();
	}

	/**
	 * Returns the short description.
	 *
	 * @return The short description.
	 * @since 1.8
	 */
	public abstract String getShortDescription();

	/**
	 * Returns the thread pool.
	 *
	 * @return The thread pool.
	 * @since 1.8
	 */
	public abstract SchedulerService.ThreadPool getThreadPool();

	/**
	 * Executes the job if it is in scheduled state.
	 *
	 * @return The end state of the execution, this means, canceled, completed or
	 *         interrupted.
	 * @since 1.8
	 */
	protected abstract State execute();

	/**
	 * Kills the job if it is not done.
	 *
	 * @since 1.8
	 */
	protected abstract void kill();

	/**
	 * Returns the id. 0 if not set, this means, it is not under the control of the
	 * scheduler.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns true if the job is under the control of the scheduler.
	 *
	 * @return True if the job is under the control of the scheduler.
	 * @since 1.8
	 */
	public boolean isSchedulerControl() {
		return !State.initialized.equals(state);
	}

	/**
	 * Schedules the job if it is not under the control of the scheduler and the
	 * given id is greater than 0.
	 *
	 * @param id The job id.
	 * @return True if the job was scheduled.
	 * @since 1.8
	 */
	boolean schedule(int id) {
		if (!isSchedulerControl() && id > 0) {
			state = State.scheduled;
			this.id = id;

			return true;
		} else
			return false;
	}

	/**
	 * Returns true if the state is scheduled.
	 *
	 * @return True if the state is scheduled.
	 * @since 1.8
	 */
	public boolean isStateScheduled() {
		return State.scheduled.equals(state);
	}

	/**
	 * Returns true if the state is running.
	 *
	 * @return True if the state is running.
	 * @since 1.8
	 */
	public boolean isStateRunning() {
		return State.running.equals(state);
	}

	/**
	 * Returns true if the job is done.
	 *
	 * @return True if the job is done.
	 * @since 1.8
	 */
	public boolean isDone() {
		switch (state) {
		case canceled:
		case completed:
		case interrupted:
			return true;

		default:
			return false;
		}
	}

	/**
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the created time.
	 *
	 * @return The created time.
	 * @since 1.8
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Returns the start time. Null if not started.
	 *
	 * @return The start time.
	 * @since 1.8
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Returns the end time. Null if not ended.
	 *
	 * @return The end time.
	 * @since 1.8
	 */
	public Date getEnd() {
		return end;
	}

	/**
	 * Defines callback.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface Callback {
		/**
		 * Callback method at the end of the job.
		 *
		 * @param job The job that has been done.
		 * @since 1.8
		 */
		public void done(Job job);
	}

	/**
	 * Starts the job in a new thread if it is in scheduled state.
	 *
	 * @param taskExecutor The task executor.
	 * @param callback     The callback method when the job finishes. If null, no
	 *                     callback is performed.
	 * @return The job state.
	 * @since 1.8
	 */
	synchronized State start(ThreadPoolTaskExecutor taskExecutor, Callback callback) {
		if (isStateScheduled()) {
			state = State.running;
			start = new Date();

			taskExecutor.execute(() -> {
				logger.info("Start execution of job ID " + getId() + ".");

				State executionState = execute();

				if (!State.canceled.equals(state)) {
					state = State.completed.equals(executionState) ? State.completed : State.interrupted;

					end = new Date();
				}

				if (callback != null)
					callback.done(Job.this);

				logger.info("End execution of the job ID " + getId() + ".");
			});
		}

		return state;
	}

	/**
	 * Cancels the job in a new thread if it is not done.
	 *
	 * @return The state of the job. It is cancelled if the job was not done.
	 *         Otherwise, the state of the done job is returned.
	 * @since 1.8
	 */
	synchronized State cancel() {
		if (isStateScheduled() || isStateRunning()) {
			final boolean isRunning = State.running.equals(state);

			state = State.canceled;
			end = new Date();

			if (isRunning)
				new Thread(new Runnable() {
					/*
					 * (non-Javadoc)
					 *
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						kill();
					}
				}).start();
		}

		return state;
	}
}
