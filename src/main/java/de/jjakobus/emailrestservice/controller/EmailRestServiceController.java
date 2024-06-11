package de.jjakobus.emailrestservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Defines CRUD endpoints of the Email REST API and delegates the tasks to responsible service(s).
 *
 * @author jjakobus
 */
@RestController()
@RequestMapping("${email-rest-service.request-path}")
public class EmailRestServiceController {

  public EmailRestServiceController() {
  }
}
