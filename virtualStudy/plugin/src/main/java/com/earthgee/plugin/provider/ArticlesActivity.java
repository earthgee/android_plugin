package com.earthgee.plugin.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.earthgee.plugin.R;
import com.earthgee.plugin.contentProvider.Articles;

/**
 * Created by zhaoruixuan on 2017/8/8.
 */
public class ArticlesActivity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private final static int ADD_ARTICAL_ACTIVITY=1;
    private final static int EDIT_ARTICAL_ACTIVITY=2;

    private ArticlesAdapter aa=null;
    private ArticleAdapter adapter=null;
    private ArticleObserver observer=null;

    private ListView articleList=null;
    private Button addButton=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articles_rename);

        aa=new ArticlesAdapter(this);

        articleList= (ListView) findViewById(R.id.listview);
        adapter=new ArticleAdapter(this);
        articleList.setAdapter(adapter);
        articleList.setOnItemClickListener(this);

        observer=new ArticleObserver(new Handler());
        getContentResolver().registerContentObserver(Articles.CONTENT_URI,true,observer);

        addButton= (Button) findViewById(R.id.btn_add);
        addButton.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(observer);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.equals(addButton)){
            Intent intent=new Intent(this,ArticleActivity.class);
            startActivityForResult(intent,ADD_ARTICAL_ACTIVITY);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(this,ArticleActivity.class);

        Article article=aa.getArticleByPos(position);
        intent.putExtra(Articles.ID,article.getId());
        intent.putExtra(Articles.TITLE,article.getTitle());
        intent.putExtra(Articles.ABSTRACT,article.getAbs());
        intent.putExtra(Articles.URL,article.getUrl());

        startActivityForResult(intent,EDIT_ARTICAL_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case ADD_ARTICAL_ACTIVITY:{
                if(resultCode==RESULT_OK){
                    String title=data.getStringExtra(Articles.TITLE);
                    String abs=data.getStringExtra(Articles.ABSTRACT);
                    String url=data.getStringExtra(Articles.URL);

                    Article article=new Article(-1,title,abs,url);
                    aa.insertArticle(article);
                }
                break;
            }
            case EDIT_ARTICAL_ACTIVITY:{
                if(resultCode==RESULT_OK){
                    int action=data.getIntExtra(ArticleActivity.EDIT_ARTICLE_ACTION,-1);
                    if(action==ArticleActivity.MODIFY_ARTICLE){
                        int id=data.getIntExtra(Articles.ID,-1);
                        String title=data.getStringExtra(Articles.TITLE);
                        String abs=data.getStringExtra(Articles.ABSTRACT);
                        String url=data.getStringExtra(Articles.URL);

                        Article article=new Article(id,title,abs,url);
                        aa.updateArticle(article);
                    }else if(action==ArticleActivity.DELETE_ARTICLE){
                        int id=data.getIntExtra(Articles.ID,-1);
                        aa.removeArticle(id);
                    }
                }
                break;
            }
        }
    }

    private class ArticleObserver extends ContentObserver{

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ArticleObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            adapter.notifyDataSetChanged();
        }
    }

}
