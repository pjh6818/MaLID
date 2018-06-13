package malid.datacollector.Helpers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import malid.datacollector.Helpers.historyitem;
import malid.datacollector.R;

import java.util.List;

/**
 * Created by woong on 2015. 1. 20..
 */
public class historyitemadapter extends RecyclerView.Adapter<historyitemadapter.ViewHolder> {

    private List<historyitem> albumList;
    private int itemLayout;

    /**
     * 생성자
     * @param items
     * @param itemLayout
     */
    public historyitemadapter(List<historyitem> items , int itemLayout){

        this.albumList = items;
        this.itemLayout = itemLayout;
    }

    /**
     * 레이아웃을 만들어서 Holer에 저장
     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout,viewGroup,false);
        return new ViewHolder(view);
    }

    /**
     * listView getView 를 대체
     * 넘겨 받은 데이터를 화면에 출력하는 역할
     *
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        historyitem item = albumList.get(position);
        viewHolder.idx.setText(item.getidx());
        viewHolder.ttime.setText(item.gettime());
        viewHolder.heartrate.setText(item.getheartrate());
        viewHolder.classs.setText(item.getClasss());
        viewHolder.itemView.setTag(item);

    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    /**
     * 뷰 재활용을 위한 viewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView idx;
        public TextView ttime;
        public TextView heartrate;
        public TextView classs;

        public ViewHolder(View itemView){
            super(itemView);

            idx = (TextView) itemView.findViewById(R.id.itemidx);
            ttime = (TextView) itemView.findViewById(R.id.itemtime);
            heartrate = (TextView) itemView.findViewById(R.id.itemheart);
            classs = (TextView) itemView.findViewById(R.id.itemclass);
        }

    }
}