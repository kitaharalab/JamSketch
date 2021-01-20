package jp.kthrlab.jamsketch;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class CloudFunctionsHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final String boundary = UUID.randomUUID().toString();
    private final String twoHyphens = "--";

    public Task<String> uploadScc(String name, MelodyData2 melodyData, String mimeType) {
        return Tasks.call(mExecutor, () -> uploadFile(name,
                new ByteArrayInputStream(melodyData.getScc().toWrapper().toMIDIXML().getSMFByteArray()),
                mimeType));
    }

    public Task<String> uploadSccxml(String name, MelodyData2 melodyData, String mimeType) {
        return Tasks.call(mExecutor, () -> {
            ByteArrayOutputStream outputStreamScc = new ByteArrayOutputStream();
            TransformerFactory.newInstance().newTransformer().transform(
                    new DOMSource(melodyData.getScc().toWrapper().getXMLDocument()),
                    new StreamResult(outputStreamScc));
            return uploadFile(name,
                    new ByteArrayInputStream(outputStreamScc.toByteArray()),
                    mimeType);
        });
    }

    public Task<String> uploadJson(String name, MelodyData2 melodyData, String mimeType) {
        return Tasks.call(mExecutor, () -> uploadFile(name,
                new ByteArrayInputStream(new Gson().toJson(melodyData.getCurve1()).getBytes()),
                mimeType));
    }

    public Task<String> uploadText(String name, String text, String mimeType) {
        return Tasks.call(mExecutor, () -> uploadFile(name,
                new ByteArrayInputStream(text.getBytes()),
                mimeType));
    }

    public String uploadFile(String name, InputStream inputStream, String mimeType) throws IOException {
        HttpURLConnection con = null;
        String message = null;

        if (!BuildConfig.CLOUD_FUNCTIONS.equals("")) {
            con = (HttpURLConnection) new URL(BuildConfig.CLOUD_FUNCTIONS).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            // https://developer.mozilla.org/ja/docs/Web/HTTP/Headers/Content-Disposition
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(twoHyphens + boundary + System.lineSeparator());
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + boundary + "_" + name + "\"" + System.lineSeparator() + System.lineSeparator());

            // write OutputStream
            out.write(IOUtils.toByteArray(inputStream));
            out.writeBytes(System.lineSeparator());

            out.writeBytes(twoHyphens + boundary + twoHyphens + System.lineSeparator());
            out.flush();
            out.close();

            con.connect();
            message = con.getResponseMessage();
            System.out.println(boundary + "_" + name + " " + message);

            if (con.getErrorStream() != null) { con.getErrorStream().close(); }
            if (con.getInputStream() != null) { con.getInputStream().close(); }

//            con.getHeaderFields().forEach((key, value) ->System.out.println(con.getHeaderField(key)));
            con.disconnect();

        }
        return message;
    }

//    public Task<String> uploadFile(String name, InputStream inputStream, String mimeType) {
//        return Tasks.call(mExecutor, () -> {
//        });
//    }

}
