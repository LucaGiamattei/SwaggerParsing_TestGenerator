# SwaggerParsing_TestGenerator

## Description
This tool implements:
- Parsing of an OpenAPI specification
- Generation of different types of requests depending on the configuration
- Requesting a service with the generated requests selected in 2 different ways (random, prioritizing)

## File needed to execute

The executable Jar needs a txt configuration file.

## Configuration
Read one of the example config file for details.
- The request generation has 3 possible configuration:
  - **Normal**:
  - **Quick**:
  - **Pairwise**:
- The requesting part has 3 possible configuration:
  - **Random**:
  - **Prioritization on all failures**:
  - **Prioritization on severe failures**:
