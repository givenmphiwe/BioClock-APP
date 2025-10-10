package polysphere.dms.com.dtts.HeaderView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import polysphere.dms.com.dtts.R;

public class HeaderView extends LinearLayout {
    private ImageView ivNav;
    private TextView tvTitle;
    private ImageView ivProfile;
    private ImageView ivHome;

    public HeaderView(Context context) {
        this(context, null);
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context ctx) {
        LayoutInflater.from(ctx).inflate(R.layout.view_app_header, this, true);
        ivNav = findViewById(R.id.ivNav);
        tvTitle = findViewById(R.id.tvHeaderTitle);
        ivProfile = findViewById(R.id.ivProfile);
        ivHome = findViewById(R.id.ivHome);
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public void setNavIcon(int resId) {
        ivNav.setImageResource(resId);
    }

    public void setOnNavClickListener(OnClickListener l) {
        ivNav.setOnClickListener(l);
    }

    public void setOnProfileClickListener(OnClickListener l) {
        ivProfile.setOnClickListener(l);
    }

    public void setOnHomeClickListener(OnClickListener l) {
        ivHome.setOnClickListener(l);
    }
}