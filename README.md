# sbt-multi-project-example 

The goal of this example is to provide a multi-project build using `sbt` providing:
* A single `build.sbt` file which allows for centralized configuration, dependency and build management
* Each sub-project contains only its source code
* Sub-projects can depend on other sub-projects
* Only *deliverable* sub-projects produce a *fat-jar* using [sbt-assembly](https://github.com/sbt/sbt-assembly)

# Examples 

* build of both projects:  web_serv_api_core>sbt clean compile assembly
* build only api        :  web_serv_api_core>sbt api/clean api/compile api/assembly 
* build only core       :  web_serv_api_core>sbt core/clean core/compile core/assembly

# Example structure
* sbt-multi-project-example/
    * api/
        * src/
          * main/
            * scala/
          * test/
    * core/
        * src/
          * main/
            * scala/
          * test/
    * project/
        * build.properties
        * plugins.sbt
    * build.sbt
