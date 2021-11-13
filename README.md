# pass-keyword-service
Service for generating keywords about a submitted manuscript. 


## Description
This service accepts a resolvable URL to a bytestream, which is the user uploaded manuscript:

`http://<host>:<port>/keywords?file=<file>`

The service validates the inputted text file, making checks to ensure the URL accepted is the manuscript. If the file is valid, then the service uses the MALLET (MAchine Learning for LanguagE Toolkit) API to generate keywords about the manuscript. 

The service then returns to the caller a JSON object that contains the top keywords of the submitted manuscript `keywords`, along with a HTTP response status code of `200` for success.

Here is an example of the output, `keywords`:

```
{
  "keywords": ["keyword1", "keyword2", "keyword3", ..., "keyword10"]
}
```

### Failure Handling
There are two types of failure: one where no keywords are generated, in which `keywords` will be an empty JSON object, and when the inputted URL is invalid. In this invalid case, a `400` HTTP code will arise along with a JSON error response:

```
{
  "error": {
    "message": "Invalid input file",
    "code": 400
  }
}
```

## Configuration
The service will not require any required environment variables, unless specified. All environment variables not specified will fulfill their default values. 

| Environment Variable  		| Description  		| Default Value |
| ------------- | ------------- | ------------- |
| PASS_KEYWORD_MAX | Do not output more than specified number of keywords | 10 |
