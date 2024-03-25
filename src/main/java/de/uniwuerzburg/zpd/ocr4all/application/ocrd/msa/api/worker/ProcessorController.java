/**
 * File:     ProcessorController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     13.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.api.worker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.msa.api.domain.JobResponse;
import de.uniwuerzburg.zpd.ocr4all.application.ocrd.communication.api.DescriptionResponse;
import de.uniwuerzburg.zpd.ocr4all.application.ocrd.communication.api.ProcessRequest;
import de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa.core.ProcessorService;
import jakarta.validation.Valid;

/**
 * Defines processor controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@RestController
@RequestMapping(path = ProcessorController.contextPath, produces = CoreApiController.applicationJson)
public class ProcessorController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/processor";

	/**
	 * The processor service.
	 */
	private final ProcessorService service;

	/**
	 * Creates a processor controller for the api.
	 * 
	 * @param service The processor service.
	 * @since 17
	 */
	public ProcessorController(ProcessorService service) {
		super(ProcessorController.class);

		this.service = service;
	}

	/**
	 * Returns the processor json description in the response body.
	 * 
	 * @param processor The processor.
	 * @return The processor json description in the response body.
	 * @since 1.8
	 */
	@GetMapping(descriptionRequestMapping + jsonRequestMapping + processorPathVariable)
	public ResponseEntity<DescriptionResponse> jsonDescription(@PathVariable String processor) {
		try {
			return ResponseEntity.ok().body(new DescriptionResponse(service.getDescriptionJson(processor.trim())));
		} catch (IllegalArgumentException ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Executes the process and returns the job in the response body.
	 * 
	 * @param request The process request.
	 * @return The job in the response body.
	 * @since 1.8
	 */
	@PostMapping(executeRequestMapping)
	public ResponseEntity<JobResponse> execute(@RequestBody @Valid ProcessRequest request) {
		try {
			return ResponseEntity.ok().body(new JobResponse(service.start(request.getKey(), request.getFolder(),
					request.getProcessor(), request.getInput(), request.getOutput(), request.getArguments())));
		} catch (IllegalArgumentException ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
}
