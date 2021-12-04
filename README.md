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
  keywords: 
    0:  "keyword0"
    1:  "keyword1"
    2:  "keyword2"
    ...
    n:  "keywordn"
}
```

### Failure Handling
There are four types of failures:
Error Type | Description | Error Message Response |
---------- | ----------- | ---------------------- |
Invalid URL Format | URL request containing manuscript is not in the correct configuration as configued in the web.xml | `Supplied URL is not in valid format.`
Unable to Parse URL input stream | Cannot parse PDF contents of URL request to parsed text | `Supplied manuscript file cannot be parsed.`
Unable to Evaluate Keywords | Cannot evaluate keywords using PassKeywordService | `Cannot evaluate keywords.`
No Keywords Found | No keywords could be found from manuscript | `No Keywords found.`

In each invalid case, a `400` HTTP code will arise along with a JSON error response with the relevant error message:
```
{
  error: {
    message: "Error Response Here",
    code: 400
  }
}
```

## Configuration
The service will not require any required environment variables, unless specified. All environment variables not specified will fulfill their default values. 

| Environment Variable  		| Description  		| Default Value |
| ------------- | ------------- | ------------- |
| PASS_KEYWORD_MAX | Do not output more than specified number of keywords | 10 |
