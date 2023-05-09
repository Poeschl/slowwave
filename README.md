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

More coming soon...

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
