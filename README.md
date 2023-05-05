# AEM SDK Development Tools

## Asset Workflow Hook

The asset workflow hook can be used to add to a content-package that contains test content to be installed to your AEM SDK
instance. Installing assets through a content-package will trigger a lot of workflows related to rendition creation etc.
This pollutes the logs and makes it less clear if anything goes wrong. The content hook will disable the asset workflows
before installing the content and re-enable them after the content has been installed.

### Installation

When using this for the AEM SDK and in an aem archetype, add the following configuration to your maven pom.xml:

```xml

<profile>
    <id>installHook</id>
    <activation>
        <property>
            <name>!env.CM_BUILD</name>
        </property>
    </activation>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <executions>
                    <execution>
                        <id>copy</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>copy</goal>
                        </goals>
                        <configuration>
                            <artifactItems>
                                <artifactItem>
                                    <groupId>be.orbinson.aem</groupId>
                                    <artifactId>be.orbinson.aem.sdk-dev-tools.asset-workflow-hook</artifactId>
                                    <version>1.0.0-SNAPSHOT</version>
                                    <overWrite>true</overWrite>
                                    <outputDirectory>
                                        ${project.build.directory}/vault-work/META-INF/vault/hooks
                                    </outputDirectory>
                                </artifactItem>
                            </artifactItems>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>

```

### Optional settings

By default, the workflow hook will work for the AEM SDK. If you would like to use this for an AEM 6.5, you can set the `aemVersion` vault package property to 6.5