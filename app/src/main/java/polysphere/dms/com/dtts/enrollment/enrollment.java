package polysphere.dms.com.dtts.enrollment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import polysphere.dms.com.dtts.Home.Home;
import polysphere.dms.com.dtts.R;

public class enrollment extends AppCompatActivity {

    private void navigateHome() {
        Intent intent = new Intent(enrollment.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        // Header wiring
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        ImageView ivHome = findViewById(R.id.ivBack);

        // Skip button
        Button btnSkip = findViewById(R.id.btnSkip);

        // Set dynamic title
        tvHeaderTitle.setText("Gang Enrolment");

        // Home icon: go home
        if (ivHome != null) {
            ivHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateHome();
                }
            });
        }

        // Skip: also go home
        if (btnSkip != null) {
            btnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateHome();
                }
            });
        }
    }
}
