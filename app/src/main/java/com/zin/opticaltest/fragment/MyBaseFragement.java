package com.zin.opticaltest.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zin.opticaltest.R;
import com.zin.opticaltest.activity.DetailPagerActivity;
import com.zin.opticaltest.adapter.PaperItemRecyclerAdapter;
import com.zin.opticaltest.dao.PaperBankDao;
import com.zin.opticaltest.entity.TestBank;
import com.zin.opticaltest.ui.MyItemDecoration;
import com.zin.opticaltest.utils.LogUtils;
import com.zin.opticaltest.utils.ToastUtils;
import com.zin.opticaltest.utils.UIUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SQLQueryListener;

/**
 * Created by ZIN on 2016/4/1.
 */
public abstract class MyBaseFragement extends Fragment {
    @Bind(R.id.progressBar2)
    ProgressBar progressBar2;
    private View recylerViewLayout;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private PaperItemRecyclerAdapter mAdapter;
    public int lastVisibleItem;
    private int page = 1;
    private boolean isLoading = false;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //  return this.onSubCreateView();
        initView();
        ButterKnife.bind(this, recylerViewLayout);
        return recylerViewLayout;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // this.onSubActivityCreated();
        setSwipeFlashLayout();
        setRecyclerView();
        mSwipeLayout.setRefreshing(true);
        getDataFromNet(1);
    }

    /*
    *  private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mSwipeLayout.setRefreshing(false);
            progressBar2.setVisibility(View.GONE);
            switch (msg.what) {
                case 1:
                case 2: {

                    List<TestBank> datas = (List<TestBank>) msg.obj;

                    LogUtils.i("5执行handler");
                    if (mAdapter == null) {
                        mAdapter = new PaperItemRecyclerAdapter(datas);
                        mAdapter.setHasMoreData(true);

                    }
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(mAdapter);
                    LogUtils.i("datas大小B=" + datas.size());
                    if (datas == null || datas.size() == 0) {
                        mAdapter.setHasMoreDataAndFooter(false, false);
                        LogUtils.i("datas为0执行了");
                    } else {

                    }

                    if (page == 1) {
                        mAdapter.clear();
                        mAdapter.appendToList(datas);

                        LogUtils.i("svroll0");
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(0);

                    } else {
                        // mAdapter.setHasMoreData(true);
                        int position = mAdapter.getItemCount();
                        mAdapter.appendToList(datas);

                        LogUtils.i("scrollposition" + position);
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(position);
                    }
                    mAdapter.setOnItemClickListener(new PaperItemRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(View view, TestBank data) {
                            //Toast.makeText(UIUtils.getContext(), "data:" + data, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UIUtils.getContext(), DetailPagerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("PAPERINFO", data);
                            intent.putExtras(bundle);

                            startActivity(intent);
                        }
                    });
                    break;
                }
                case 0: {

                    break;
                }


            }


        }
    };*/

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                case 2: {
                    mSwipeLayout.setRefreshing(false);
                    progressBar2.setVisibility(View.GONE);
                    isLoading=false;
                    List<TestBank> datas = (List<TestBank>) msg.obj;

                    LogUtils.i("5执行handler");

                    if (mAdapter == null) {
                        mAdapter = new PaperItemRecyclerAdapter(datas);

                        mRecyclerView.setAdapter(mAdapter);

                    }

                   // mAdapter.setHasMoreData(true);
                    LogUtils.i("datas大小B=" + datas.size());
                    if (datas == null || datas.size() == 0) {
                       mAdapter.setHasMoreDataAndFooter(false,true);
                        LogUtils.i("datas为0执行了");
                    } else {

                    }

                    if (page == 1) {
                        mAdapter.clear();
                        mAdapter.appendToList(datas);
                        mAdapter.notifyDataSetChanged();

                        //仅仅缓存一页数据

                        saveDateToLocal(datas);


                    } else {


                        mAdapter.appendToList(datas);


                        mAdapter.notifyDataSetChanged();

                    }
                    mAdapter.setOnItemClickListener(new PaperItemRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(View view, TestBank data) {
                            //Toast.makeText(UIUtils.getContext(), "data:" + data, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UIUtils.getContext(), DetailPagerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("PAPERINFO", data);
                            LogUtils.i("045id："+data.getPaperId());
                            intent.putExtras(bundle);

                            startActivity(intent);
                        }
                    });
                    break;
                }
                case -1: {
                    ToastUtils.showToast(getActivity(),"无法连接到服务器");
                    List<TestBank> datas = getDataFromLocal();
                    mSwipeLayout.setRefreshing(false);
                    progressBar2.setVisibility(View.GONE);
                    isLoading=false;


                    LogUtils.i("-1");

                    if (mAdapter == null) {
                        mAdapter = new PaperItemRecyclerAdapter(datas);

                        mRecyclerView.setAdapter(mAdapter);

                    }
                    mAdapter.setHasNet(false);
                    mAdapter.notifyDataSetChanged();
                    mAdapter.setOnItemClickListener(new PaperItemRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(View view, TestBank data) {
                            //Toast.makeText(UIUtils.getContext(), "data:" + data, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UIUtils.getContext(), DetailPagerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("PAPERINFO", data);
                            intent.putExtras(bundle);

                            startActivity(intent);
                        }
                    });






                    break;
                }


            }


        }
    };
   /* private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mSwipeLayout.setRefreshing(false);
            progressBar2.setVisibility(View.GONE);
            switch (msg.what) {
                case 1:
                case 2: {
                    List<TestBank> datas = (List<TestBank>) msg.obj;

                    LogUtils.i("5执行handler");
                    if (mAdapter == null) {
                        mAdapter = new PaperItemRecyclerAdapter(datas);
                        mRecyclerView.setAdapter(mAdapter);

                    }
                    LogUtils.i("datas大小B=" + datas.size());
                    if (datas == null || datas.size() ==0) {
                      //  mAdapter.setHasMoreDataAndFooter(false, false);
                        mAdapter.setHasMoreData(false);
                        LogUtils.i("datas为0执行了");
                    } else {
                        mAdapter.setHasMoreData(true);
                    }

                    if (page == 1) {
                        mAdapter.clear();
                    }
                    mAdapter.appendToList(datas);
                    mAdapter.notifyDataSetChanged();
                    mAdapter.setOnItemClickListener(new PaperItemRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(View view, TestBank data) {
                            //Toast.makeText(UIUtils.getContext(), "data:" + data, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UIUtils.getContext(), DetailPagerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("PAPERINFO", data);
                            intent.putExtras(bundle);

                            startActivity(intent);
                        }
                    });
                    break;
                }
                case 0: {

                    break;
                }


            }


        }
    };
*/




    /**
     * 设置刷新模块的方法
     */
    private void setSwipeFlashLayout() {
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                page = 1;

                getDataFromNet(page);

            }
        });
    }

    /**
     * 设置RecyclerView的方法
     */
    private void setRecyclerView() {
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        //mAdapter = new MyRecyclerViewAdapter(getTitleData());
       /* mRecyclerView.setAdapter(mAdapter);*/
        //每个item高度一致，可设置为true，提高性能
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);


        //分隔线
        mRecyclerView.addItemDecoration(new MyItemDecoration(getActivity()));
        ///为每个item增加响应事件


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int currentScrollState = newState;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if ((visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE &&
                        (lastVisibleItem) >= totalItemCount - 1)) {


                    // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
                    if (!isLoading) {
                        isLoading=true;
                        //mAdapter.setHasFooter(true);
                        mAdapter.setHasMoreDataAndFooter(true,true);
                        page = page + 1;

                        getDataFromNet(page);
                    }

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
            }
        });

/*

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
               mAdapter.setHasFooter(true);
                    page = current_page;
                    new  Thread(new Runnable() {
                        @Override
                        public void run() {
                            SystemClock.sleep(3000);
                            getDataFromNet(page);
                        }
                    }).start();

            }
        });
*/


    }

    /**
     * init组件的方法
     */
    private void initView() {

        recylerViewLayout = UIUtils.inflate(R.layout.layout_recylerview);
        mSwipeLayout = (SwipeRefreshLayout) recylerViewLayout.findViewById(R.id.swipe_container);
        mRecyclerView = (RecyclerView) recylerViewLayout.findViewById(R.id.recyclerView);
    }


    private void getDataFromNet(int page) {
        LogUtils.i("0getDataFromNet请求网络获取试题列表json");
        //创建okHttpClient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
       // String url = "http://192.168.1.122:8080/otweb/testBank/tlist.action";
        HttpUrl url = HttpUrl.parse(setURL()).newBuilder()
                .addQueryParameter("index", page+"")
                .build();
        LogUtils.i("1.网络地址"+url.toString());
        //创建一个Request
        final Request request = new Request.Builder()
                .url(url)
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                LogUtils.i("3.onFailure" + e);
              //  getDataFromBmod();
                Message message=handler.obtainMessage();
                message.what=-1;
                handler.sendMessage(message);
            }
            @Override
            public void onResponse(final Response response) throws IOException {
                String resultJson = response.body().string();
                LogUtils.i("4.请求结果" + resultJson);
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonArray Jarray = parser.parse(resultJson).getAsJsonArray();
                ArrayList<TestBank> lcs = new ArrayList<TestBank>();
                for (JsonElement obj : Jarray) {
                    TestBank cse = gson.fromJson(obj, TestBank.class);
                    lcs.add(cse);
                }
                Message message = handler.obtainMessage();
                LogUtils.i("134paperid:"+lcs.get(0).getPaperId());
                message.what = 1;
                message.obj = lcs;
                handler.sendMessage(message);
            }
        });
    }

    private List<TestBank> getDataFromLocal(){
        PaperBankDao paperBankDao=new PaperBankDao(getActivity());
        List<TestBank> list = paperBankDao.showALL();
        return list;
    }
    private void saveDateToLocal(List<TestBank> list){
        PaperBankDao paperBankDao=new PaperBankDao(getActivity());
        if (!paperBankDao.isEmpty()){
            paperBankDao.clear();
        }
        paperBankDao.addList(list);



    }
    public void getDataFromBmod() {
        LogUtils.i("通过Bmod获取数据");
        //查询所有的游戏得分记录
        /*Bmode后台的数据库字段必须全部小写*/

        String bql = "select * from test_bank";
        BmobQuery<TestBank> query = new BmobQuery<TestBank>();
        //设置查询的SQL语句
        query.setSQL(bql);
        query.doSQLQuery(UIUtils.getContext(), new SQLQueryListener<TestBank>() {

            @Override
            public void done(BmobQueryResult<TestBank> result, BmobException e) {
                if (e == null) {

                    List<TestBank> list = result.getResults();
                    LogUtils.i("Bmod数据获取成功");

                    //   List<String> titles = new ArrayList<String>();
                    if (list != null && list.size() > 0) {
                        for (TestBank i : list) {
                            LogUtils.i("ff" + i.getTitle());
                            LogUtils.i("ff" + i.getRespondencetime());
                        }


                        Message message = handler.obtainMessage();
                        message.what = 2;
                        message.obj = list;
                        handler.sendMessage(message);
                        mSwipeLayout.setRefreshing(false);
                        //滚动到列首部--->这是一个很方便的api，可以滑动到指定位置
                        mRecyclerView.scrollToPosition(0);


                    } else {
                        Log.i("smile", "查询成功，无数据返回");
                        Toast.makeText(getActivity(),"无数据返回",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i("smile", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                    Toast.makeText(getActivity(),"错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public abstract String setURL();

    public abstract String setId();


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
