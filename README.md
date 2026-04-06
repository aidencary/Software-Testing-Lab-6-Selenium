This is a demonstration project used for CSCI 4325 Software Testing at the University of Central Arkansas.
The purpose of this demonstration is to provide an environment for creating end-to-end tests using Selenium in a Container environment.

The project requires Docker or Podman to run (I use Podman): https://podman-desktop.io/

Be sure that the docker-compose functionality is installed along with the container service.

The backend is written using Maven, SpringBoot along with an H2 database for simplicity.  It is exposed on 8080 by default.
The frontend makes use of React and Typescript, along with a Vite webserver.
The end2end-tests uses Maven and Selenium and is set up to only run in a testing profile.  

To run the front and backend, simply from the project directory, type: podman compose up -d
To run the Selenium tests, type: podman compose --profile testing up end2end-tests

