//
// This script is executed by Grails when the plugin is uninstalled from project.
// Use this script if you intend to do any additional clean-up on uninstall, but
// beware of messing up SVN directories!
//
Ant.delete(file:"${basedir}/grails-app/controllers/AppEngineGormTestController.groovy")
Ant.delete(dir:"${basedir}/grails-app/domain/com") 