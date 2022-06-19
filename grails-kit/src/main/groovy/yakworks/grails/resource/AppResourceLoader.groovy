/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails.resource

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

import grails.config.Config
import grails.core.GrailsApplication
import grails.core.support.GrailsConfigurationAware
import yakworks.commons.lang.Validate

/**
 * A place for file resource related functionality which may required an application context or logged-in user.
 * Things related to the attachments directory or the tempUploadsDirectory or any other directory
 * we reference from Config.groovy would go here, especially if they require GString-like parsing.
 *
 * See Attachments and application.groovy for more description of how this works
 *
 */
@Slf4j
@CompileStatic
class AppResourceLoader implements ResourceLoader, GrailsConfigurationAware {
    @Autowired
    GrailsApplication grailsApplication

    ResourceLoader resourceLoader

    Config cfg

    /**
     * The path to resources config root. eg "nine.resources"
     */
    String resourcesConfigRootKey = "app.resources"

    private Path rootPath

    @PostConstruct
    void init() {
        resourceLoader = grailsApplication.mainContext
    }

    /**
     * if location starts with a / then it realtive to the war (web-app).
     * For example, '/WEB-INF/invoice/greenbar.ftl
     *
     * if it does not start with a / then its considered relative to the rootLocation
     * For example, '2010-11/23452.pdf' would look for file://myroot/2010-11/23452.pdf if that what was set in
     * nine.attachments.directory
     */
    Resource getResource(String location) {
        String urlToUse = location
        if ((!location.startsWith('/')) && !(location.startsWith('file:'))) {
            urlToUse = "file:${rootPath.resolve(location)}/"
        }
        log.debug "appResourceLoader.getResource with $urlToUse"
        resourceLoader.getResource(urlToUse)
    }

    /**
     * gets a resource with a locationBase and a relative location
     *   - if relative location starts with "/" just pass it to getResource() and ignore locationBase (will look in war (web-app in dev))
     *   - if relative location starts with URL like classpath:,file:, http: it be passed directly to resourceLoader
     *   - if locationBase starts with config: then it will use whatever key comes after
     *     to find the location (ex- config:reports.location)
     *
     * @param locationBase if null uses the default key of 'attachments.location'
     * @param location the relative location to use
     */
    Resource getResource(String configKey, String location) {
        Path path = getPath(configKey)
        Path locationPath = path.resolve(location)
        return resourceLoader.getResource("file:${locationPath}/")
    }

    /**
     * just converts path toString and calls getResource, which will resolve against the rootPath
     */
    Resource getResource(Path pathLoc) {
        return getResource(pathLoc.toString())
    }

    ClassLoader getClassLoader() {
        resourceLoader.getClassLoader()
    }

    /**
     * Creates a temp file inside the tempDir and, optionally, populates it with data.
     * The file is guaranteed to not have a colliding name.
     * If the originalFileName has a dot in the name, then the final file short name will be
     * <whateverIsBeforeTheLastDot><someRandomCharacters>.<whateverIsAfterTheLastDot>
     * For example, "readme.txt" would turn out something like "readme12a35c23.txt"
     *
     * @param originalFileName the name of the file that was uploaded
     * @param data is the file contents, and can be String, byte[], or null.
     * @return a non-null File instance, which has a unique name within the tempDir, and
     *         if data is non-null will exist and will contain the data specified.
     */
    Path createTempFile(String originalFileName, Object data) {
        String baseName = PathUtils.getBaseName(originalFileName)
        if (baseName.length() < 3) baseName = baseName + "tmp"
        String extension = PathUtils.getExtension(originalFileName)
        extension = extension ? ".${extension}" : ''

        Path tmpDir = getTempDirectory()
        Path tmpFilePath = Files.createTempFile(tmpDir, baseName, extension)

        if (data) {
            if (data instanceof String) {
                tmpFilePath.write(data)
                // FileUtils.writeStringToFile(tmpFile, data)
            } else if (data instanceof byte[]) {
                tmpFilePath.setBytes(data)
                // FileUtils.writeByteArrayToFile(tmpFile, data)
            } else if (data instanceof ByteArrayOutputStream) {
                tmpFilePath.withOutputStream {
                    (data as ByteArrayOutputStream).writeTo(it)
                }
            }
        }
        return tmpFilePath
    }

    static void deleteIfExists(Path ...paths){
        for (Path path: paths) {
            Files.deleteIfExists(path)
        }
    }

    /**
     * gets the tempDir specified in app.resources.tempDir.
     * Will create it if it does not exist
     */
    Path getTempDirectory() {
        Path tempPath
        String _tempDir = getProp("tempDir")
        // Path tempPath
        if(_tempDir){
            tempPath = Paths.get(_tempDir)
            //if its not absolute then make it relative to the rootPath
            if(!tempPath.isAbsolute()) tempPath = rootPath.resolve(_tempDir)
            if(Files.notExists(tempPath)) Files.createDirectories(tempPath)
        } else {
            //will create it in java.io.tmpdir
            tempPath = Files.createTempDirectory("appResources")
        }
        return tempPath
    }

    /**
     * gets the root dir Path
     */
    Path getRootPath() {
        return this.rootPath
    }

    /**
     * Get a configured directory Path from a app.resources key.
     * If the name is relative then it is resolved against the rootPath
     * If the name from config is absolute (starts with a /) then it builds based on just that name.
     *
     * @param key The short name of the key. Will get the resourcesConfigRootKey prefix added.
     *        for example: `attachments.location` will use the key app.resources.attachments.location
     * @param create True if the directory should be created if missing.
     * @return the Path pointed to the dir for the key
     */
    Path getPath(String key, boolean create = false) {
        String dir = getProp(key)
        if (!dir) throw new IllegalArgumentException("app resource key '${key}' is not defined or returns an empty value.")
        return checkPath(Paths.get(dir), create)
    }

    /**
     * makes a path from the passed in directory if its not absolute
     * if the dir passed in is absolute then will use that, otherwise just returns it.
     */
    Path checkPath(Path directory, boolean create = false) {
        Path dirPath = directory
        if(!dirPath.isAbsolute()) dirPath = rootPath.resolve(dirPath)
        if(create) PathUtils.createDirectories(dirPath)
        return dirPath
    }

    /**
     * makes a path from the passed in directory
     * if the dir passed in is absolute then will use that, otherwise just returns it.
     */
    Path checkPath(String directory) {
        return checkPath(Path.of(directory))
    }

    /**
     * recursively deletes the dir for the short key. Used mostly for testing cleanup
     */
    boolean deleteDirectory(String key) {
        PathUtils.deleteDirectory(getPath(key))
    }

    /** Get a list of script locations as absolute files. */
    Path getScripts() {
        String scriptsDir = getProp('scripts.locations', 'scripts')
        return checkPath(Path.of(scriptsDir))
    }

    /**
     * returns the relative path of the file to the dir for the config path locationKey.
     * so if locationKey='attachments.location' and appResourceLoader.getPath returns '/foo/attachments'
     * and the file is '/foo/attachments/attachments/2020-12/foo123.jpg' this will return '2020-12/foo123.jpg'
     */
    Path getRelativePath(Path file, String locationKey) {
        Path rootPath = getPath(locationKey)
        Path relativePath = rootPath.relativize(file)
        return relativePath
    }

    /**
     * prefixes resourcesConfigRootKey to subkey and gets the config value
     * @param subKey the suffix.
     * @return passing in attachments.location will return value for app.resources.attachments.location
     */
    String getProp(String subKey, String defaultValue = null) {
        getConfigProperty(subKey, String, defaultValue)
    }

    /** prepends resourcesConfigRootKey to subKey */
    String buildResourceKey(String subKey) {
        Validate.notEmpty(subKey)
        return resourcesConfigRootKey + "." + subKey
    }

    /** prepends resourcesConfigRootKey to subKey */
    public <T> T getConfigProperty(String subKey, Class<T> targetType, T defaultValue = null){
        String fullKey = buildResourceKey(subKey)
        return defaultValue ? cfg.getProperty(fullKey, targetType, defaultValue) : cfg.getProperty(fullKey, targetType)
    }


    @Override
    void setConfiguration(Config co) {
        cfg = co
        String rootLoc = co.getProperty(buildResourceKey('rootLocation'), String)
        if(rootLoc){
            rootPath = Paths.get(rootLoc)
            if(Files.notExists(rootPath)) Files.createDirectories(rootPath)
        }
    }
}
