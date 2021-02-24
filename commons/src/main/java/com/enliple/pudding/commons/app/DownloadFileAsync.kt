package com.enliple.pudding.commons.app

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.net.URL

class DownloadFileAsync(private val downloadLocation: String, private val callback: PostDownload?) : AsyncTask<String?, String?, String?>() {
    private var fd: FileDescriptor? = null
    private var file: File? = null

    override fun doInBackground(vararg aurl: String?): String? {

        try {
            val url = URL(aurl[0])
            val connection = url.openConnection()
            connection.connect()

            val lenghtOfFile = connection.contentLength
            Log.d(TAG, "Length of the file: $lenghtOfFile")

            val input = BufferedInputStream(url.openStream())
            file = File(downloadLocation)
            val output = FileOutputStream(file) //context.openFileOutput("content.zip", Context.MODE_PRIVATE);
            Log.d(TAG, "file saved at " + file!!.absolutePath)
            fd = output.fd

            val data = ByteArray(1024)
            var total: Long = 0
            var count:Int
            while (true) {
                count = input.read(data)
                if (count == -1)
                    break
                
                total += count.toLong()
                publishProgress("" + (total * 100 / lenghtOfFile).toInt())
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
        } catch (e: Exception) {
        }

        return null

    }

    override fun onProgressUpdate(vararg progress: String?) {
        //Log.d(TAG,progress[0]);
    }

    override fun onPostExecute(unused: String?) {
        callback?.downloadDone(file)
    }

    interface PostDownload {
        fun downloadDone(file: File?)
    }

    companion object {

        private val TAG = "DOWNLOADFILE"

        val DIALOG_DOWNLOAD_PROGRESS = 0
    }
}
