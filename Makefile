# check for build/shipkit and clone if not there, this should come first
SHIPKIT_DIR = build/shipkit
$(shell [ ! -e $(SHIPKIT_DIR) ] && git clone -b v2.0.12 https://github.com/yakworks/shipkit.git $(SHIPKIT_DIR) >/dev/null 2>&1)
# Shipkit.make first, which does all the lifting to create makefile.env for the BUILD_VARS
include $(SHIPKIT_DIR)/Shipkit.make
include $(SHIPKIT_MAKEFILES)/circle.make
include $(SHIPKIT_MAKEFILES)/vault.make
# spring common has the git and gradle targets
include $(SHIPKIT_MAKEFILES)/spring-common.make
include $(SHIPKIT_MAKEFILES)/ship-gh-pages.make

# DB = true # set this to true to turn on the DB environment options

## Run spotlessApply and normal check
check:
	$(gradlew) spotlessApply check

# should run vault.decrypt before this,
# sets up github, kubernetes and docker login
ship.authorize: git.config-bot-user
	$(logr.done)

# publish the java jar lib to repo.9ci for snapshot and to both for prod Sonatype Maven Central
publish:
	if [ "$(dry_run)" ]; then
		echo "🌮 dry_run ->  $(gradlew) publish"
	else
		if [ "$(IS_SNAPSHOT)" ]; then
			$(logr) "publishing SNAPSHOT $(VERSION)"
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
		else
			$(logr) "publishing to repo.9ci $(VERSION)"
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
			$(logr) "publishing to Sonatype Maven Central $(VERSION)"
			$(gradlew) publishToSonatype closeAndReleaseSonatypeStagingRepository
		fi
		$(logr.done) "published"
	fi

## publish snapsot to repo.9ci
publish.snapshot:
	if [ "$(IS_SNAPSHOT)" ]; then
		$(gradlew) publishJavaLibraryPublicationToMavenRepository
		$(logr.done) "- libs with version $(VERSION)$(VERSION_SUFFIX) published to snapshot repo"
	fi

## alias to publish.snapshot
snapshot.publish: publish.snapshot

## Build snapshot and publishes to your local maven.
snapshot:
	$(gradlew) snapshot
	$(logr.done) "- libs with version $(VERSION)$(VERSION_SUFFIX) published to local ~/.m2 maven"

ifdef PUBLISHABLE_BRANCH_OR_DRY_RUN

# removed  ship.docker kube.deploy for now
 ship.release: build publish
	$(logr.done)

else

 ship.release:
	$(logr.done) "not on a RELEASABLE_BRANCH, nothing to do"

endif # end RELEASABLE_BRANCH


# ---- Docmark -------

# the "dockmark-build" target depends on this. depend on the docmark-copy-readme to move readme to index
docmark.build-prep: docmark.copy-readme

# --- Testing and misc, here below is for testing and debugging ----

PORT ?= 8080

# -- helpers --

# shows grails-kit:dependencies --configuration compileClasspath
gradle.dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath runtimeClasspath
	# ./gradlew rally-api:dependencies --configuration compileClasspath
	./gradlew grails-kit:dependencies --configuration compileClasspath

