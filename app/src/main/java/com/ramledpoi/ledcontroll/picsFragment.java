package com.ramledpoi.ledcontroll;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link picsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link picsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class picsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private boolean mParam2;

    private OnFragmentInteractionListener mListener;

    public picsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment picsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static picsFragment newInstance(String param1, boolean param2) {
        picsFragment fragment = new picsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putBoolean(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getBoolean(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_pics, container, false);

        WebView webView1 = view.findViewById(R.id.ww1);
        WebView webView2 = view.findViewById(R.id.ww2);
        WebSettings webSettings1 = webView1.getSettings();
        WebSettings webSettings2 = webView2.getSettings();
        webSettings1.setJavaScriptEnabled(true);
        webView1.addJavascriptInterface(new WebAppInterface(getContext()), "Android");

        File dir = new File("/data/data/com.ramledpoi.ledcontroll/cache");

        webView1.setWebViewClient(new MyWebViewClient());
        webView2.setWebViewClient(new MyWebViewClient());

        webSettings1.setDomStorageEnabled(true);
        webSettings1.setAppCacheMaxSize(1024*1024*8);
        webSettings1.setAppCachePath(dir.getPath());
        webSettings1.setAllowFileAccess(true);
        webSettings1.setAppCacheEnabled(true);
        webSettings1.setCacheMode(WebSettings.LOAD_DEFAULT);


        webSettings2.setDomStorageEnabled(true);
        webSettings2.setAppCacheMaxSize(1024*1024*8);
        webSettings2.setAppCachePath(dir.getPath());
        webSettings2.setAllowFileAccess(true);
        webSettings2.setAppCacheEnabled(true);
        webSettings2.setCacheMode(WebSettings.LOAD_DEFAULT);

        String ipe = mParam1;
        TextView t1 = view.findViewById(R.id.textViewAdr1);
        TextView t2 = view.findViewById(R.id.textViewAdr2);
        try {
            if (!ipe.isEmpty()) {
                String[] ips = ipe.split("\n");
                if (ips.length == 1) {
                    String[] ips0 = ips[0].split("\\.");
                    webView1.loadUrl("http://" + Integer.parseInt(ips0[0]) + "." + Integer.parseInt(ips0[1]) + "." + Integer.parseInt(ips0[2]) + "." + Integer.parseInt(ips0[3]) + "." + "/filesap");
                    t1.setText(ips[0]);
                    t2.setText("adr2");
                }
                if (ips.length > 1) {
                    String[] ips0 = ips[0].split("\\.");
                    webView1.loadUrl("http://" + Integer.parseInt(ips0[0]) + "." + Integer.parseInt(ips0[1]) + "." + Integer.parseInt(ips0[2]) + "." + Integer.parseInt(ips0[3]) + "." + "/filesap");
                    webView1.loadUrl("http://" + ips[0] + "/filesap");
                    String[] ips1 = ips[1].split("\\.");
                    webView2.loadUrl("http://" + Integer.parseInt(ips1[0]) + "." + Integer.parseInt(ips1[1]) + "." + Integer.parseInt(ips1[2]) + "." + Integer.parseInt(ips1[3]) + "." + "/filesap");
                    t1.setText(ips[0]);
                    t2.setText(ips[1]);
                }
            } else {
                t1.setText("adr1");
                t2.setText("adr2");
            }
        } catch (Throwable e) {
            Toast.makeText(getContext(), "Ошибка "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PICSERR", e.getMessage());
            e.printStackTrace();
        }
        CheckBox checkBox1 = view.findViewById(R.id.checkBoxCashing);
        checkBox1.setChecked(mParam2);
        if(mParam2) {
            webSettings1.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            webSettings2.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        }
        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class MyWebViewClient extends WebViewClient {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        // Для старых устройств
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
