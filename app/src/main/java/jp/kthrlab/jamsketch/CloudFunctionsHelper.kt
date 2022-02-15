package jp.kthrlab.jamsketch

import com.google.android.gms.common.util.IOUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class CloudFunctionsHelper {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val boundary = UUID.randomUUID().toString()
    private val twoHyphens = "--"
    fun uploadScc(name: String, melodyData: MelodyData2, mimeType: String?): Task<String?> {
        return Tasks.call(mExecutor) {
            uploadFile(
                name,
                ByteArrayInputStream(melodyData.scc.toWrapper().toMIDIXML().smfByteArray),
                mimeType
            )
        }
    }

    fun uploadSccxml(name: String, melodyData: MelodyData2, mimeType: String?): Task<String?> {
        return Tasks.call(mExecutor) {
            val outputStreamScc = ByteArrayOutputStream()
            TransformerFactory.newInstance().newTransformer().transform(
                DOMSource(melodyData.scc.toWrapper().xmlDocument),
                StreamResult(outputStreamScc)
            )
            uploadFile(
                name,
                ByteArrayInputStream(outputStreamScc.toByteArray()),
                mimeType
            )
        }
    }

    fun uploadJson(name: String, melodyData: MelodyData2, mimeType: String?): Task<String?> {
        return Tasks.call(mExecutor) {
            uploadFile(
                name,
                ByteArrayInputStream(Gson().toJson(melodyData.curve1).toByteArray()),
                mimeType
            )
        }
    }

    fun uploadText(name: String, text: String, mimeType: String?): Task<String?> {
        return Tasks.call(mExecutor) {
            uploadFile(
                name,
                ByteArrayInputStream(text.toByteArray()),
                mimeType
            )
        }
    }

    @Throws(IOException::class)
    fun uploadFile(name: String, inputStream: InputStream?, mimeType: String?): String? {
        var con: HttpURLConnection? = null
        var message: String? = null
        if (BuildConfig.CLOUD_FUNCTIONS != "") {
            con = URL(BuildConfig.CLOUD_FUNCTIONS).openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con!!.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
            // https://developer.mozilla.org/ja/docs/Web/HTTP/Headers/Content-Disposition
            val out = DataOutputStream(con.outputStream)
            out.writeBytes(twoHyphens + boundary + System.lineSeparator())
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + boundary + "_" + name + "\"" + System.lineSeparator() + System.lineSeparator())

            // write OutputStream
            out.write(IOUtils.toByteArray(inputStream!!))
            out.writeBytes(System.lineSeparator())
            out.writeBytes(twoHyphens + boundary + twoHyphens + System.lineSeparator())
            out.flush()
            out.close()
            con.connect()
            message = con.responseMessage
            println(boundary + "_" + name + " " + message)
            if (con.errorStream != null) {
                con.errorStream.close()
            }
            if (con.inputStream != null) {
                con.inputStream.close()
            }

//            con.getHeaderFields().forEach((key, value) ->System.out.println(con.getHeaderField(key)));
            con.disconnect()
        }
        return message
    } //    public Task<String> uploadFile(String name, InputStream inputStream, String mimeType) {
    //        return Tasks.call(mExecutor, () -> {
    //        });
    //    }
}