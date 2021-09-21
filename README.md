# Koppeltaal PoC EPD
The Koppeltaal PoC implementation of a Portal

## Running locally
```shell script
mvn spring-boot:run -Dserver.port=8080
```

## Downloading shared libraries

Koppeltaal 2.0 uses shared libraries as certain functionality (e.g. JWKS or SMART Backend Services)
are used in many components. These shared libraries are published
to [GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry).
In order to download these, you'll need a GitHub
[Personal Access Token](https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token)
with at least the  `read:packages` scope.

After you have this token, you must add GitHub as a Maven `server` to your `~/.m2/settings.xml`.

The `<server>` tag should be added like this, replace the username and password:

```xml

<server>
  <id>github</id>
  <username>{{YOUR_GITHUB_USERNAME}}</username>
  <password>{{YOUR_GITHUB_PERSONAL_ACCESS_TOKEN}}</password>
</server>
```