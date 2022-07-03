# How to run?

## Documentation
Detailed documentation of the application can be found in the `documentation.pdf` file.

## Client App
An implementation of a sample client application is provided. It exhaustively calls all of the methods that are provided in the RMI interface and verifies that the expected result is obtained.

## Server App
Upon running the server, 5 nodes are launched and each of them runs a repository and a registry. The flooding strategy is employed by the nodes to inform their peers of their existence.
