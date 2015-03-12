package kotlin.io

import java.io.*
import java.util.ArrayList

/**
 * Create an empty directory in the specified directory, using the given prefix and suffix to generate its name.
 * Prefix shouldn't be shorter than 3 symbols or IllegalArgumentException will be thrown.
 *
 * If prefix is not specified then some unspecified name will be used.
 * If suffix is not specified then ".tmp" will be used.
 * If directory is not specified then the default temporary-file directory will be used.
 *
 * Returns a file object corresponding to a newly-created directory.
 *
 * If some error occurs then IOException is thrown.
 */
public fun createTempDir(prefix: String = "tmp", suffix: String? = null, directory: File? = null): File {
    val dir = File.createTempFile(prefix, suffix, directory)
    dir.delete()
    if (dir.mkdir()) {
        return dir
    } else {
        throw IOException("Unable to create temporary directory")
    }
}

/**
 * Create a new empty file in the specified directory, using the given prefix and suffix to generate its name.
 * Prefix shouldn't be shorter than 3 symbols or IllegalArgumentException will be thrown.
 *
 * If prefix is not specified then some unspecified name will be used.
 * If suffix is not specified then ".tmp" will be used.
 * If directory is not specified then the default temporary-file directory will be used.
 *
 * Returns a file object corresponding to a newly-created file.
 *
 * If some error occurs then IOException is thrown.
 */
public fun createTempFile(prefix: String = "tmp", suffix: String? = null, directory: File? = null): File {
    return File.createTempFile(prefix, suffix, directory)
}

/**
 * Returns `this` if this file is a directory, or the parent if it is a file inside a directory
 */
public val File.directory: File
    get() = if (isDirectory()) this else getParentFile()!!

/**
 * Returns the canonical path of this file.
 */
public val File.canonicalPath: String
    get() = getCanonicalPath()

/**
 * Returns the file name or "" for an empty name
 */
public val File.name: String
    get() = getName()

/**
 * Returns the file path or "" for an empty name
 */
public val File.path: String
    get() = getPath()

/**
 * Returns the extension of this file (not including the dot), or an empty string if it doesn't have one.
 */
public val File.extension: String
    get() {
        return name.substringAfterLast('.', "")
    }

/**
 * Replaces all separators in the string used to separate directories with system ones and returns the resulting string.
 */
public fun String.separatorsToSystem(): String {
    val otherSep = if (File.separator == "/") "\\" else "/"
    return replace(otherSep, File.separator)
}

/**
 * Replaces all path separators in the string with system ones and returns the resulting string.
 */
public fun String.pathSeparatorsToSystem(): String {
    val otherSep = if (File.pathSeparator == ":") ";" else ":"
    return replace(otherSep, File.pathSeparator)
}

/**
 * Replaces path and directories separators with corresponding system ones and returns the resulting string.
 */
public fun String.allSeparatorsToSystem(): String {
    return separatorsToSystem().pathSeparatorsToSystem()
}

/**
 * Returns a pathname of this file with all path separators replaced with File.pathSeparator
 */
public fun File.separatorsToSystem(): String {
    return toString().separatorsToSystem()
}

/**
 * Returns file's name without an extension.
 */
public val File.nameWithoutExtension: String
    get() = name.substringBeforeLast(".")

/**
 * Returns true if the given [file] is in the same directory as this file or a descendant directory.
 */
public fun File.isDescendant(file: File): Boolean {
    return file.directory.canonicalPath.startsWith(directory.canonicalPath)
}

/**
 * Returns the relative path of the given descendant of this file if it is a descendant, or an empty string otherwise.
 * Note that the base file is treated as a directory.
 * If this file matches the base directory, then an empty string will be returned.
 *
 * Throws IllegalArgumentException if child and parent have different roots.
 */
public fun File.relativeTo(base: File): String {
    fun getDriveLetter(path: String): Char? {
        return if (path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            path.charAt(0)
        } else {
            null
        }
    }

    val thisCanonical = canonicalPath
    val baseCanonical = base.canonicalPath
    if (thisCanonical.equals(baseCanonical)) {
        return ""
    }
    if (getDriveLetter(thisCanonical) != getDriveLetter(baseCanonical)) {
        throw IllegalArgumentException("this and base files have different roots")
    }

    fun String.longestCommonPrefixLen(o: String): Int {
        var i = 0
        var lastSeparator = -1
        val len = length()
        val oLen = o.length()
        // Provides len >= oLen
        if (oLen > len)
            return o.longestCommonPrefixLen(this)
        while (i < len && i < oLen && this[i] == o[i]) {
            if (this[i] == File.separatorChar)
                lastSeparator = i
            i++
        }
        if (i == oLen) {
            // Same strings => prefix = string
            if (i == len)
                return i
            // i < len, i == oLen
            // o = /foo/bar & this = /foo/bar/gav => prefix = /foo/bar
            if (this[i] == File.separatorChar)
                return i
        }
        // i < oLen <= len
        // o = /foo/bar/ & this = /foo/bar/gav => prefix = /foo/bar/
        // o = /foo/bar/my & this = /foo/bar/gav => prefix = /foo/bar/
        // o = /foo/bar & this = /foo/baran => prefix = /foo/ !!!
        // o = /foo/bar & this = /foo/baaz => prefix = /foo/
        return lastSeparator + 1
    }

    val commonPrefLen = thisCanonical.longestCommonPrefixLen(baseCanonical)
    val thisSuffix = thisCanonical.substring(commonPrefLen + if (commonPrefLen == baseCanonical.length()) 1 else 0)
    val baseSuffix = baseCanonical.substring(commonPrefLen + if (commonPrefLen == thisCanonical.length()) 1 else 0)
    val separator = File.separatorChar
    val ups = if (baseSuffix.isEmpty()) 0 else baseSuffix.count { it == separator } + 1
    val result = StringBuilder()
    for (i in 1..ups) {
        if (i != 1) {
            result.append(separator)
        }
        result.append("..")
    }
    if (commonPrefLen != thisCanonical.length()) {
        if (result.length() != 0) {
            result.append(separator)
        }
        result.append(thisSuffix)
    }
    return result.toString()
}

deprecated("Use relativeTo() function instead")
public fun File.relativePath(descendant: File): String {
    val prefix = directory.canonicalPath
    val answer = descendant.canonicalPath
    return if (answer.startsWith(prefix)) {
        val prefixSize = prefix.length()
        if (answer.length() > prefixSize) {
            answer.substring(prefixSize + 1)
        } else ""
    } else {
        answer
    }
}

/**
 * Copies this file to the given output [dst], returning the number of bytes copied.
 * 
 * If some directories on a way to the destination are missing, then they will be created.
 * If the destination file already exists, then this function will fail unless 'overwrite' argument is set to true and
 * the destination file is not a non-empty directory.
 *
 * Note: this function fails if you call it on a directory.
 * If you want to copy directories, use 'copyRecursively' function instead.
 * 
 * @param bufferSize the buffer size to use when copying.
 * @return the number of bytes copied
 * @throws NoSuchFileException if the source file doesn't exist
 * @throws FileIsDirectoryException if the source file is a directory
 * @throws FileAlreadyExistsException if the destination file already exists and 'rewrite' argument is set to false
 * @throws DirectoryNotEmptyException if 'rewrite' argument is set to true, but the destination file exists and it is a non-empty directory
 * @throws IOException if any errors occur while copying
 */
public fun File.copyTo(dst: File, overwrite: Boolean = false, bufferSize: Int = defaultBufferSize): Long {
    if (!exists()) {
        throw NoSuchFileException(file = this.toString(), reason = "The source file doesn't exist")
    } else if (isDirectory()) {
        throw FileIsDirectoryException(file = this.toString(), reason = "Cannot copy a directory")
    } else if (dst.exists()) {
        if (!overwrite) {
            throw FileAlreadyExistsException(file = this.toString(),
                                             other = dst.toString(),
                                             reason = "The destination file already exists")
        } else if (dst.isDirectory() && dst.listFiles().any()) {
            throw DirectoryNotEmptyException(file = this.toString(),
                                             other = dst.toString(),
                                             reason = "The destination file is a non-empty directory")
        }
    }
    dst.getParentFile().mkdirs()
    dst.delete()
    val input = FileInputStream(this)
    return input.use<FileInputStream, Long> {
        val output = FileOutputStream(dst)
        output.use<FileOutputStream, Long> {
            input.copyTo(output, bufferSize)
        }
    }
}

/**
 * Enum that can be used to specify behaviour of the `copyRecursively()` function
 * in exceptional conditions.
 */
public enum class OnErrorAction {
    /** Skip this file and go to the next. */
    SKIP

    /** Terminate the evaluation of the function. */
    TERMINATE
}

/**
 * Copies this file with all its children to the specified destination path.
 * If some directories on the way to the destination are missing, then they will be created.
 *
 * If any errors occur during the copying, then further actions will depend on the result of the call
 * to `onError(File, IOException)` function, that will be called with arguments,
 * specifying the file that caused the error and the exception itself.
 * By default this function rethrows exceptions.
 * Exceptions that can be passed to the `onError` function:
 * NoSuchFileException - if there was an attempt to copy a non-existent file
 * FileAlreadyExistsException - if there is a conflict
 * AccessDeniedException - if there was an attempt to open a directory that didn't succeed.
 * IOException - if some problems occur when copying.
 *
 * Returns false if the copying was terminated, true otherwise.
 *
 * Note that if this function fails, then partial copying may have taken place.
 */
public fun File.copyRecursively(dst: File,
                                onError: (File, IOException) -> OnErrorAction =
                                {(file: File, e: IOException) -> throw e }
                               ): Boolean {
    fun copy(src: File): OnErrorAction? {
        if (!src.exists()) {
            return onError(this, NoSuchFileException(file = toString(), reason = "The source file doesn't exist"))
        }
        val relPath = src.relativeTo(this@copyRecursively)
        val dstFile = File(dst, relPath)
        if (dstFile.exists() && !(src.isDirectory() && dstFile.isDirectory())) {
            return onError(dstFile, FileAlreadyExistsException(file = src.toString(),
                                                               other = dstFile.toString(),
                                                               reason = "The destination file already exists"))
        }
        try {
            if (src.isDirectory()) {
                dstFile.mkdirs()
                val children = src.listFiles()
                if (children == null) {
                    return onError(src, AccessDeniedException(file = src.toString(),
                                                              reason = "Cannot list files in a directory"))
                }
                for (child in children) {
                    val result = copy(child)
                    if (result == OnErrorAction.TERMINATE) {
                        return result
                    }
                }
            } else {
                if (src.copyTo(dstFile, true) != src.length()) {
                    return onError(src, IOException("src.length() != dst.length()"))
                }
            }
        } catch (e: IOException) {
            return onError(src, e)
        }
        return null
    }

    val result = copy(this)

    return result != OnErrorAction.TERMINATE
}

/**
 * Delete a file with all its children.
 *
 * Returns true if the file or directory is successfully deleted, false otherwise.
 *
 * Note that if this operation fails then partial deletion may have taken place.
 */
public fun File.deleteRecursively(): Boolean {
    if (isDirectory()) {
        listFiles()?.forEach { it.deleteRecursively() }
    }
    return delete()
}

/**
 * Returns an array of files and directories in the directory that satisfy the specified [filter]
 * or null if this file does not denote a directory.
 */
public fun File.listFiles(filter: (file: File) -> Boolean): Array<File>? = listFiles(
        object : FileFilter {
            override fun accept(file: File) = filter(file)
        }
                                                                                    )

/**
 * Returns an iterator to go through name parts, e.g.
 * /foo/bar/gav has parts foo, bar and gav
 */
public fun File.iterator(): Iterator<File> = FileIterator(this)

/**
 * Returns true if this file belongs to the same root as [o]
 * and starts with all components of [o] in the same order
 */
public fun File.startsWith(o: File): Boolean {
    val it = FileIterator(this)
    val otherIt = FileIterator(o)
    // Roots must be same OR other path can have no root
    if (it.root != otherIt.root && otherIt.root != "")
        return false
    // Other path is empty
    if (!otherIt.hasNext())
        return true
    val count = this.getNameCount()
    val otherCount = o.getNameCount()
    // This path is shorted than the other
    if (count < otherCount)
        return false
    // Compare first elements until other ends
    while (otherIt.hasNext()) {
        if (it.next() != otherIt.next())
            return false
    }
    return true
}

/**
 * Returns true if this file belongs to the same root as [o]
 * and starts with all components of [o] in the same order
 */
public fun File.startsWith(o: String): Boolean = startsWith(File(o))

/**
 * Returns true if this file belongs to the same root as [o]
 * and ends with all components of [o] in the same order
 */
public fun File.endsWith(o: File): Boolean {
    val it = FileIterator(this)
    val otherIt = FileIterator(o)
    // Roots must be same OR other path can have no root
    if (it.root != otherIt.root && otherIt.root != "")
        return false
    // Other path is empty
    if (!otherIt.hasNext())
        return true
    var theirs = otherIt.next()
    val count = this.getNameCount()
    val otherCount = o.getNameCount()
    // This path is shorted than the other
    if (count < otherCount)
        return false
    var ours = it.next()
    // Move forward to have same number of elements remaining
    for (i in 0..count-otherCount-1) {
        ours = it.next()
    }
    // Check all next elements are same, until the end
    while (ours == theirs && it.hasNext() && otherIt.hasNext()) {
        ours = it.next()
        theirs = otherIt.next()
    }
    if (ours != theirs || it.hasNext() || otherIt.hasNext())
        return false
    return true
}

/**
 * Returns true if this file belongs to the same root as [o]
 * and ends with all components of [o] in the same order
 */
public fun File.endsWith(o: String): Boolean = endsWith(File(o))

/**
 * Removes all . and resolves all possible .. in a file name
 */
public fun File.normalize(): File {
    val rootName = getRootName()
    val list: MutableList<String> = ArrayList()
    for (elem in this)
        if (elem.toString() != ".")
            list.add(elem.toString())
    var first = 0
    val dots = list.subList(first, list.size()).indexOf("..")
    while (dots != -1) {
        if (dots > first) {
            list.remove(dots)
            list.remove(dots-1)
        } else {
            first++
        }
    }
    var res = rootName
    var addSeparator = !res.endsWith(File.separatorChar)
    for (elem in list) {
        if (addSeparator)
            res += File.separatorChar
        res += elem
        addSeparator = true
    }
    return File(res)
}

/**
 * Adds relative [o] to this, considering this as a directory.
 * If [o] has a root, [o] is returned back
 */
public fun File.resolve(o: File): File {
    if (o.getRoot() != null)
        return o
    val ourName = toString()
    if (ourName.endsWith(File.separatorChar))
        return File(ourName + o)
    else
        return File(ourName + File.separatorChar + o)
}

/**
 * Adds relative [o] to this, considering this as a directory.
 * If [o] has a root, [o] is returned back
 */
public fun File.resolve(o: String): File = resolve(File(o))

/**
 * Adds relative [o] to this parent directory.
 * If [o] has a root or this has no parent directory, [o] is returned back
 */
public fun File.resolveSibling(o: File): File {
    val parentFile = getParentFile()
    if (parentFile != null)
        return parentFile.resolve(o)
    else
        return o
}

/**
 * Adds relative [o] to this parent directory.
 * If [o] has a root or this has no parent directory, [o] is returned back
 */
public fun File.resolveSibling(o: String): File = resolveSibling(File(o))
