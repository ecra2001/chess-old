# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

# Phase 2 Diagram
https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3SblVZPQN++oQADW6ErU32jsohfgyHM5QATE4nN0y0MxWMYFXHlNa6l6020C3Vgd0BxTF5fP4AtB2OSYAAZCDRJIBNIZLLdvJF4ol6p1JqtAzqBJoIei0azF5vDgHYsgwr5kvDrco7jE8H5LOMALnAWspqig5QIAePKwvuh6ouisTYgmhgumGbpkhSBq0uWo4mkS4YWhyMDcryBqCsKMCvmIrrSkml6weUBpaPIjrOgSuEsuUwA2rGwbaLCIkhsx5qRpRACSaAdMwEnaDxibJlBJYoTy2a5pg-4gjBJRXAMxGjCBU5BrOzb-O2BmFNkPYwP2g69CZI5maUNaWY21mLqYy6rt4fiBF4KDoHuB6+Mwx7pJkmAOReRTUNe0gAKK7ql9Spc0LQPqoT7dNOVloLZbL6cZRU+fONn6WyWHwZFvrIY1YBoRimFythfFkXhMDkmAIkBt5c6kUyboUeU1ExkGdFhJVc6hj1LGGeCMAiVxwCqV1Mj8aSMCQsMEA0IN83oKNZoRoUlrpodNBrTN2hCnNw3oFtnblRF9hRemOYIHmGl2UlRmlt+V6AwlYB9gOQ5LpwgXroEkK2ru0IwAA4qOrIxae8XnswhnXmjmU5fYo6FS9JU-mVAMVRTux9JBQIdit8owMgsQY6MqiwuzrUoGi7VvThS17f1J0U+d5EyZNPLTXGj30adJVSRGLNwfd8vcVhGq7e6fUUijsSwpL43S1Rst9aODoq529V7qjEAAGaW6M8ade9NMuyght879-1M4DP7GaTXPjBU-QhygsnSOMMAAIy9gAzAALE8J6ZAaFafE8OgIKADaZ8B2dTJHAByo7-I0oPJeDeNQy5EeY2HjejNHscJynaexfqpn3NWUy5-nhcef3fRlxXi5V2YcOeEFG7YD4UDYNw8C6pk6Ojik3e4zk+NscDN4NCTZPBBTQ7j6M1fHOpAfB6O5cj48fRK-TjOpnVnXlJ6eqcygsLf5kNqGEhbdTGgJfWA0gxDTrFVE2LErqUSmhrUS8gnowCVotMB0k1blGUlrd2wssF6yEhwX+sJI6SV1hNGA8lFJe0FDySAXsNA21YkDVakdo5CxvqmL+a8UC-x0n9PSANbZgzvq3GO5QO7JyvszU4EN65DjHqONu0ik6yOniuWeCMAiWBQMqCAyQYAACkIA8g3qMQIg8QANh3uYD+NdyjVEpHeFokdyYwLnEOZewB9FQDgBABCUBZicOkHI38Zxb6lmeHnfxgTgkrAAOosFkllFoAAhXcCg4AAGkvhhPbhoryXjfJtlqjgmAAArcxaAyE1O0vzdCWIQE7RFnrMWUClZwOkggmWvI8HADQRg1hlTBmtJVvhMAZCwk9MuuyKZ9CFZhDCZgi6Yj2JLPwbBHW7Tyh+C4gI0c5CrbaDmdQg5epLEoDdjsthH0GloBQKQ0cIiA4bOBj0CJnZFFOWhr0WG2i1zBQCF4PxXYvSwGANgZehB4iJC3jjCGjiD5pQyllHKxhSo8OBOURAkL0wC1iG89+lSQDcDwEArA2tCEXS-hSqABoWFULNuSgltEVI0tAXSmAbK8AiTWVLPpvKGXoOVMg25TpEzcvDPSyFv9mXtOoXyqF4rf6SudDiksHADEUhQGQHw4YSXQQJmmL52KFF1z+Q3QFmAgA
