package ann.example.airpollutionmonitor.View;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ann.example.airpollutionmonitor.AppManager;
import ann.example.airpollutionmonitor.Controller.MonitorDataSource;
import ann.example.airpollutionmonitor.Model.Location;
import ann.example.airpollutionmonitor.Model.SensorData;
import ann.example.airpollutionmonitor.R;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private MonitorDataSource monitorDataSource;
    FragmentPagerAdapter adapterViewPager;
    Location location;
    ListView listView;
    ViewPager vpPager;

    public static HomeFragment newInstance(Location location){
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable("location", location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        location = (Location) bundle.getSerializable("location");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        vpPager = view.findViewById(R.id.vpPager_home);

        CircleIndicator indicator = view.findViewById(R.id.indicator);
        indicator.setViewPager(vpPager);

        listView = view.findViewById(R.id.details);
        setCurrentSensorData(location.getSerialNumber());

        return view;
    }

    private void setCurrentSensorData(String serial) {
        int from=0, size=1;

        MonitorDataSource monitorDataSource = MonitorDataSource.getInstance();
        monitorDataSource.getJsonByIndex(serial, from, size)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        // retrofit 통신이 성공했을 때
                        String str = response.body();
                        Log.d(TAG, str);

                        // 데이터 model 객체 생성
                        try {
                            JSONObject jsonObject = new JSONObject(str);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            JSONObject dataJsonObject = jsonArray.getJSONObject(0);
                            //Log.d(TAG, jsonArray.toString());
                            SensorData sensorData = new SensorData(dataJsonObject.getString("time_slot"), dataJsonObject.getDouble("TEM")
                                    ,dataJsonObject.getDouble("HUM") , dataJsonObject.getDouble("CO"), dataJsonObject.getDouble("CH4"));
                            Log.d(TAG, sensorData.toString());
                            AppManager.getInstance().setSensorData(sensorData);

                            // 리스트뷰에 데이터 적용
                            listView.setAdapter(new ListViewAdapter(sensorData));
                            // 아이콘 이미지 변경
                            adapterViewPager = new ImagePagerAdapter(getChildFragmentManager(),
                                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, sensorData);
                            vpPager.setAdapter(adapterViewPager);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        // retrofit 통신이 실패했을 때
                        Log.d(TAG, "통신이 실패하였습니다.");
                    }
                });

    }

    public static class ListViewAdapter extends BaseAdapter{
        SensorData data;

        public ListViewAdapter(SensorData data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final Context context = viewGroup.getContext();

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item_detail, viewGroup, false);

                ImageView icon = view.findViewById(R.id.icon);
                TextView type = view.findViewById(R.id.type);
                TextView value = view.findViewById(R.id.value);

                switch (i){
                    case 0:
                        icon.setVisibility(View.INVISIBLE);
                        type.setText("온도");
                        value.setText(data.getTem() + " ℃");
                        break;
                    case 1:
                        icon.setVisibility(View.INVISIBLE);
                        type.setText("습도");
                        value.setText(data.getHum() + " g/m3");
                        break;
                    case 2:
                        type.setText("일산화탄소(CO)");
                        value.setText(data.getCO() + " ppm");
                        break;
                    case 3:
                        type.setText("메테인(CH4)");
                        value.setText(data.getCH4() + " ppm");

                        break;
                }
            }

            return view;
        }

        private void setIconImage(View view, ImageView imageView, int level){
            switch (level){
                case IconFragment.level1:
                    Glide.with(view)
                            .load(R.drawable.level1)
                            .into(imageView);
                    break;
                case IconFragment.level2:
                    Glide.with(view)
                            .load(R.drawable.level2)
                            .into(imageView);
                    break;
                case IconFragment. level3:
                    Glide.with(view)
                            .load(R.drawable.level3)
                            .into(imageView);
                    break;
                case IconFragment.level4:
                    Glide.with(view)
                            .load(R.drawable.level4)
                            .into(imageView);
                    break;
                case IconFragment.level5:
                    Glide.with(view)
                            .load(R.drawable.level5)
                            .into(imageView);
                    imageView.setImageResource(R.drawable.level5);
                    break;
                case IconFragment.level6:
                    Glide.with(view)
                            .load(R.drawable.level6)
                            .into(imageView);
                    break;
            }
        }
    }

    public static class ImagePagerAdapter extends FragmentPagerAdapter {
        // 데이터 받아 오면 수정해야함
        private static int NUM_ITEMS=2;
        SensorData sensorData;

        public ImagePagerAdapter(@NonNull FragmentManager fm, int behavior, SensorData sensorData) {
            super(fm, behavior);
            this.sensorData = sensorData;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return IconFragment.newInstance("일산화탄소(CO)", getCOLevel(sensorData.getCO()));
                case 1:
                    return IconFragment.newInstance("메테인(CH4)", IconFragment.level2);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        private int getCOLevel(double CO){
            if(CO<10){
                return IconFragment.level1;
            }else if(CO<25){
                return IconFragment.level2;
            }else if(CO<50){
                return IconFragment.level3;
            }else if(CO<100){
                return IconFragment.level4;
            }else if(CO<300){
                return IconFragment.level5;
            }else{
                return IconFragment.level6;
            }
        }
    }

}
