package com.ramledpoi.ledcontroll;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

class httpGet extends AsyncTask<String, Void, Wrapper> {

    Context context;
    public httpGet(Context context){
        this.context=context;
    }

    protected Wrapper doInBackground(String... url) {
        Wrapper w = new Wrapper();
        try {
            w.url = url[0];
            w.result = myNet.get(url[0]);
        } catch (Exception e) {
            w.error = e.getMessage();
            e.printStackTrace();
            return w;
        }
        return w;
    }

    protected void onPostExecute(Wrapper w) {
        //Log.d("STATE", w == null ? "w is null" : "w is not null");
        try {
            if(w.url.contains("cloud.mail.ru")){
                if(w.result == null) {
                    return;
                }
                JSONObject files = new JSONObject(w.result).getJSONObject("body");
                final String ls = files.getJSONArray("list").getJSONObject(0).getString("name");
                String myV = (context).getString(R.string.version).replaceAll("\\D+","");
                String newV = ls.replaceAll("\\D+","");
                if(!myV.equals(newV)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Доступна новая версия. Скачать?").setTitle("Обновление");
                    builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.updateLink +"/"+ls));
                            (context).startActivity(browserIntent);
                        }
                    });
                    builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } else {
                ((MainActivity) context).parseConf("0 " + w.result);
                TextView txtView = ((Activity) context).findViewById(R.id.textViewAns);
                txtView.append("\n" + myNet.getIpFromString(w.url) + ": " + w.result);
            }
        }  catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
