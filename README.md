Maven-Proxy
----

A lightweight proxy for Maven Repositories

## What is it?

`Maven-Proxy` is *not* a repository manager, similar to Nexus and Artifactory.  `Maven-Proxy` is simple a caching proxy for any Maven Proxy.

## But why?

Maven and Gradle already maintain their own caches, and for any other use we could just use one of the popular repository managers, couldn't we?

If your only goal is to build projects, `Maven-Proxy` is not for you.  But, there are various scenarios where this can be handy, for example:

* You want to build Docker containers and have a Maven cache for your Maven/Gradle builds
  * Docker builds do not allow you to mount a volume, unlike running a Docker container.  This means that your .m2 and .gradle directories are completely unavailable to a Docker build.  Instead of running a full blown repository manager, you can run `Maven-Proxy` and point project to it.
* TODO (Docker is my use case, I'm sure there are others)

## Building from Source

```
./gradlew shadowjar
```

## Running `Maven-Proxy`

`Maven-Proxy` can take in a configuration JSON file (see `examples/maven-central.json`), or you can run it without any default parameters and it will configure itself as such:

* Listen on port 8002
* Maven Central is available at /maven-central
* JCenter is available at /jcenter
* The internal cache will be stored at ~/.maven-proxy
