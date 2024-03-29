package com.example.master.newsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class NewsDetailActivity extends AppCompatActivity implements  AppBarLayout.OnOffsetChangedListener  {

    private ImageView imageView;
    private TextView appbar_title,date,time,title;
    private boolean isHideToolbarView = false;
    private FrameLayout date_behavior;
    private LinearLayout titleAppbar;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private String mUrl, mImg, mDate, mSource, mAuthor, mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("");

        appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(this);
        date_behavior = findViewById(R.id.date_behavior);
        titleAppbar  = findViewById(R.id.title_appbar);
        imageView = findViewById(R.id.backdrop);
        appbar_title = findViewById(R.id.title_on_appbar);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        title = findViewById(R.id.title);


        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        mImg = intent.getStringExtra("img");
        mTitle = intent.getStringExtra("title");
        mDate = intent.getStringExtra("date");
        mSource = intent.getStringExtra("sources");
        mAuthor = intent.getStringExtra("author");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(Utils.getRandomDrawbleColor());

        Glide.with(this)
                .load(mImg)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);

        appbar_title.setText("Back");
        date.setText(Utils.DateFormat(mDate));
        title.setText(mTitle);


        String author = null ;
        if (mAuthor != null || mAuthor != ""){
            mAuthor = "" + mAuthor ;
        }

        time.setText( mAuthor + " \u2022 " + Utils.DateFormat(mDate) );

        initWebView(mUrl);

    }


    //MENAMPILKAN WEB VIEW
    private void initWebView(String url){
        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }



    //NAVIGASI BACK/KEMBALI
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        supportFinishAfterTransition();
    }


    //VISIBILITI SCRILL UNTUK APPBAR
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset)/(float) maxScroll;

        if (percentage == 1f && isHideToolbarView){
           date_behavior.setVisibility(View.GONE);
            appbar_title.setVisibility(View.VISIBLE);
            isHideToolbarView = !isHideToolbarView;
        }
        else if (percentage < 1f && !isHideToolbarView) {
           date_behavior.setVisibility(View.VISIBLE);
            appbar_title.setVisibility(View.GONE);
            isHideToolbarView = !isHideToolbarView;
        }
    }
}
