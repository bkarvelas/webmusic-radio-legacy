package gr.webmusic.webmusicradio.lib;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;


/**
 * Simple Volley Class for doing XML HTTP Requests which are parsed
 * into Java objects by Simple @see {{@link //simple.sourceforge.net/}
 */
public class SimpleXmlRequest<T> extends Request<T> {

    public static final int XML_REQUEST = 1;
//    public static final int GSON_REQUEST = 2;

//    private Gson mGson;
    private Serializer mSerializer;
    private final Class<T> mClazz;
    private final Listener<T> mListener;
    private final int mRequestType;


    public SimpleXmlRequest(int method, int requestType, String url, Class<T> clazz, Listener<T> listener,
                          ErrorListener errorListener) {
        super(method, url, errorListener);
        mClazz = clazz;
        mListener = listener;
        mRequestType = requestType;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String source = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            if (mRequestType == XML_REQUEST) {

                mSerializer = new Persister();
                Reader reader = new StringReader(source);
                return Response.success(mSerializer.read(mClazz, reader, false),
                        HttpHeaderParser.parseCacheHeaders(response));

            }
//            else if (mRequestType == GSON_REQUEST) {
//
//                mGson = new Gson();
//                return Response.success(mGson.fromJson(source, mClazz), HttpHeaderParser.parseCacheHeaders(response));
//
//            }
            else {
                return null;
            }

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
//        } catch (JsonSyntaxException e) {
//            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}

