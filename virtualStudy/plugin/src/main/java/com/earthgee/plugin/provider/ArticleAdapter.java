package com.earthgee.plugin.provider;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.earthgee.plugin.R;

/**
 * Created by zhaoruixuan on 2017/8/8.
 */
public class ArticleAdapter extends BaseAdapter{

    private Context context;
    private ArticlesAdapter articlesAdapter;

    public ArticleAdapter(Context context){
        this.context=context;
        articlesAdapter=new ArticlesAdapter(context);
    }

    @Override
    public int getCount() {
        return articlesAdapter.getAllArticles().size();
    }

    @Override
    public Object getItem(int position) {
        return articlesAdapter.getArticleByPos(position);
    }

    @Override
    public long getItemId(int position) {
        return articlesAdapter.getArticleByPos(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Article article= (Article) getItem(position);

        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item,null);
        }

        TextView title= (TextView) convertView.findViewById(R.id.title);
        title.setText(article.getTitle());

        TextView abs= (TextView) convertView.findViewById(R.id.abs);
        abs.setText(article.getAbs());

        TextView url= (TextView) convertView.findViewById(R.id.url);
        url.setText(article.getUrl());
        return convertView;
    }
}
