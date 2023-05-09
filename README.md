# Slowwave

```text
       _.====.._
     ,:._       ~-_
         `\        ~-_
           |          `.
         ,/             ~-_
-..__..-''                ~~--..__...-
```

A slow Pixelflut server with some other benefits.

## Features

* Not high-performance
* Default [Pixelflut](https://github.com/defnull/pixelflut) commands (`HELP`, `SIZE`, `PX`)
* `OFFSET` command for setting an offset for all `PX` commands on this connection.
* Website at http://localhost:8080 for image output and some stats
* The `TOKEN` command. For more see below.

More coming soon...

### The `TOKEN` command

When the `TOKEN` command is enabled with commandline flag `--token` every `PX` command requires a token to paint.
One token is valid for the ip the request is sent from and will last 100 `PX` calls.
The usage count can be changed with the `--useCountPerToken <value>` commandline parameter.

All `PX` commands must specify an additional parameter, which is the token.

An example flow:

```text
-> PX 1 1 ff0000
<- Invalid Token

-> TOKEN
<- TOKEN a466bb98-492e-4770-a772-b0b477bf26a7 100
#         /\                                   /\
#         ||                                   ||        
#      The token                      Remaining usage count

-> PX 1 1 ff0000 a466bb98-492e-4770-a772-b0b477bf26a7
<- 
🮱
```

## Running

The recommended way to run slowwave is a containerized deployment.
Pre-build images can be found in the packages of this project.
For an easy start there is a `docker-compose.yaml` in the repository.

The docker images have versioned tags.
But the `latest` tag points to the latest dev version (this will be the bloody edge)!

## Development

To test the local dev build of slowwave a pixelpawner docker-compose can be found in the `test` folder.

### Release

For a release execute `./gradlew release` locally, it will create a release with version number.
