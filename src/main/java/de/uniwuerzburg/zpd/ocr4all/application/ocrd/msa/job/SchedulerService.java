/**
 * File:     SchedulerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     05.02.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

/**
 * Defines scheduler services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
@ApplicationScope
public class SchedulerService {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SchedulerService.class);

	/**
	 * The prefix to use for the names of newly created threads by task executor.
	 */
	private static final String taskExecutorThreadNamePrefix = "job";

	/**
	 * Defines thread pools.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum ThreadPool {
		/**
		 * The core thread pool.
		 */
		core("core"),
		/**
		 * The time-consuming thread pool.
		 */
		timeConsuming("tc");

		/**
		 * The label.
		 */
		private final String label;

		/**
		 * Creates a thread pool.
		 * 
		 * @param label The label.
		 * @since 1.8
		 */
		private ThreadPool(String label) {
			this.label = label;
		}

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

	}

	/**
	 * The job id.
	 */
	private int id = 0;

	/**
	 * The jobs. The key is the job id.
	 */
	private final Hashtable<Integer, Job> jobs = new Hashtable<>();

	/**
	 * The running jobs. The key is the job id.
	 */
	private final Hashtable<Integer, Job> running = new Hashtable<>();

	/**
	 * The scheduled jobs.
	 */
	private final List<Job> scheduled = new ArrayList<>();

	/**
	 * The start time.
	 */
	private final Date start = new Date();

	/**
	 * The thread pool for core jobs.
	 */
	private final ThreadPoolTaskExecutor threadPoolCore;

	/**
	 * The thread pool for time-consuming jobs.
	 */
	private final ThreadPoolTaskExecutor threadPoolTimeConsuming;

	/**
	 * Creates a scheduler service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	public SchedulerService(@Value("${ocr4all.thread.pool.size.core}") int threadPoolCoreSize,
			@Value("${ocr4all.thread.pool.size,time-consuming}") int threadPoolTimeConsumingSize) {
		super();

		/*
		 * The thread pools
		 */
		threadPoolCore = createThreadPool(ThreadPool.core.getLabel(), threadPoolCoreSize);
		threadPoolTimeConsuming = createThreadPool(ThreadPool.timeConsuming.getLabel(), threadPoolTimeConsumingSize);
	}

	/**
	 * Creates a thread pool.
	 * 
	 * @param threadName   The thread name.
	 * @param corePoolSize The core pool size.
	 * @return The thread pool.
	 * @since 1.8
	 */
	private ThreadPoolTaskExecutor createThreadPool(String threadName, int corePoolSize) {
		String name = taskExecutorThreadNamePrefix + "-" + threadName;

		ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();

		threadPool.setThreadNamePrefix(name + "-");
		threadPool.setCorePoolSize(corePoolSize);
		threadPool.setWaitForTasksToCompleteOnShutdown(false);

		threadPool.afterPropertiesSet();

		logger.info("created thread pool '" + name + "' with size " + corePoolSize + ".");

		return threadPool;
	}

	/**
	 * Returns the start time.
	 *
	 * @return The start time.
	 * @since 1.8
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Returns the job.
	 * 
	 * @param id The job id.
	 * @return The job.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public Job getJob(int id) throws IllegalArgumentException {
		Job job = jobs.get(id);

		if (job == null)
			throw new IllegalArgumentException("SchedulerService: unknown job id " + id + ".");

		return job;
	}

	/**
	 * Starts the job.
	 * 
	 * @param job The job to start.
	 * @since 1.8
	 */
	private void start(Job job) {
		job.start(ThreadPool.timeConsuming.equals(job.getThreadPool()) ? threadPoolTimeConsuming : threadPoolCore,
				instance -> schedule());

		if (job.isStateRunning())
			running.put(job.getId(), job);
	}

	/**
	 * Schedule the jobs.
	 * 
	 * @since 1.8
	 */
	private synchronized void schedule() {
		// expunge done jobs from running table
		for (Job job : new ArrayList<>(running.values()))
			if (job.isDone())
				running.remove(job.getId());

		// start scheduled jobs
		synchronized (scheduled) {
			for (Job job : scheduled)
				if (job.isStateScheduled())
					start(job);

			scheduled.clear();
		}
	}

	/**
	 * Schedules the job if it is not under scheduler control.
	 * 
	 * @param job The job to schedule.
	 * @return The job state.
	 * @since 1.8
	 */
	public synchronized Job.State schedule(Job job) {
		if (job != null && !job.isSchedulerControl() && job.schedule(++id)) {
			jobs.put(job.getId(), job);

			scheduled.add(job);

			schedule();
		}

		return job == null ? null : job.getState();
	}

	/**
	 * Cancels the job.
	 * 
	 * @param id The job id.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public void cancelJob(int id) throws IllegalArgumentException {
		getJob(id).cancel();

		schedule();
	}

	/**
	 * Expunges the done jobs.
	 * 
	 * @since 1.8
	 */
	public synchronized void expungeDone() {
		List<Integer> expunges = new ArrayList<>();
		for (int id : jobs.keySet())
			if (jobs.get(id).isDone())
				expunges.add(id);

		for (int id : expunges)
			jobs.remove(id);
	}

	/**
	 * Expunges the done job.
	 * 
	 * @param id The job id.
	 * @return True if the job could be expunged.
	 * @since 1.8
	 */
	public synchronized boolean expungeDone(int id) {
		Job job = jobs.get(id);

		if (job != null && job.isDone()) {
			jobs.remove(id);

			return true;
		} else
			return false;
	}

}
