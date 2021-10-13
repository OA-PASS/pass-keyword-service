# pass-keyword-service
Service for generating keywords about a submitted manuscript. 


## Description
This service accepts a resolvable URL to a bytestream, which is the user uploaded manuscript.

The service validates the inputted text file. If the file is valid (i.e. it is a .txt file), then the service uses the MALLET (MAchine Learning for LanguagE Toolkit) API to generate keywords about the manuscript. The service then returns to the caller a JSON object that contains the top keywords of the submitted manuscript `keywords`.

Here is an example of the output, `keywords`:

```
{
  "keywords": {
      "keywords": "keyword1 keyword2 keyword3 keyword4 keyword5 ..."
  }
}
```
*Note that for failure, the service outpute a blank `keywords`*


## Configuration
The service will not require any required environment variables, unless specified. All environment variables not specified will fulfill their default values. 

| Environment Variable  		| Description  		| Default Value |
| ------------- | ------------- | ------------- |
| PASS_KEYWORD_MAX | Do not output more than specified number of keywords | 10 |


