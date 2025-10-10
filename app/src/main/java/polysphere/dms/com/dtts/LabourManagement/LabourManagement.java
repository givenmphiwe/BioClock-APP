package polysphere.dms.com.dtts.LabourManagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import polysphere.dms.com.dtts.Home.GridSpacingItemDecoration;
import polysphere.dms.com.dtts.Home.MenuAdapter;
import polysphere.dms.com.dtts.Home.MenuItem;
import polysphere.dms.com.dtts.R;

public class LabourManagement extends AppCompatActivity {

    private RecyclerView rvMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labour_management);

        rvMenu = findViewById(R.id.rvMenu);
        int spanCount = 3;
        rvMenu.setLayoutManager(new GridLayoutManager(this, spanCount));
        rvMenu.setHasFixedSize(true);

        int spacingPx = dpToPx(1);
        rvMenu.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacingPx, true));

        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Work Place Attendance", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Leave Management", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Transfer Management", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Labour Planned Unavailables", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Employee Development", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Poor Work Attendance", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Administration", LabourManagement.class));
        menuItems.add(new MenuItem(R.drawable.ic_placeholder, "Reports", LabourManagement.class));

        MenuAdapter adapter = new MenuAdapter(this, menuItems, new MenuAdapter.Listener() {
            @Override
            public void onItemClicked(MenuItem item) {
                if (item.getDestination() != null) {
                    Intent intent = new Intent(LabourManagement.this, item.getDestination());
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
