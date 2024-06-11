package de.jjakobus.emailrestservice.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

/**
 * Tests controller's endpoints for all request-response scenarios. Delegation of tasks to responsible services is
 * tested as well. Services are mocked to not test their implementation here (see their individual unit tests).
 *
 * @author jjakobus
 */
@WebMvcTest(controllers = EmailRestServiceController.class) // Fokus on EmailRestController
class EmailRestControllerTest {

}
