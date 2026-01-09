package com.yzplan.lanbase.app.bean.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ArticleListResponse implements Serializable {
    @SerializedName("curPage")
    private int curPage;
    @SerializedName("datas")
    private List<Article> datas;
    @SerializedName("pageCount")
    private int pageCount;
    @SerializedName("total")
    private int total;
    @SerializedName("over")
    private boolean over;

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public List<Article> getDatas() {
        return datas;
    }

    public void setDatas(List<Article> datas) {
        this.datas = datas;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isOver() {
        return over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }
}
