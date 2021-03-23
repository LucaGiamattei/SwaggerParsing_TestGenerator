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
  - **Normal**: For every method all possible combination of many input classes are generated
  - **Quick**:  For every method only valid/non Valid combination of input are generated
  - **Pairwise**: For every method only combination of Non valid input are generated
- The requesting part has 3 possible configuration:
  - **Random**: The next test is selected randomly from the ones generated
  - **Prioritization on all failures**: The next test is the one with the higher priority calculated considering all failures.
  - **Prioritization on severe failures**: The next test is the one with the higher priority calculated considering only severe failures (return code >=500).
