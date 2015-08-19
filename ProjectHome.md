Regroup all commons/general classes and utilities shared by mine projects.

This version 0.2.3 just publish few classes used by [JBusyComponent](http://code.google.com/p/jbusycomponent/) but should grow in the future.

## Maven project integration ##
  * Add in your **pom.xml** a dependency to this library:
```
<dependency>
    <groupId>org.divxdede</groupId>
    <artifactId>commons</artifactId>
    <version>0.2.3</version>
</dependency> 
```

You can refer to the [OSS Nexus Repository](https://oss.sonatype.org/content/groups/staging/org/divxdede/commons/) for refer to a SNAPSHOT release like it:
```
    <repositories>
        <repository>
            <id>sonatype.oss.snapshots</id>
            <name>Sonatype OSS Snapshot Repository</name>
            <url>http://oss.sonatype.org/content/repositories/snapshots</url>
            <releases>
              <enabled>false</enabled>
            </releases>
            <snapshots>
              <enabled>true</enabled>
            </snapshots>
        </repository> 
    </repositories>
```