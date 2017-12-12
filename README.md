# jpa-apt

If you need anyhow the JPA entity configuration, jpa-apt reads it for you.

It is an Annotation Processor for the APT build step
so the JPA configuration gets read at build time and there's no costly annotation reading with reflection at application start up.


## Setup

Gradle:
```
// so that generated meta model is visible from code
sourceSets {
    main.java.srcDirs += 'build/generated/'
}

dependencies {
  compile 'net.dankito.jpa.annotationreader:jpa-apt:1.0-alpha'
  kapt 'net.dankito.jpa.annotationreader:jpa-apt:1.0-alpha'
}
```

Maven:
```
<dependency>
   <groupId>net.dankito.jpa.annotationreader</groupId>
   <artifactId>jpa-apt</artifactId>
   <version>1.0-alpha</version>
</dependency>
```


## Usage

```kotlin
private fun loadGeneratedModel(): JPAEntityConfiguration {
        val generatedConfigs = net.dankito.jpa.apt.generated.GeneratedEntityConfigs()

        val generatedEntityConfigs = generatedConfigs.getGeneratedEntityConfigs()
    }
```


# License

    Copyright 2017 dankito

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.