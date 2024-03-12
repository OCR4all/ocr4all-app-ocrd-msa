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
	 * The thread pool.
	 */
	private final SchedulerService.ThreadPool threadPool;

	/**
	 * The key.
	 */
	private final String key;

	/**
	 * The description.
	 */
	private final String description;

	/**
	 * Creates a job
	 *
	 * @since 1.8
	 */
	Job(SchedulerService.ThreadPool threadPool, String key, String description) {
		super();

		this.threadPool = threadPool;

		this.key = key;
		this.description = description;
	}

	/**
	 * Returns the thread pool.
	 *
	 * @return The thread pool.
	 * @since 1.8
	 */
	public SchedulerService.ThreadPool getThreadPool() {
		return threadPool;
	}

	/**
	 * Returns the key.
	 *
	 * @return The key.
	 * @since 1.8
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns the description.
	 *
	 * @return The description.
	 * @since 1.8
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the message.
	 * 
	 * @return The message.
	 * @since 17
	 */
	protected abstract String getMessage();

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
		 * Callback method to send an event.
		 *
		 * @param job The job sending the event.
		 * @since 1.8
		 */
		public void event(Job job);
	}

	/**
	 * Schedules the job if it is not under the control of the scheduler.
	 *
	 * @param taskExecutor The task executor. The thread pool to execute the job.
	 * @param id           The job id. This is a positive number.
	 * @param callback     The callback method when the job finishes. If null, no
	 *                     callback is performed.
	 * @since 1.8
	 */
	synchronized void schedule(ThreadPoolTaskExecutor taskExecutor, int id, Callback callback) {
		if (!isSchedulerControl() && id > 0) {
			state = State.scheduled;
			this.id = id;

			logger.info("scheduled job ID " + getId() + ".");

			if (callback != null)
				callback.event(Job.this);

			taskExecutor.execute(() -> {
				if (isStateScheduled()) {
					state = State.running;
					start = new Date();

					logger.info("start execution of job ID " + getId() + ".");

					if (callback != null)
						callback.event(Job.this);

					State executionState = execute();

					if (!State.canceled.equals(state)) {
						state = State.completed.equals(executionState) ? State.completed : State.interrupted;

						end = new Date();
					}

					logger.info("end execution of the job ID " + getId() + " with state '" + state.name() + "'.");
				}

				if (callback != null)
					callback.event(Job.this);

			});
		}
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

			logger.info("canceled job ID " + getId() + ".");
			
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
