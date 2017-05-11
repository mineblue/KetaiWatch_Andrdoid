package com.mineblue.ketaiwatch;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopActivity extends ActionBarActivity {
    private Handler handler;
    private final String RSS_URL = "http://rss.rssad.jp/rss/k-taiwatch/k-tai.rdf";
    private String RssText;
    ArrayList<RSS> rss = new ArrayList<RSS>();
    private TopActivity currentActivity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        ListView ArtView = (ListView) findViewById(R.id.articleListView);
        handler = new Handler();
        currentActivity = this;

        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
        if (drawable != null) {
            drawable.setColorFilter(Color.argb(255, 255, 171, 182), PorterDuff.Mode.SRC);
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(drawable);
        }


        // Adapter - ArrayAdapter
        RSSAdapter adapter = new RSSAdapter(currentActivity, 0, rss);

        // ListViewに表示
        ArtView.setAdapter(adapter);

        ArtView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // インテントのインスタンス生成
                Intent intent = new Intent(TopActivity.this, WebContentActivity.class);
                intent.putExtra("url", rss.get(position).getUrl());
                // 次画面のアクティビティ起動
                startActivity(intent);
            }
        });

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                try {
                    // URLよりRSSを取得
                    RssText = getRss(TopActivity.this, RSS_URL);
                    rss = (ArrayList<RSS>) parse(RssText);
                    Log.d("RssText", rss.toString());

                    // handler
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ListView ArtView = (ListView) findViewById(R.id.articleListView);
                            // Adapter - ArrayAdapter
                            RSSAdapter adapter = new RSSAdapter(currentActivity, 0, rss);

                            // ListViewに表示
                            ArtView.setAdapter(adapter);
                            //Toast.makeText(TopActivity.this, "読み込み終了", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    Log.d("sample", e.getMessage());
                }
            }
        };
        thread1.start();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    public class RSSAdapter extends ArrayAdapter<RSS> {

        private LayoutInflater layoutInflater;

        public RSSAdapter(Context c, int id, ArrayList<RSS> rss) {
            super(c, id, rss);
            this.layoutInflater = (LayoutInflater) c.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
            );
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(
                        R.layout.thum_item,
                        parent,
                        false
                );
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.desc = (TextView) convertView.findViewById(R.id.desc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            RSS item = (RSS) getItem(pos);
            holder.icon.setImageBitmap(item.getIcon());
            holder.title.setText(item.getTitle());
            holder.desc.setText(item.getDesc());

            try{
                Log.d("ListViewTest", pos + "の画像読み込みを開始");

                DownloadImageTask task = new DownloadImageTask(holder.icon, currentActivity);
                task.execute(rss.get(pos).getIconUrl());
            }
            catch(Exception e){
                //
                Log.d("ListViewTest", pos + "の画像読み込みに失敗");
            }
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView desc;
    }

    public static class RSS {
        private Bitmap icon;
        private String iconUrl;
        private String title;
        private String desc;
        private String url;
        private String pubDate;

        public RSS() {
            this.icon = BitmapFactory.decodeResource(
                    Resources.getSystem(),
                    R.mipmap.ic_launcher
            );
            //R.minmap.ic_launchar
        }

        public Bitmap getIcon() {
            return icon;
        }

        public void setIcon(Bitmap icon) {
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getPubDate() {
            return pubDate;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String icon_url) {
            this.iconUrl = icon_url;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static String getRss(Context context, String url) throws IOException {

        StringBuilder sb = new StringBuilder();

        AndroidHttpClient client = AndroidHttpClient.newInstance("TEST");
        HttpGet get = new HttpGet(url);

        try {
            // リクエストを取得
            HttpResponse response = client.execute(get);

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            // 閉じる
            client.close();
        }
        return sb.toString();
    }

    public static List<RSS> parse(String xml){

        List<RSS> list = new ArrayList<RSS>();

        String regex = "([0-9]{4})([0-9]{3}).html";
        Pattern p = Pattern.compile(regex);
        Matcher m;

        // XMLパーサを生成
        XmlPullParser xmlPullParser = Xml.newPullParser();

        // 入力データを設定
        try {
            xmlPullParser.setInput(new StringReader(xml));
        } catch (XmlPullParserException e) {
            Log.d("sample", "Error");
        }

        // 解析して記事のタイトルやリンク、日時を取得
        try {
            int eventType;
            String data = null;
            int itemFlg = -1;
            String fieldName = null;
            String rawUrl;
            String imgLink;
            RSS item = new RSS();

            eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    // Log.d("sample", "Start document");
                } else if(eventType == XmlPullParser.END_DOCUMENT) {
                    // Log.d("sample", "End document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    data = xmlPullParser.getName();
                    Log.d("sample", "Start tag "+ data);
                    rawUrl = "";
                    imgLink = "";
                    if(data.equals("item")){
                        itemFlg = 1;
                        item = new RSS();
                        for(int i = 0; i < xmlPullParser.getAttributeCount() && i < 1; i++) {
                            rawUrl = xmlPullParser.getAttributeValue(i);
                            m = p.matcher(rawUrl);
                            if (m.find()) {
                                imgLink = "http://k-tai.impress.co.jp/img/ktw/list/"+m.group(1)+"/"+m.group(2)+"/list.png";
                                Log.d("REGEXP1", imgLink);
                                item.setIconUrl(imgLink);
                            }
                        }
                        Log.d("RAW", rawUrl);
                    }
                    fieldName = data;

                } else if(eventType == XmlPullParser.END_TAG) {
                    data = xmlPullParser.getName();
                    Log.d("sample", "End tag "+ data);
                    if(data.equals("item")){
                        itemFlg = 0;
                        list.add(item);
                    }

                } else if(eventType == XmlPullParser.TEXT) {
                    data = xmlPullParser.getText();
                    Log.d("sample", "Text "+ data);

                    if(itemFlg == 1){
                        Log.d("mydebug", fieldName);
                        if(fieldName.equals("title")){
                            Log.d("sample", "title = "+ data);
                            item.setTitle(data);
                            fieldName = "";
                        }
                        if(fieldName.equals("date")){
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            data = data.replace("T", " ").substring(0,16);
                            item.setDesc(data);
                            fieldName = "";
                        }
                        if(fieldName.equals("link")){
                            Log.d("sample", "link = "+ data);
                            item.setUrl(data);
                            fieldName = "";
                        }
                    }

                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.d("sample", "Error");
        }
        return list;
    }

    /**
     * Web上から画像を読み込むタスク
     *
     */
    class DownloadImageTask extends AsyncTask<String,Void,Bitmap> {
        private ImageView imageView;
        private Context context;

        // 初期化
        public DownloadImageTask(ImageView imageView, Context context) {
            this.imageView = imageView;
            this.context = context;
        }

        // execute時のタスク本体。画像をビットマップとして読み込んで返す
        @Override
        protected Bitmap doInBackground(String... params) {
            synchronized (context) {
                try {
                    String str_url = params[0];
                    URL imageUrl = new URL(str_url);
                    InputStream imageIs;

                    // 読み込み実行
                    imageIs = imageUrl.openStream();
                    Bitmap bm = BitmapFactory.decodeStream(imageIs);
                    Log.d("ListViewTest", "画像読み込み完了");

                    return bm;
                } catch (Exception e) {
                    Log.d("ListViewTest", "画像読み込みタスクで例外発生："
                            + e.toString());
                    return null;
                }
            }
        }

        // タスク完了時
        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                Log.d("ListViewTest", "ビューに画像をセット");
                imageView.setImageBitmap(result);
            }
        }
    }
}
