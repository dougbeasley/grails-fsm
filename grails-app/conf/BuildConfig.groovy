coverage {
    exclusions = [
                 'grails/fixture/**',
                 'org/grails/**',
                 '**/BuildConfig*',
                 ]
}

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn" // log level of Ivy resolver, either 'error',
               // 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
    }

    plugins {   
        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }
        build ":tomcat:$grailsVersion"
        compile ":hibernate:$grailsVersion"
    }

    dependencies {
    }
}

