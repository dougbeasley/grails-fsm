package grails.fsm

import grails.plugins.*
import groovy.lang.MetaProperty;
import org.grails.spring.DefaultBeanConfiguration;

import org.codehaus.groovy.runtime.metaclass.ThreadManagedMetaBeanProperty;

import grails.plugin.fsm.FsmSupport
import grails.plugin.fsm.FsmUtils
import grails.plugin.fsm.FsmSupportException

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import grails.util.GrailsClassUtils
import org.grails.commons.metaclass.*

class GrailsFsmGrailsPlugin extends Plugin {
    private static final Log log = LogFactory.getLog(GrailsFsmGrailsPlugin.class);
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.1.4 > *"

    def loadAfter = ['controllers', 'hibernate', 'domainclass']

    // resources that are excluded from plugin packaging
    def pluginExcludes = [ "plugin/test/**" ]

    // TODO Fill in these fields
    def title = "Finite State Machine behaviour for domain classes"
    def author = "Jorge Uriarte"
    def authorEmail = "jorge.uriarte@omelas.net"
    def description = '''\\
        This plugin allow definition of simple workflows attached to domain classes, including
        states, events, transitions and conditions.
        Current workflow's state will be held in domain class' property that must be defined.
        Multiple workflows can be defined on every domain class.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-fsm"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {

        // Will add the fire closure where needed
        grailsApplication.domainClasses.each {domainClass ->
            MetaClassRegistry registry = GroovySystem.metaClassRegistry
            def fsm = GrailsClassUtils.getStaticPropertyValue(domainClass.clazz, FsmUtils.FSMDEF )
            if (fsm) {
                // Will create the proper FsmSupport instance!
                fsm.each {String p, definition ->
                    definition.each { start, defclosure ->
                      def mp = domainClass.metaClass.getMetaProperty(p)
                        if (!mp)
                            throw new FsmSupportException("Error in FSM definition: '${domainClass.clazz}' does not have '${p}' property to hold defined workflow status!")
                      def tmp = domainClass.clazz.newInstance()
                      if (tmp[p] != null)
                        log.warn("Default value of '${domainClass.clazz}.${p}' will be overriden by FSM definition for that property. ")

                        // Modify the metaclass so new instances will have new behaviour!!
                        domainClass.metaClass.setProperty("_fsm${p}", null)  // internal, will hold FsmSupport instance
                        domainClass.metaClass.fire = FsmUtils.fireClosure
                        domainClass.metaClass."fire_${p}" = FsmUtils.fireClosure.curry(p)
                        domainClass.metaClass.fireable = FsmUtils.fireableClosure
                        domainClass.metaClass."fireable_${p}" = FsmUtils.fireableClosure.curry(p)
                    }
                }
                // This code is a COPY of DomainClassGrailsPlugin.enhanceDomainClasses
                // because I cannot seem to be able to decorate it.
                // We just added the "${p}" initializing!
                domainClass.metaClass.constructor = {->
                def bean
                  if(applicationContext.containsBean(domainClass.fullName)) {
                      bean = applicationContext.getBean(domainClass.fullName)
                  }
                  else {
                      bean = BeanUtils.instantiateClass(domainClass.clazz)
                  }
                fsm.each { pp, defdef ->
                  defdef.each { startstart, clos ->
    //	            			def setter = GrailsClassUtils.getSetterName(pp)
    //	            			bean."${setter}"(startstart)
                    bean."${pp}" = startstart
                  }
                }
                bean
              }
                domainClass.metaClass.static.create = {->
                  def bean = applicationContext.getBean(domainClass.getFullName())
                fsm.each { pp, defdef ->
                  defdef.each { startstart, clos ->
                    bean."${pp}" = startstart
                  }
                }
                  bean
                }
            }
        }
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange( Map<String, Object> event) {
    	event.manager?.getGrailsPlugin("fsm")?.doWithDynamicMethods(event.ctx)
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
