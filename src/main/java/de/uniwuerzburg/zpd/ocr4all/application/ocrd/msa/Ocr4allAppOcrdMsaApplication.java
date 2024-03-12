/**
 * File:     Ocr4allAppOcrdMsaApplication.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     07.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.ocrd.msa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Triggers auto-configuration and component scanning and enables the ocr-d
 * server.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@SpringBootApplication
@ComponentScan("de.uniwuerzburg.zpd.ocr4all.application")
public class Ocr4allAppOcrdMsaApplication {

	/**
	 * The main method to start the orc-d server.
	 * 
	 * @param args The application arguments.
	 * @since 17
	 */
	public static void main(String[] args) {
		SpringApplication.run(Ocr4allAppOcrdMsaApplication.class, args);
	}

}
