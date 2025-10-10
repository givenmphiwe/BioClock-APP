package polysphere.dms.com.dtts.sync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;

import polysphere.dms.com.dtts.R;
import polysphere.dms.com.dtts.enrollment.enrollment;

public class sync extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        TextView tvStatusBar = findViewById(R.id.tvStatusBar);
        tvStatusBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Navigate to enrollment screen
                Intent intent = new Intent(sync.this, enrollment.class);
                // optional: pass a title/name
                intent.putExtra("extra_name", "Gang Enrolment");
                startActivity(intent);
            }
        });
    }
}
