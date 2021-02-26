# Spring Boot environment config property resolution

This example demonstrates a possible issue with the Spring Boot translation of
environment variables into property values. Loading a configuration value from 
an environment variable into a `@ConfigurationProperties` can result in having a
different value compared to what one would receive when using `@Value("${}")` 
syntax. See the [ConfigTest](src/test/kotlin/io/github/sandornemeth/spring/ConfigTest.kt)
for the actual test to reproduce the issue.

The issue is reproduced on the latest Spring Core version (5.3.4) shipping with
Spring Boot 2.4.3.


# Credits

This issue was originally during work done at 
[Trade Republic](https://traderepublic.com).