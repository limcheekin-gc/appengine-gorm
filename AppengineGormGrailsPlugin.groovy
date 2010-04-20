class AppengineGormGrailsPlugin {
    // the plugin version
    def version = "0.1.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.1.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [appEngine: "0.8.5 > *",
                     gormJpa: "0.5 > *"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "lib/*"  
    ]


    def author = "Lim Chee Kin"
    def authorEmail = "limcheekin@vobject.com"
    def title = "Batch insert, update and delete operation support in Google App Engine"
    def description = '''\\
    The App Engine GORM plugin work with App Engine Plugin and GORM JPA plugin 
    to allow Grails application running in Google App Engine(GAE) perform 
    batch insert, update and delete operation. Given request time limit
    (30 seconds approximately) from GAE, support of these batch operations is critical 
    for any serious Grails application running in GAE such as application that 
    required massive data input from complex form, import data by file upload and 
    bulk loading data to data store(BigTable). 
    '''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/GormAppengine+Plugin"

    def doWithSpring = {
	dataStoreService(com.vobject.appengine.gorm.DataStoreService) {}
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional)
    }

    def doWithDynamicMethods = { applicationContext ->
         
	 def dataStoreService = applicationContext.getBean("dataStoreService")

	 AbstractCollection.metaClass.batchSave = {
	     if (Collection.class.isAssignableFrom(delegate.class)) {
	         return dataStoreService.batchSave(delegate) 
	     } else {
	         throw new IllegalArgumentException("The argument must be type of Collection!")
	     }		
	 }

	 AbstractCollection.metaClass.batchDelete = {		
	     if (Collection.class.isAssignableFrom(delegate.class)) {
	         dataStoreService.batchDelete(delegate) 
	     } else {
	         throw new IllegalArgumentException("The argument must be type of Collection!")
	     }
	 }
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
