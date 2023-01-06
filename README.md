# Kinetica 3D Service

[![Kinetica Nexus (Releases)](https://img.shields.io/nexus/r/com.kinetica.3d/gltf-service?label=Kinetica%20Nexus&server=https%3A%2F%2Fnexus.kinetica.com&style=flat)](https://nexus.kinetica.com/#browse/browse:releases:com%2Fkinetica%2F3d%2Fgltf-service)
[![Kinetica Nexus (Snapshots)](https://img.shields.io/badge/dynamic/xml?color=orange&label=Kinetica%20Nexus&query=%2F%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Blast()%5D&url=https%3A%2F%2Fnexus.kinetica.com%2Frepository%2Fsnapshots%2Fcom%2Fkinetica%2F3d%2Fgltf-service%2Fmaven-metadata.xml&style=flat)](https://nexus.kinetica.com/#browse/browse:snapshots:com%2Fkinetica%2F3d%2Fgltf-service)
[![Deploy Snapshot to Kinetica](https://github.com/kineticadb/kinetica-3d-service/actions/workflows/maven-deploy-snapshot.yml/badge.svg)](https://github.com/kineticadb/kinetica-3d-service/actions/workflows/maven-deploy-snapshot.yml)

The Kinetica 3D Service is a REST service based on [Spring Boot][SPRING_BOOT] that generates 3D models in [glTF][GLTF_SPEC] format from feature data in Kinetica. The types of models supported include:

* Terrain/Layers
* Pipes/Wellbores
* Sphere Events/MSI

[GLTF_SPEC]: <https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html>
[SPRING_BOOT]: <https://docs.spring.io/spring-boot/docs/current/reference/html/>

## Table of Contents

- [Table of Contents](#table-of-contents)
- [REST API](#rest-api)
- [Configuration](#configuration)
    - [Deployment](#deployment)
    - [SSL Setup](#ssl-setup)
- [Administration](#administration)
    - [Monitoring](#monitoring)
- [Testing](#testing)
    - [JUnit testing](#junit-testing)
    - [Browser based testing](#browser-based-testing)
- [Feature Table Reference](#feature-table-reference)
    - [gvis\_sphere\_events](#gvis_sphere_events)
    - [gvis\_pipe](#gvis_pipe)
    - [gvis\_terrain](#gvis_terrain)
- [References](#references)

## REST API

A detailed description of the rest API's is available in [postman][SERVICE_API]. HTTP Get calls for each of the 3 feature tables.

| API | table | description |
| :--- | :--- | :--- |
| `getSphereEvents` | `gvis_sphere_events` | Generate a set of spheres representing sub-terrain data |
| `getPipe` | `gvis_pipe` | Generate a pipe representing a wellbore |
| `getTerrain` | `gvis_terrain` | Generate a terrain from a grid of elevations |

The `getSphereEvents` API makes use of the following glTF extensions.

* [EXT_instance_features](https://github.com/CesiumGS/glTF/tree/3d-tiles-next/extensions/2.0/Vendor/EXT_instance_features)
* [EXT_structural_metadata](https://github.com/CesiumGS/glTF/tree/proposal-EXT_structural_metadata/extensions/2.0/Vendor/EXT_structural_metadata)
* [EXT_mesh_gpu_instancing](https://github.com/KhronosGroup/glTF/tree/main/extensions/2.0/Vendor/EXT_mesh_gpu_instancing)

[SERVICE_API]: <https://documenter.getpostman.com/view/4489533/VUjTihRa>

## Configuration

### Deployment

The project distribution releases are available on the [Kinetica Nexus][KINETICA_NEXUS]. You can browse for the latest version number. Assuming your version is `1.3.2` you can download and extract it as shown.

```sh
$ curl -O https://nexus.kinetica.com/repository/releases/com/kinetica/3d/gltf-service/1.3.2/gltf-service-1.3.2-dist.zip
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 26.8M  100 26.8M    0     0  15.1M      0  0:00:01  0:00:01 --:--:-- 15.2M

$ unzip gltf-service-1.3.2-dist.zip
Archive:  gltf-service-1.3.2-dist.zip
   creating: gltf-service-1.3.2/
  inflating: gltf-service-1.3.2/application.properties
  inflating: gltf-service-1.3.2/logback-spring.xml
  inflating: gltf-service-1.3.2/application-prod.properties
  inflating: gltf-service-1.3.2/gltf-service-1.3.2.jar
```

The following files should be deployed into the application directory:

| File | Description |
| :--- | :--- |
| `/application.properties` | Base configuration for all profiles including testing. |
| `/application-prod.properties` | Configuration for prod profile. |
| `/logback-spring.xml` | Enable logging of specific classes. |
| `/gltf-service-1.3.2.jar` | Self contained executable jar. |

This service is based on [Spring Boot][SPRING_BOOT]. Kinetica connection info must be made in `application.properties-prod`. You must set `spring.profiles.active=prod` so the the prod configuration will be used. You can update the default port which is set to `8120`.

### SSL Setup

To configure inbound SSL you will first need to obtain a key/certificate pair. Assuming that you have your PEM formatted certificate as `kinetica.pem` and key as `kinetica_key.pem` you can verify that their public keys match.

[KINETICA_NEXUS]: <https://nexus.kinetica.com/#browse/browse:releases:com%2Fkinetica%2F3d%2Fgltf-service>

```sh
$ openssl x509 -noout -modulus -in kinetica.pem | openssl md5
(stdin)= e19e78a3eb16485e8af04ba9d15bc37e

$  openssl rsa -noout -modulus -in kinetica_key.pem | openssl md5
(stdin)= e19e78a3eb16485e8af04ba9d15bc37e
```

Next you will need to create the PKCS12 keystore containing the cert/key pair.

```sh
openssl pkcs12 -export \
    -name kinetica \
    -passout pass:kinetica \
    -in kinetica.pem \
    -inkey kinetica_key.pem \
    -out kinetica.p12
```

Finally you will need to add the below lines to the application-prod.properties with the location of the keystore.

```properties
server.ssl.enabled=true
server.ssl.key-store=/gvis/certs/kinetica.p12
server.ssl.key-store-password=kinetica
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=kinetica
```

See also:

* [Spring Boot SSL Configuration](https://docs.spring.io/spring-cloud-skipper/docs/1.0.0.BUILD-SNAPSHOT/reference/html/configuration-security-enabling-https.html)
* [OpenSSL PKCS12](https://www.openssl.org/docs/manmaster/man1/openssl-pkcs12.html)
* [OpenSSL X509](https://www.openssl.org/docs/manmaster/man1/openssl-x509.html)
* [OpenSSL Quick reference](https://www.digicert.com/kb/ssl-support/openssl-quick-reference-guide.htm)

## Administration

Start the server:

```sh
[~/gvis-service]$ nohup ./gltf-service-1.1.jar &
```

Stop the server:

```sh
[~/gvis-service]$ kill $(<~/gvis/application.pid)
```

At startup the following files and directories will be created.

| File | Description |
| :--- | :--- |
| `${deploy}/application.pid` | Contains the process ID of the running server |
| `${deploy}/logs/gserv*.log` | Service application log |
| `${deploy}/work/` | contains Tomcat runtime files |
| `${deploy}/gltf_cache/` | contains generated raster images. |

### Monitoring

This service uses [Spring Boot Actuator][ACTUATOR] to generate metrics that can retrieved from endpoints. Some example URL's are shown below.

| URL | Description |
| :--- | :--- |
| http://localhost:8120/actuator/info | Git build information |
| http://localhost:8120/actuator/metrics | All available metrics |
| http://localhost:8120/actuator/metrics/get.pipe | Timer for `/getPipe` |

[ACTUATOR]: (https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## Testing

### JUnit testing

A JUnit testing framework is implemented using [Spring MVC Test Framework][MOCK_MVC]. These JUnit tests
can be run directly from eclipse. You will need to set connection information in `/src/test/resources/application-test.properties`.

[MOCK_MVC]: <https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#spring-mvc-test-framework>

### Browser based testing

You can test the service with a web browser by copying a test URL. The response will be a gltf file you can save to disk and open in an [online viewer][GLTF_VIEWER].

[GLTF_VIEWER]: <https://gltf-viewer.donmccurdy.com/>

## Feature Table Reference

The tables are kept in the Kinetica `3DBASIN` schema by default. This schema can be configured in the `application.properties` file. Table create scripts are available under the '/ddl' directory.

### gvis_sphere_events

Each record in this table indicates a event that will be rendered as a sphere with a variable location, size, and color.

| Column | Type | Description |
| :--- | :--- | :--- |
| `project_id` | `VARCHAR` | Project ID grouping the events. |
| `event_id` | `VARCHAR` | **Primary Key.** Unique ID indicating the event. |
| `event_ts` | `TIMESTAMP` | Time of the event. |
| `x` | `REAL` | Lon coordinate in WGS. |
| `y` | `REAL` | Lat coordinate in WGS. |
| `z` | `REAL` | Depth in feet. |
| `amp` | `REAL` | Relative size of the event. |
| `color_id` | `INTEGER` | ID indicating the color of the event. |

### gvis_pipe

Each record in this table identifies a segment of the pipe. Records will be ordered by measured depth before rendering.

| Column | Type | Description |
| :--- | :--- | :--- |
| `project_id` | `VARCHAR` | Project ID grouping the pipes. |
| `name` | `VARCHAR` | Name of the pipe/wellbore. |
| `stage` | `INTEGER` | Stage number used to color the segment |
| `md` | `REAL` | Measured depth in feet. |
| `x` | `REAL` | Lon coordinate in WGS. |
| `y` | `REAL` | Lat coordinate in WGS. |
| `z` | `REAL` | Depth in feet. |

### gvis_terrain

This table contains 2D grids used for rendering 3D terrains. Each layer represents a surface so that each surface is 
identified by (`project_id`, `layer`) combination.

| Column | Type | Description |
| :--- | :--- | :--- |
| `project_id` | `VARCHAR` | Project ID grouping the terrains. |
| `lod` | `INTEGER` | Level of detail starting at 0. |
| `scan_x` | `INTEGER` | X position of the grid. |
| `scan_y` | `INTEGER` | Y position of the grid. |
| `layer` | `INTEGER` | Layer of the grid. |
| `x` | `REAL` | Lon coordinate in WGS. |
| `y` | `REAL` | Lat coordinate in WGS. |
| `z` | `REAL` | Depth in feet. |

## References

* [gltf-mesh Library](https://github.com/chadj2/jgltf-mesh)
* [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
* [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
* [Spring MVC Documentation](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html)
* [MVC Test Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
