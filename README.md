# pass-keyword-service
Service for generating keywords about a submitted manuscript. 

## Description
This service a

The service validates the inputted text file. If the file is valid (i.e. it is a .txt file), then the service uses the MALLET (MAchine Learning for LanguagE Toolkit) API to generate keywords about the manuscript. The service then returns to the caller a JSON object that contains the top keywords of the submitted manuscript.

## Configuration
The service will not require any required environment variables, unless specified. All environment variables not specified will fulfill their default values. 


