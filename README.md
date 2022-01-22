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
Error Type | Description | Error Message Response | Response Code
---------- | ----------- | ---------------------- | -------------|
Invalid URL Format | URL request containing manuscript is not in the correct configuration as configued in the web.xml | `Supplied URL is not in valid format.` | 400
Unable to Parse URL input stream | Cannot parse PDF contents of URL request to parsed text | `Supplied manuscript file cannot be parsed.` | 415
Unable to Evaluate Keywords | Cannot evaluate keywords using PassKeywordService | `Cannot evaluate keywords.` | 422
No Keywords Found | No keywords could be found from manuscript | `No Keywords found.` | 500

In each error case, a JSON object is outputted with the following format:
```
{
  error: {
    message: "Error Response Here",
  }
}
```
## Running and Testing the Service

### Running the Service
To run the service,
  1. Run `mvn install` to install the pass-keyword-service.war inside the `target` directory.
  2. Run `docker build -t {name of image} .` to build the docker image with your desired image name.
  3. Run `docker run -p {port of choice}:8080 {name of image}` to deploy and run the docker image.
      1. Optionally, environment variables can be overriden through the `-e` flag like so: `docker run -p {port of choice}:8080 -e MAXKEYWORDS=5 {name of image}` *note: `-e` must preceed each overridden environment variable*

### Testing the Service
To test the service,
  1. Run `mvn install` or `mvn verify` to run the unit and integration tests.

### Configuration
The service will not require any environment variables to be specified. All environment variables not specified will fulfill their default values. 

| Environment Variable  		| Description  		| Default Value |
| ------------- | ------------- | ------------- |
HOSTURL | the host URL of the manuscript URL passed in | pass.local
CONTEXTPATH | the context path of the manuscript URL | /fcrepo/rest/submissions
MAXKEYWORDS | the maximum amount of keywords outputted by the service | 10
