package com.enliple.commons.io

import android.content.Context
import android.content.res.AssetManager
import android.text.TextUtils
import com.enliple.pudding.commons.log.Logger
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * Created by hkcha on 2018-01-09.
 * File Access Utils with kotlin
 */
class FileUtils {
    companion object {
        private const val TAG = "FileUtils"

        // File Access 기본 Buffer size
        private const val DEFAULT_BUFFER_SIZE: Int = 8192

        /**
         * 해당 경로에 있는 파일을 삭제
         */
        fun removeFile(fileFullPath: String): Boolean {
            var f: File = File(fileFullPath)
            if (f.exists()) {
                return f.delete()
            }

            return false
        }

        /**
         * 특정 디렉토리 내 파일들을 제거
         */
        fun removeRecursive(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                try {
                    var children: Array<String> = dir.list()
                    children.indices
                            .map { removeRecursive(File(dir, children[it])) }
                            .filterNot { it }
                            .forEach { return false }
                } catch (e: IOException) {
                    Logger.p(e)
                }
            }

            return dir?.delete() ?: false
        }

        /**
         * 빈 파일을 생성
         */
        fun createNewEmptyFile(fileFullPath: String): Boolean {
            var paths: List<String> = fileFullPath.split(File.separator)
            var dir = StringBuilder()

            for (i in paths.indices) {
                if (i == paths.size - 2) {
                    dir.append(paths[i])
                } else {
                    dir.append(paths[i] + File.pathSeparator)
                }
            }

            return if (createDir(dir.toString())) {
                var createFile = File(fileFullPath)
                try {
                    createFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                    false
                }
            } else {
                false
            }
        }

        /**
         * 해당 경로의 디렉토리를 생성
         */
        fun createDir(createPath: String): Boolean {
            return try {
                var dir: File = File(createPath)

                if (!dir.exists()) {
                    dir.mkdirs()
                }

                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * 바이너리 파일을 생성
         * @param fileFullPath
         * @param binaryStream
         * @param callback
         * @return
         */
        fun writeFile(fileFullPath: String, binaryStream: InputStream, callback: ProgressCallback?): Boolean {
            if (TextUtils.isEmpty(fileFullPath)) return false

            var isWrite = false
            if (createNewEmptyFile(fileFullPath)) {
                var f = File(fileFullPath)
                var fos: FileOutputStream? = null

                try {
                    fos = FileOutputStream(f)
                    var totalSize: Int = binaryStream.available()
                    var read: Int
                    var buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    read = binaryStream.read(buffer)
                    if (read != -1) {
                        do {
                            fos.write(buffer, 0, read)
                            if (callback != null) {
                                val writeSize = totalSize - binaryStream.available()
                                callback.onWriteProgress(totalSize, writeSize,
                                        if (read > 0) writeSize.toDouble() / totalSize.toDouble() * 100.0 else -1.0)
                            }

                            read = binaryStream.read(buffer)
                        } while (read != -1)
                    }

                    isWrite = true
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        binaryStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    try {
                        fos?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            return isWrite
        }

        /**
         * 바이너리 파일을 생성
         * @param fileFullPath
         * @param data
         * @param callback
         * @return
         */
        fun writeFile(fileFullPath: String, data: ByteArray, callback: ProgressCallback) =
                writeFile(fileFullPath, ByteArrayInputStream(data), callback)

        /**
         * 바이너리 파일을 생성
         * @param fileFullPath
         * @param data
         * @param callback
         * @return
         */
        fun writeFile(fileFullPath: String, data: String, callback: ProgressCallback): Boolean =
                writeFile(fileFullPath, data.toByteArray(Charset.forName("UTF-8")), callback)

        /**
         * 파일을 읽어서 문자열 형태로 변환하이 획득
         */
        fun readStringFromFile(fileFullPath: String, callback: ProgressCallback): String? {
            var readBytes: ByteArray? = readBinaryFromFile(fileFullPath, callback)

            if (readBytes != null && readBytes.size > 0) {
                return try {
                    readBytes.toString(Charset.forName("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    readBytes.toString()
                }
            }

            return null
        }

        /**
         * 바이너리 파일로부터 ByteArray 형태의 데이터 배열을 획득
         * @param fileFullPath
         * @param callback
         */
        fun readBinaryFromFile(fileFullPath: String, callback: ProgressCallback?): ByteArray? {
            if (TextUtils.isEmpty(fileFullPath) || isDir(fileFullPath)) return null

            var file = File(fileFullPath)
            var length: Int = file.length().toInt()
            var result: ByteArray? = null

            if (file.exists()) {
                result = ByteArray(length)
                try {
                    var input: InputStream?
                    var totalBytesRead = 0
                    input = BufferedInputStream(FileInputStream(file))
                    while (totalBytesRead < result.size) {
                        var bytesRemaining: Int = result.size - totalBytesRead
                        var bytesRead = input.read(result, totalBytesRead, bytesRemaining)
                        if (bytesRead > 0) {
                            totalBytesRead += bytesRead
                        }

                        callback?.onReadProgress(length, totalBytesRead,
                                if (bytesRead > 0) totalBytesRead.toDouble() / length.toDouble() * 100.0 else -1.0)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            return result
        }

        /**
         * 해당 경로가 디렉토리인지 확인
         */
        fun isDir(path: String): Boolean = try {
            File(path).isDirectory
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

        /**
         * 해당 경로가 파일경로인지 확인
         */
        fun isFile(path: String): Boolean = try {
            File(path).isFile
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

        /**
         * 해당 경로의 크기를 확인
         */
        fun getPathLength(path: String) = try {
            var f = File(path)
            if (f.exists()) f.length() else 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

        /**
         * 원본 경로 위치의 파일을 대상경로로 복사
         * @param srcFile
         * @param destFile
         * @param callback
         */
        @Throws(IOException::class)
        fun copyPath(srcFile: File, destFile: File, callback: ProgressCallback): Boolean {
            if (isFile(srcFile.absolutePath)) {
                var inStream: InputStream = FileInputStream(srcFile)
                var outStream = FileOutputStream(destFile)

                try {
                    var channelIn: ReadableByteChannel =
                            CallbackByteChannel(Channels.newChannel(inStream), srcFile.length(), callback)
                    var channelOut: FileChannel = outStream.getChannel()
                    channelOut.transferFrom(channelIn, 0, Long.MAX_VALUE)
                    inStream.close()
                    outStream.close()

                    Logger.d(TAG, "File copied from ${srcFile.absolutePath} to ${destFile.absolutePath} is successfully")
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            } else {
                if (!destFile.exists()) {
                    destFile.mkdir()
                    Logger.d(TAG, "Directory copied from ${srcFile.absolutePath} to ${destFile.absolutePath} is successfully")
                }

                var folderContents: Array<String> = srcFile.list()

                for (file in folderContents) {
                    var sFile = File(srcFile, file)
                    var dFile = File(destFile, file)
                    copyPath(sFile, dFile, callback)
                }
            }

            return destFile.exists() && srcFile.length() == destFile.length()
        }

        /**
         * 원본 경로 위치의 파일을 대상경로로 복사
         * @param sourcePath
         * @param targetPath
         * @param callback
         * @return
         */
        @Throws(IOException::class)
        fun copyPath(sourcePath: String, targetPath: String, callback: ProgressCallback): Boolean {
            try {
                return copyPath(File(sourcePath), File(targetPath), callback)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return false
        }

        /**
         * 원본 경로 위치를 대상경로 위치로 이동
         * @param sourcePath
         * @param targetPath
         * @param callback
         * @return
         */
        @Throws(IOException::class)
        fun movePath(sourcePath: String, targetPath: String, callback: ProgressCallback): Boolean {
            return try {
                copyPath(sourcePath, targetPath, callback)
                var f = File(sourcePath)
                if (f.isDirectory) {
                    removeRecursive(f)
                } else {
                    removeFile(sourcePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        /**
         * Asset 위치에 해당 파일이 실제로 존재 하는지 확인
         */
        fun isExistsAsset(manager: AssetManager, subDirectory: String, item: String): Boolean {
            var inStream: InputStream? = null

            try {
                inStream = manager.open(subDirectory + "/" + item, AssetManager.ACCESS_BUFFER)
                return inStream != null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    inStream?.close()
                } catch (e: Exception) {
                }
            }

            return false
        }

        /**
         * Asset 위치의 파일 사이즈를 확인
         */
        fun getAssetFileSize(manager: AssetManager, subDirectory: String, item: String): Long {
            return try {
                var afd = manager.openFd(subDirectory + "/" + item)
                afd.length
            } catch (e: IOException) {
                e.printStackTrace()
                0
            }
        }

        /**
         * Asset 으로 부터 Binary File 을 ByteArray 형태로 읽어들임
         * @param context
         * @param subDirectory
         * @param assetFileName
         * @param callback
         * @return
         */
        fun readBinaryFromAsset(context: Context, subDirectory: String,
                                assetFileName: String, callback: ProgressCallback?): ByteArray? {
            try {
                var inStream: InputStream = context.assets.open(subDirectory, AssetManager.ACCESS_RANDOM)
                var byteArrayOutputStream = ByteArrayOutputStream()
                var totalSize = getAssetFileSize(context.assets, subDirectory, assetFileName).toInt()
                var writeSize: Int
                var reads = inStream.read()
                writeSize = reads

                while (reads != -1) {
                    byteArrayOutputStream.write(reads)
                    reads = inStream.read()
                    writeSize += reads

                    callback?.onReadProgress(totalSize, writeSize, if (reads > 0) writeSize.toDouble() / totalSize.toDouble() * 100.0 else -1.0)
                }

                return byteArrayOutputStream.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        /**
         * Asset TextFile 로 부터 String Text 를 읽어들임
         */
        fun readTextFromAsset(context: Context, subDirectory: String, assetFileName: String, callback: ProgressCallback): String? {
            var readByte: ByteArray? = readBinaryFromAsset(context, subDirectory, assetFileName, callback)

            if (readByte != null) {
                try {
                    readByte.toString(Charset.forName("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    readByte.toString()
                }
            }

            return null
        }

        /**
         * Asset 의 해당 SubDirectory 내 파일 리스트를 확인
         * @param context
         * @param subDirectoryName
         * @return
         */
        fun readAssetItemsAtDirectory(context: Context, subDirectoryName: String?): List<String>? {
            var assetMgr = context.assets
            var resultList = ArrayList<String>()

            return try {
                var items: Array<String> = assetMgr.list(subDirectoryName ?: "/")
                items.filterTo(resultList) { isExistsAsset(assetMgr, subDirectoryName ?: "/", it) }
            } catch (e: IOException) {
                null
            }
        }

        fun unzip(zipFile: File, targetPath: String): File {
            val zip = ZipFile(zipFile)
            val enumeration = zip.entries()
            lateinit var file: File
            while (enumeration.hasMoreElements()) {
                val entry = enumeration.nextElement()
                val destFilePath = File(targetPath, entry.name)
                destFilePath.parentFile.mkdirs()
                if (entry.isDirectory)
                    continue
                val bufferedIs = BufferedInputStream(zip.getInputStream(entry))
                bufferedIs.use {
                    destFilePath.outputStream().buffered(1024).use { bos ->
                        bufferedIs.copyTo(bos)
                    }
                }
                file = destFilePath
            }
            return file
        }
    }

    class CallbackByteChannel(rbc: ReadableByteChannel, expectedSize: Long, delegate: ProgressCallback?) : ReadableByteChannel {

        val delegate: ProgressCallback? = delegate
        val rbc: ReadableByteChannel = rbc
        var size: Long = expectedSize
        var sizeRead: Long = 0L

        override fun isOpen(): Boolean = rbc.isOpen

        @Throws(IOException::class)
        override fun close() {
            rbc.close()
        }

        @Throws(IOException::class)
        override fun read(bb: ByteBuffer?): Int {
            var n: Int = rbc.read(bb)
            var progress: Double

            if (n > 0) {
                sizeRead += n
                progress = if (size > 0) sizeRead.toDouble() / size.toDouble() * 100.0 else -1.0

                delegate?.onCopyProgress(this, progress)
            }

            return n
        }
    }

    interface ProgressCallback {
        fun onCopyProgress(rbc: CallbackByteChannel, progress: Double)
        fun onWriteProgress(totalSize: Int, writeSize: Int, progress: Double)
        fun onReadProgress(totalSize: Int, readSize: Int, progress: Double)
    }
}