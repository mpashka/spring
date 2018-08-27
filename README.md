This project is intended to provide spring configuraion that can be dynamically
reloaded

Inspired by [wuenschenswert.net](http://www.wuenschenswert.net/wunschdenken/archives/127)

#### Maven dependency
```xml
<dependency>
    <groupId>com.github.mpashka.spring</groupId>
    <artifactId>configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Spring context modification
In order to use this one several beans must be configured in spring context:
* <b>PropertySourcesPlaceholderConfigurer</b> - core spring class to configure spring placeholder
* <b>EnvironmentConfigurationSource</b> - thing to watch and reload configuration files
* <b>ReloadableConfigurationSupport</b> - traverse spring configuration and find all properties 
that need to be reloaded
* (Optionally) <b>ConfigurationUpdatedEvent</b> listener to listen configuration files updates

See [package-info](https://github.com/mpashka/spring/blob/master/configuration/src/main/java/com/github/mpashka/spring/config/package-info.java) 
for more details.

See [examples](https://github.com/mpashka/spring/tree/master/examples).
