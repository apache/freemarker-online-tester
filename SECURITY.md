# Security Scan Guidance

This document is for AI security scanners to understand the security model of this project, and to avoid false positives.

## Intended Use

This project is deployed as a public website (no login is needed). It's not used as a dependency of other
projects. Also, currently it produces no official releases, such as to Maven Central, as it's only used as part of the
Apache FreeMarker website.

## Security Scope

The relevant attack vectors are those via HTTP requests, such as through a web browser.

Users submit an arbitrary FreeMarker template and a data-model description via an HTML form. The
template is then processed on the server-side, and the resulting output or error message is shown on the website.

As the submitted FreeMarker template runs on the server-side, it's important to limit what it can
do, such as:

  - It must not be possible to return sensitive information from the server
  - It must not be possible to change the configuration of the server
  - It must not be possible to deface the website
  - It must not be possible to cause the website to run arbitrary JavaScript, or implement other kinds of XSS attacks

Because of the nature of the template language, Denial-of-Service attacks can't be fully prevented. The main goal is
to recover from user mistakes, where the user submits a template that runs for too long, or produces long output.
Therefore, we intend to make a reasonable effort to terminate long-running templates, and limit the size of the output.
For malicious attacks, we accept that the service can be disrupted, as it's not a critical service, and runs in its own
VM to not drag down the main website.
