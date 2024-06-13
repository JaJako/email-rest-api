package de.jjakobus.emailrestservice.model;

/**
 * Represents the state of an email.
 *
 * @author jjakobus
 */
public enum EmailState {

  /** An email that has been created but not sent yet. */
  DRAFT,

  /** An email that has been sent. */
  SENT,

  /** An email that has been deleted, e.g. moved to trash bin. */
  DELETED,

  /** An email that has been classified as spam. */
  SPAM

}
