# Development Tools Project
This project will contain various development tools and files such as checkstyle
rules, an editorconfig file, etc.

It will be used by the build system to enforce code style and code quality.

**Table of Contents:**

* [IntelliJ Code Style Settings](#intellij-code-style-settings)
* [Java Checkstyle](#java-checkstyle)
    * [IntelliJ Integration](#intellij-integration)
    * [Build System Integration](#build-system-integration)
* [EditorConfig](#editorconfig)

## IntelliJ Code Style Settings

To make IntelliJ reformat your code according to Ocopea's code style rules, you can import
our code style settings for IntelliJ.

**NOTE: Using these setting fix more than 60% of the errors we found in existing projects.**

To do that:

1. In IntelliJ, click File --> Settings --> Editor --> Code Style --> Java.
2. Click Manage --> Import and select "IntelliJ IDEA code style XML".
3. Select the file `intellij-code-style/Ocopea.xml` from this repository.
4. Select the "Ocopea" scheme in IntelliJ's Code Style settings.

## Java CheckStyle
Our Checkstyle rules are derived from
[Google's Java style guide](https://google.github.io/styleguide/javaguide.html).

Generally speaking, you should be fine if you follow the following rules:

* Indent using 4 spaces. Never use tabs.
* Lines are no longer than 120 characters.
* Use spaces around operators and curly braces.
* Public methods should have JavaDoc. If a JavaDoc is truly redundant you can use `@NoJavadoc`.
* Separate methods and classes using a single blank line
* Longs line are broken using wrap all, indent by one (or indent by two, for method declaration arguments), as demonstrated and explained [here](http://www.draconianoverlord.com/2016/09/16/one-true-way-of-indenting.html). This style is refered to as "Chop Down" by IntelliJ.
* When in doubt about where to wrap lines, follow Google's instruction: "The prime directive of line-wrapping is: prefer to break at a **higher syntactic level**."

However, the above list in not a complete description of our style guide.
### IntelliJ Integration
IntelliJ has a Checkstyle plugin that can use our checkstyle definitions to
highlight styling errors. To set up this:

1. Install the Intellij Checkstyle plugin: Go to Settings --> Plugins -->
Browse Repositories and install the "CheckStyle-IDEA" plugin.
2. Go to Settings --> Other Settings --> Checkstyle and under "Configuration
Files" hit the + button and select the file
`checkstyle/src/main/resources/checkstyle.xml` from this repository.
If you don't want to clone this repository you can also point to its address.
In the description field you can put "Ocopea Checkstyle".
3. In the same window, make sure the configuration you just added is marked
as "Active".
4. Go to Settings --> Editor --> Inspections, select the "Checkstyle real-time
scan" inspection and change its Severity level to "Error".

You can now use the new Checkstyle tab in Intellij to check the current file,
entire project, etc.

### Build System Integration
This project creates a JAR that packages our coding style definitions.
You can depend on this JAR and then point `maven-checkstyle-plugin` to this
file by adding the following to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>2.10</version>
            <executions>
                <execution>
                    <goals>
                        <goal>unpack</goal>
                    </goals>
                    <phase>validate</phase>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>com.emc.ocopea.devtools</groupId>
                                <artifactId>checkstyle</artifactId>
                                <version>${ocopea.dev-tools.version}</version>
                            </artifactItem>
                        </artifactItems>
                        <outputDirectory>${project.build.directory}/checkstyle</outputDirectory>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>2.17</version>
            <dependencies>
                <dependency>
                    <groupId>com.puppycrawl.tools</groupId>
                    <artifactId>checkstyle</artifactId>
                    <version>7.4</version>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <id>validate</id>
                    <phase>validate</phase>
                    <configuration>
                        <configLocation>${project.build.directory}/checkstyle/checkstyle.xml</configLocation>
                        <encoding>UTF-8</encoding>
                        <consoleOutput>true</consoleOutput>
                        <failsOnError>true</failsOnError>
                        <failOnViolation>true</failOnViolation>
                        <linkXRef>false</linkXRef>
                    </configuration>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```


## EditorConfig

[EditorConfig](http://editorconfig.org/) helps developers define and maintain
consistent coding styles between different editors and IDEs. Some IDEs and text
editors have builtin support for it (Intellij) and others have plugins you can
download and install (Vim, Sublime Text, Emacs, Eclipse).

To use Ocopea's EditorConfig settings:

1. Download and install the appropriate EditorConfig plugin for your IDE, or
2. In IntelliJ, go to Settings --> Editor --> Code Style and make sure "Enable
EditorConfig support" is checked.
3. Symlink the file `editorconfig` to your home directory. Notice the dot.  
```
ln -s <dev-tools repo>/editorconfig ~/.editorconfig
```
