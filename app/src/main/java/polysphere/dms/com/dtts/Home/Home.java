package polysphere.dms.com.dtts.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import polysphere.dms.com.dtts.LabourManagement.LabourManagement;
import polysphere.dms.com.dtts.R;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private RecyclerView rvMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvMenu = findViewById(R.id.rvMenu);
        int spanCount = 3;
        rvMenu.setLayoutManager(new GridLayoutManager(this, spanCount));
        rvMenu.setHasFixedSize(true);

        int spacingPx = dpToPx(1);
        rvMenu.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacingPx, true));

        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Labour Management", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Health & Safety", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Productions", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Communication", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Administration", LabourManagement.class));

        MenuAdapter adapter = new MenuAdapter(this, menuItems, new MenuAdapter.Listener() {
            @Override
            public void onItemClicked(MenuItem item) {
                if (item.getDestination() != null) {
                    Intent intent = new Intent(Home.this, item.getDestination());
                    startActivity(intent);
                }
            }
        });
        rvMenu.setAdapter(adapter);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
