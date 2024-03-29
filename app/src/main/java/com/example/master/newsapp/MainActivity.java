package com.example.master.newsapp;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.master.newsapp.api.ApiClient;
import com.example.master.newsapp.api.ApiInterface;
import com.example.master.newsapp.models.Article;
import com.example.master.newsapp.models.News;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.master.newsapp.NotificationUtils.ANDROID_CHANNEL_ID;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final int NOTIF_ID = 1;
    PendingIntent pendingIntent;

    // APIKEY NEWS.ORG
    public static final String API_KEY = "9db9d14f73de4c34a4a5184c6f685d52";

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private TextView topHeadlines;
    private SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadlines = findViewById(R.id.topHeadlines);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager( MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

       // LoadJson("");
        onLoadingSwipeRefresh("");

    }

    public void LoadJson(final String keyword){

        topHeadlines.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        String language = Utils.getLanguage();
        Call<News> call;

        if (keyword.length() > 0){
            call = apiInterface.getMewsSearch(keyword, language, "publishedAt", API_KEY);
        } else {
            call = apiInterface.getNews(country, API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticles() != null){
                    if (!articles.isEmpty()){
                        articles.clear();
                    }
                    articles = response.body().getArticles();
                    adapter = new Adapter(articles, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();


                    topHeadlines.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                }else {
                    topHeadlines.setVisibility(View.INVISIBLE);

                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No result!" , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                topHeadlines.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void initListener()  {

        adapter.setOnItemClickListener(new Adapter.OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position){
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url",(Serializable)  article.getUrl());
                intent.putExtra("title", (Serializable) article.getTitle());
                intent.putExtra("img", (Serializable) article.getUrlToImage());
                intent.putExtra("date", (Serializable) article.getPublishedAt());
                intent.putExtra("source", article.getSource());
                intent.putExtra("author", (Serializable)  article.getAuthor());

                startActivity(intent);
            }
        });
    }


    //MENU BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //SEARCH MENU
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length()>2){
                    onLoadingSwipeRefresh(query);
                }
                else{
                    Toast.makeText(MainActivity.this, "Type more than two letters..", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false, false);
        return true;
    }


    ///MENU TOOLBAR
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent actionSettings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(actionSettings);
                break;
            case R.id.action_update:
                Intent updste = new Intent(Intent.ACTION_VIEW, Uri.parse("http://news.org/"));
                pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, updste, 0);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    showNotifOreo();
                else showNotifDefault();
        }
        return super.onOptionsItemSelected(item);
    }



    //REFRESH
    @Override
    public void onRefresh() {
        LoadJson("");
    }

    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);
                    }
                }
        );
    }



    //NOTIFICATION UPDATE
    public void showNotifDefault(){
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(MainActivity.this)
                .setSmallIcon(R.drawable.icon_news)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources()
                        , R.drawable.icon_news))
                .setContentTitle(getResources().getString(R.string.content_title))
                .setContentText(getResources().getString(R.string.content_text))
                .setSubText(getResources().getString(R.string.subtext))
                .setAutoCancel(true);

        NotificationManagerCompat notifManager = NotificationManagerCompat.from(getApplicationContext());
        notifManager.notify(NOTIF_ID, notifBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotifOreo(){
        Notification.Builder notifBuilder = new Notification.Builder(MainActivity.this, ANDROID_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_news)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources()
                        , R.drawable.icon_news))
                .setContentTitle(getResources().getString(R.string.content_title))
                .setContentText(getResources().getString(R.string.content_text))
                .setSubText(getResources().getString(R.string.subtext))
                .setAutoCancel(true);

        NotificationUtils utils = new NotificationUtils(MainActivity.this);
        utils.getManager().notify(NOTIF_ID, notifBuilder.build());
    }

}
