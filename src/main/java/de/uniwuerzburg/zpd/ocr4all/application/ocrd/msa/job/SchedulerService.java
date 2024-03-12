/**
 * File:     SchedulerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     05.02.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.job;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import de.uniwuerzburg.zpd.ocr4all.application.communication.message.Message;
import de.uniwuerzburg.zpd.ocr4all.application.communication.message.spi.EventSPI;
import de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.message.WebSocketService;

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
	private final Set<Job> jobs = new HashSet<>();

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
	 * The WebSocket service.
	 */
	private final WebSocketService webSocketService;

	/**
	 * Creates a scheduler service.
	 * 
	 * @param threadPoolCoreSize          The size of the thread pool for core jobs.
	 * @param threadPoolTimeConsumingSize The size of the thread pool for
	 *                                    time-consuming jobs.
	 * @param webSocketService            The WebSocket service.
	 * @since 17
	 */
	public SchedulerService(@Value("${ocr4all.thread.pool.size.core}") int threadPoolCoreSize,
			@Value("${ocr4all.thread.pool.size,time-consuming}") int threadPoolTimeConsumingSize,
			WebSocketService webSocketService) {
		super();

		/*
		 * The thread pools
		 */
		threadPoolCore = createThreadPool(ThreadPool.core.getLabel(), threadPoolCoreSize);
		threadPoolTimeConsuming = createThreadPool(ThreadPool.timeConsuming.getLabel(), threadPoolTimeConsumingSize);

		this.webSocketService = webSocketService;
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
	 * Starts the job if it is not under scheduler control.
	 * 
	 * @param job The job to start.
	 * @since 1.8
	 */
	public void start(Job job) {
		if (job != null && !job.isSchedulerControl()) {
			job.schedule(
					ThreadPool.timeConsuming.equals(job.getThreadPool()) ? threadPoolTimeConsuming : threadPoolCore,
					++id, entity -> sendEvent(entity));

			synchronized (job) {
				jobs.add(job);
			}
		}
	}

	/**
	 * Broadcasts an event to the registered clients on the WebSocket if the job
	 * state is not 'initialized'.
	 * 
	 * @param job The job. It is mandatory and cannot be null.
	 * @since 17
	 */
	private void sendEvent(Job job) {
		EventSPI.Type type;
		switch (job.getState()) {
		case scheduled:
			type = EventSPI.Type.scheduled;
			break;
		case running:
			type = EventSPI.Type.running;
			break;
		case canceled:
			type = EventSPI.Type.canceled;
			break;
		case completed:
			type = EventSPI.Type.completed;
			break;
		case interrupted:
			type = EventSPI.Type.interrupted;
			break;
		default:
			type = null;
			break;
		}

		if (type != null)
			webSocketService.broadcast(new EventSPI(type, job.getKey(), new Message(job.getMessage())));
	}

	/**
	 * Cancels the job.
	 * 
	 * @param id The job id.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public void cancel(Job job) throws IllegalArgumentException {
		if (job != null)
			job.cancel();
	}

	/**
	 * Expunges the done jobs.
	 * 
	 * @since 1.8
	 */
	public void expunge() {
		synchronized (jobs) {
			jobs.removeIf(job -> job.isDone());
		}
	}

	/**
	 * Expunges the given job is it is done.
	 * 
	 * @param id The job.
	 * @return True if the job could be expunged.
	 * @since 1.8
	 */
	public synchronized boolean expunge(Job job) {
		if (job != null && job.isDone()) {
			synchronized (jobs) {
				jobs.remove(job);
			}

			return true;
		} else
			return false;
	}

}
