package com.adapter.oishi;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by batmaster on 2/26/16 AD.
 */
public class HTTPService {

    private Context context;
    private RequestQueue queue;
    private String BASE_URL = "http://www.oishidrink.com/sakura/api/mobile/";

    public HTTPService(Context context) {
        this.context = context;
        queue = Volley.newRequestQueue(context);
    }


    /**
     * API 1 SaveGameNonToken-iOS/Android ส่งค่าตอนเริ่มเกม
     *
     * @param responseCallback
     */
    public void saveGame(final OnResponseCallback<JSONObject> responseCallback) {
        Log.d("httpapi", "API 1 SaveGameNonToken-iOS/Android");

        final StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "submitGameNonToken.aspx", new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                Log.d("httpapi", "API 1 onResponse: " + s);
                try {
                    JSONObject json = new JSONObject(s);

                    responseCallback.onResponse(true, null, json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("httpapi", "API 1 onErrorResponse: " + volleyError);
                responseCallback.onResponse(false, volleyError, null);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", _UA);
                return headers;
            }
        };

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("param1", "android");

        params.put("access", "mobileapp");
        params.put("caller", "json");

        if (AccessToken.getCurrentAccessToken() == null) {
            params.put("fbuid", SharePref.getStringRid(context));
            request.setParams(params);
            queue.add(request);
        } else {
            GraphRequest graph = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    if (response.getJSONObject() != null) {
                        Log.d("httpapi", "API 5 != null GraphResponse response: " + response.toString());

                        try {
                            String first_name = response.getJSONObject().getString("first_name");
                            String last_name = response.getJSONObject().getString("last_name");
                            String name = "";//response.getJSONObject().getString("name");
                            String email = "";//response.getJSONObject().getString("email");
                            String gender = response.getJSONObject().getString("gender");
                            String link = response.getJSONObject().getString("link");

                            params.put("fbuid", AccessToken.getCurrentAccessToken().getUserId());
                            params.put("firstname", first_name);
                            params.put("lastname", last_name);
                            params.put("username", name);
                            params.put("email", email);
                            params.put("gender", gender);
                            params.put("profilelink", link);

                            Log.d("httpapi", "API 5 " + params);

                            request.setParams(params);
                            queue.add(request);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,first_name,last_name,email,gender,link");
            graph.setParameters(parameters);
            graph.executeAsync();
        }
    }

    /**
     * API 2 SaveGameComplete-iOS/Android ส่งค่าตอนเปลี่ยนไปหน้า preview
     *
     * @param responseCallback
     */
    public void goToPreview(String gid, String where, final OnResponseCallback<JSONObject> responseCallback) {
        Log.d("httpapi", "API 2 SaveGameComplete-iOS/Android");
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "submitGameComplete.aspx", new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                Log.d("httpapi", "API 2 onResponse: " + s);
                try {
                    JSONObject json = new JSONObject(s);

                    responseCallback.onResponse(true, null, json);

                } catch (JSONException e) {
                    e.printStackTrace();
                    responseCallback.onResponse(true, null, null);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("httpapi", "API 2 onErrorResponse: " + volleyError);
                responseCallback.onResponse(false, volleyError, null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", _UA);
                return headers;
            }
        };

        queue.add(request);
    }

    /**
     * API 3 Share
     *
     * @param responseCallback
     */
    public void saveShare(String gid, String postId, final OnResponseCallback<JSONObject> responseCallback) {
        Log.d("httpapi", "API 3 Share");
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "saveShareToWall.aspx", new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                Log.d("httpapi", "API 3 onResponse: " + s);
                try {
                    JSONObject json = new JSONObject(s);

                    responseCallback.onResponse(true, null, json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("httpapi", "API 3 onErrorResponse: " + volleyError);
                responseCallback.onResponse(false, volleyError, null);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", _UA);
                return headers;
            }
        };

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("gid", gid);
        params.put("type", "postshare");
        params.put("postid", postId);
        params.put("access", "mobileapp");
        params.put("code", AccessToken.getCurrentAccessToken().getToken());
        params.put("caller", "json");
        request.setParams(params);

        queue.add(request);
    }

    /**
     * API 4 Update FacebookID-NonToken
     *
     * @param responseCallback
     */
    public void updateToken(final OnResponseCallback<JSONObject> responseCallback) {
        Log.d("httpapi", "API 4 Update FacebookID-NonToken with: " + AccessToken.getCurrentAccessToken());
        GraphRequest graph = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.d("httpapi", "API 4 GraphResponse response: " + response);

                try {
                    String first_name = response.getJSONObject().getString("first_name");
                    String last_name = response.getJSONObject().getString("last_name");
                    String name = "";//response.getJSONObject().getString("name");
                    String email = "";//response.getJSONObject().getString("email");
                    String gender = response.getJSONObject().getString("gender");
                    String link = response.getJSONObject().getString("link");

                    StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "getinfoV3NonToken.aspx", new Response.Listener<String>() {

                        @Override
                        public void onResponse(String s) {
                            Log.d("httpapi", "API 4 onResponse: " + s);
                            try {
                                JSONObject json = new JSONObject(s);

                                responseCallback.onResponse(true, null, json);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d("httpapi", "API 4 onErrorResponse: " + volleyError);
                            responseCallback.onResponse(false, volleyError, null);

                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<String, String>();
                            headers.put("User-agent", _UA);
                            return headers;
                        }
                    };

                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("fakefbuid", SharePref.getStringRid(context));
                    params.put("fbuid", AccessToken.getCurrentAccessToken().getUserId());
                    params.put("firstname", first_name);
                    params.put("lastname", last_name);
                    params.put("username", name);
                    params.put("email", email);
                    params.put("gender", gender);
                    params.put("profilelink", link);
                    params.put("access", "mobileapp");
                    params.put("caller", "json");
                    request.setParams(params);

                    Log.d("httpapi", "API 6 HashMap: " + params.toString());

                    queue.add(request);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email,gender,link");
        graph.setParameters(parameters);
        graph.executeAsync();
    }



    // STAT

    public static final String OPENAPP = "openapp";
    public static final String STARTGAME = "startgame";
    public static final String SHARERESULT = "shareresult";
    public static final String SAVERESULT = "saveresult";

    public void sendStat(final String STAT) {
        Log.d("httpapi", "API 00: " + STAT);

        StringRequest request = new StringRequest(Request.Method.GET, "http://www.oishidrink.com/sakura/api/mobile/applicationstatlog.aspx", new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                Log.d("httpapi", "API 00: " + STAT + " onResponse: " + s);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("httpapi", "API 00: " + STAT + " onErrorResponse: " + volleyError);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", _UA);
                return headers;
            }


        };

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("stat", "sakura");
        params.put("param1", "android");
        params.put("param2", STAT);
        request.setParams(params);

        queue.add(request);
    }

    public interface OnResponseCallback<T> {
        void onResponse(boolean success, Throwable error, T data);
    }

    public static final String _UA = Build.BRAND + " " + Build.MODEL + " (" + Build.BOOTLOADER + "); API: " + Build.VERSION.SDK_INT;

}
