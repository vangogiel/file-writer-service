# file-writer-service

This is a microservice exposing an endpoint to allow thread-safe file creation upon a request.
It presents a deliberately challenging case of a file that takes a reasonable couple of seconds to create.
It prevents duplicates from beign created and manages get requests in a thread-safe way as well.

## Requirements:

 - sbt
 - Java 11
 - create "/tmp" folder on your machine 

## Run the service

```shell
sbt start
```

## Development build

This is advised to be run before the push.

```shell
sbt devBuild
```

## CI build

This is to be run before merges to master and before a release

```shell
sbt ciBuild
```
## To use the service

### Health endpoint

You can do GET to localhost:9000/health to obtain 200 OK.

### File creating endpoint

You can do POST to localhost:9000/api/server/create with following request body:
```json
{"requestId": "a random alphanumeric string (S1)"}
```

If the file has been created it will respond with:

```json
{
    "requestId": "1244rrrs5",
    "created": true,
    "fileContent": "4ELy4Ks64dBoBQ5YGA1hkfjgTTic5amco0G0SRpzUkzBBrCXVXIm67qblSBiDJ9wnTKHok8fnUhd52SyZxX3x9LYply1pLir6I29iyZrGiOAmTgqFXFwScY4uwS4inljRgnVR83jVL0GAjYCQ3c05ZgEO4AP2P5Lo14x2stqabMaRaWpAmRZcx4S9mpBTDOo1IYdfSBVulgKBzn1hToLgAM7xHdQuBPb06ABSMh1I47irIbNSwp4QQQwYd2lS7E3FgjrfBR906m0Edrqcrj8KkK4g831fhVOzCtzlHbJzMWg"
}
```

If the file is still being created it will respond with:

```json
{
    "requestId": "1244rrrs5",
    "created": false
}
```

If you send more than 2 requests within a second, the service will requests with 429, TooManyRequests.
