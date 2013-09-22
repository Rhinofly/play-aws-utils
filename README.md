*Job opening: Scala programmer at Rhinofly*
-------------------------------------------
Each new project we start is being developed in Scala. Therefore, we are in need of a [Scala programmer](http://rhinofly.nl/vacature-scala.html) who loves to write beautiful code. No more legacy projects or maintenance of old systems of which the original programmer is already six feet under. What we need is new, fresh code for awesome projects.

Are you the Scala programmer we are looking for? Take a look at the [job description](http://rhinofly.nl/vacature-scala.html) (in Dutch) and give the Scala puzzle a try! Send us your solution and you will be invited for a job interview.
* * *

Amazon Web Services utils
=========================

This project contains utilities that are used in multiple Amazon services.

The Aws object
--------------

This object is mainly used to wrap Play's `WS` component and add signing to it. Usage is like this:

``` scala
Aws
  .withSigner(...)
  .url(...)
 	
  ...
 	
Aws
  .withSigner3(...)
  .url(...)
```

The Signers
-----------

The current version supplies Aws version 3 and Aws version 4 signers.

AwsCredentials
--------------

The `AwsCredentials` object contains `apply` and `unapply` methods. Some usage examples:

``` scala
val c = AwsCredentials("key", "secret")

val AwsCredentials(key, secret, tokenOption, expirationOption) = c

```

The `AwsCredentials` object allows you to create an instance using information in the configuration:

``` scala
# In the configuration
aws.accessKeyId=testKey
aws.secretKey=testSecret

//In Scala
val c = AwsCredentials.fromConfiguration
```

The `AwsCredentials` object also contains an implicit configuration (internally uses the `fromConfiguration` object.

``` scala

def myMethod(arg1:Int)(implicit credentials:AwsCredentials) = {
  //...
}
```

Installation
------------

``` scala
  val appDependencies = Seq(
    "nl.rhinofly" %% "play-aws-utils" % "2.3.2"
  )
  
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "Rhinofly Internal Release Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"
  )
```
