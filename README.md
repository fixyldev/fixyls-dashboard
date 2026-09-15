# Fixyl's Dashboard

## Building

```sh
docker buildx build -t fixyldev/fixyls-dashboard:latest .
```

## Run

```sh
docker run -d -v /:/host:ro -p 8080:8080 fixyldev/fixyls-dashboard
```

or

```sh
docker run -d -v /:/host:ro --net=host fixyldev/fixyls-dashboard
```

if you want the container to be able to read the hostname of your host machine.

**Important:** This app will not work with Docker Desktop because it uses virtualization, even on a Linux host.
