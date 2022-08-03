/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails.resource


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic

import org.apache.commons.io.file.PathUtils

/**
 * Utility methods for copying or deleting from the file system.
 */
@CompileStatic
abstract class PathSupport {

    /**
     * Deletes a directory recursively. does not throw error, returns false on IOException
     */
    static boolean deleteDirectory(Path root) {
        if (root == null) {
            return false
        }
        try {
            return PathUtils.deleteDirectory(root)
        }
        catch (IOException ex) {
            return false
        }
    }

    /**
     * Files.createDirectories to ensure its created, checks if exists first.
     */
    static Path createDirectories(Path dir){
        if(!Files.exists(dir)) return Files.createDirectories(dir)
        return dir
    }

    static String getBaseName(String filename) {
        return getBaseName(Paths.get(filename))
    }

    /**
     * get base filename without extension for path
     * 'foo'->'foo', 'foo.txt'->'foo', 'foo.tar.gz'->'foo.tar'
     * @return the base name. will be same if no ext exists.
     * @see #getBaseName
     */
    static String getBaseName(Path path) {
        return removeFileExtension(path.fileName.toString())
    }

    /**
     * Strips the
     * @param filename the filename to remove ext
     * @param removeAllExtensions if true then foo.tar.gz will return foo
     * @return the name without extenstion
     */
    static String removeFileExtension(String filename, boolean removeAllExtensions = false) {
        if (filename == null || filename.isEmpty()) return filename
        return filename.replaceAll(extensionPattern(removeAllExtensions), "")
    }

    /**
     * regex pattern to get extension from filename string ,
     * if wholeExtension is true then will match the .tar.gz in foo.tar.gz instead of just the .gz
     */
    static String extensionPattern(boolean wholeExtension = false) {
        return '(?<!^)[.]' + (wholeExtension ? '.*' : '[^.]*$')
    }

    /**
     * Gets extension from name
     * @return null if nothing found of the extension
     */
    static String getExtension(String fileName) {
        getExtension(Paths.get(fileName))
    }

    static String getExtension(Path path, boolean wholeExtension = false) {
        String fileName = path.fileName
        def match = (fileName =~ extensionPattern(wholeExtension))

        //fist char will be dot so just remove it
        return match.size() ? (match[0] as String).substring(1) : null
    }

    static String extractMimeType(String filename) {
        extractMimeType(Paths.get(filename))
    }
    /**
     * Gets mime type from file name
     */
    static String extractMimeType(Path path) {
        String fileName = path.fileName
        String mimeType = URLConnection.guessContentTypeFromName(fileName)
        if(!mimeType) {
            // see if its word or excel as they are the most common that are not mapped
            Map mimeMap = [
                doc: 'application/msword', docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                xls: 'application/vnd.ms-excel', xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            ]
            String exten = getExtension(fileName)
            if(mimeMap.containsKey(exten)) return mimeMap[exten]
        }
        return mimeType ?: 'application/octet-stream'
    }
}
