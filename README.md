# Jysics - Physics Engine in Java

Jysics is a physics engine written in Java that simulates the behavior of balls and rectangles in a 2D environment. It includes features such as collision detection, friction, and gravity.

## Installation

To use Jysics, you will need to have Java installed on your system. You can download the latest version of Java from the [official website](https://www.java.com/en/download/).

Once you have Java installed, you can download the Jysics source code from the [GitHub repository](https://github.com/your-username/jysics). You can either clone the repository using Git or download the ZIP file and extract it.

## Usage

To run the Jysics simulation, navigate to the project directory in your terminal and run the following command:

```
java Main
```

This will launch the Jysics application, which includes a user interface for creating and interacting with physics objects.

### Creating Objects

You can create balls and rectangles by clicking and dragging in the simulation area. The radius and mass of the objects can be adjusted using the input fields in the UI.

### Interacting with Objects

Once objects are created, you can interact with them by clicking and dragging to apply forces. The objects will collide and respond to the forces applied.

## API

The Jysics API consists of the following classes:

- `PhysicsWorld`: Manages the physics simulation and the objects within it.
- `Ball`: Represents a circular physics object.
- `Square`: Represents a rectangular physics object.
- `EngineLoop`: Handles the update loop for the simulation.
- `PhysicsUI`: Provides the user interface for interacting with the simulation.

## Contributing

If you would like to contribute to the Jysics project, please follow these steps:

1. Fork the repository on GitHub.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them to your branch.
4. Submit a pull request to the main repository.

## License

Jysics is released under the [MIT License](LICENSE).

## Testing

Jysics includes a set of unit tests to ensure the correctness of the physics simulation. You can run the tests by executing the following command in the project directory:

```
./gradlew test
```

This will run the test suite and report the results.
