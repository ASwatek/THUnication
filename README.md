# THUnication
Software Project 2019/2020 - Team 7


## Dependencies
- Java 11
- [`commonmark-0.13.1`](https://search.maven.org/search?q=g:com.atlassian.commonmark)
- [`commonmark-ext-autolink-0.13.1`](https://search.maven.org/search?q=g:com.atlassian.commonmark)
- [`autolink-0.10.0`](https://search.maven.org/remotecontent?filepath=org/nibor/autolink/autolink/0.10.0/autolink-0.10.0.jar)
- [`sqlite-jdbc-3.27.2.1`](https://bitbucket.org/xerial/sqlite-jdbc/downloads/)

## How to start the server?
The server can be started by starting `ServerApp`. It expects the port where it should listen as a command line argument.

Example with jar from the [releases](https://github.com/Fabii547/THUnication/releases): `java -jar ServerApp.jar 9001`


## How to start the application?
For starting the application the two environment variables `THUNICATION_SERVER_ADDRESS` and `THUNICATION_SERVER_PORT` has to be defined with
proper values.

Example: If the server is running locally on port 9001 the environment variables should have the following values:
- `THUNICATION_SERVER_ADDRESS=127.0.0.1` 
- `THUNICATION_SERVER_PORT=9001`
