# myraindrop-exercise

## Run the service

```shell
sbt run start
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

You can do GET to localhost/health to obtain 200 OK.
