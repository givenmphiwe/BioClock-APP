package polysphere.dms.com.dtts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.senter.function.openapi.unstable.FingerprintE;

import org.json.JSONObject;

import java.io.File;

import polysphere.dms.com.dtts.services.AuthManager;
import polysphere.dms.com.dtts.sync.sync;

public class Login extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 1001;

    private AuthManager authManager;

    private FingerprintE fingerprintE;
    private TextView tv_info;
//    private Button btnFingerprint;
    private ImageButton btnFingerprint;
    private Button btnLogin;
    private EditText etPassword;
    private EditText etIndustry;

    private volatile boolean isDoing = false;
    private volatile boolean deviceOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager(this);

        tv_info = findViewById(R.id.tv_info);
        btnFingerprint = findViewById(R.id.btn_fingerprint);
        btnLogin = findViewById(R.id.btn_login);
        etPassword = findViewById(R.id.et_password);
        etIndustry = findViewById(R.id.et_industry);

        // initialize fingerprint SDK
        fingerprintE = FingerprintE.getInstance(this);
        boolean isSuccess = fingerprintE.init();
        Toast.makeText(this, isSuccess ? "init success" : "init failed", Toast.LENGTH_SHORT).show();

        // request permissions if needed, then open device
        ensurePermissionsAndOpenDevice();

        // login button: industry number + password
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String industry = etIndustry.getText().toString().trim();
                final String pwd = etPassword.getText().toString();

                if (industry.isEmpty() || pwd.isEmpty()) {
                    Toast.makeText(Login.this, "Industry number and password required", Toast.LENGTH_SHORT).show();
                    return;
                }

                tv_info.setText("Logging in...");

                authManager.login(industry, pwd, false, new AuthManager.Callback() {
                    @Override
                    public void onSuccess(JSONObject userInfo) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_info.setText("Login success");
                                Intent intent = new Intent(Login.this, sync.class);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        final String e = error; // copy to a final local
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_info.setText("Login failed: " + e);
                            }
                        });
                    }
                });
            }
        });

        // password visibility toggle logic
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Drawable[] drawables = etPassword.getCompoundDrawables();
                    Drawable drawableEnd = drawables[2];
                    if (drawableEnd != null) {
                        int touchAreaStart = etPassword.getWidth() - etPassword.getPaddingEnd() - drawableEnd.getBounds().width();
                        if (event.getX() >= touchAreaStart) {
                            int selection = etPassword.getSelectionStart();
                            if ((etPassword.getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                etPassword.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                        getResources().getDrawable(R.drawable.ic_visibility_off), null);
                            } else {
                                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                etPassword.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                        getResources().getDrawable(R.drawable.ic_visibility), null);
                            }
                            etPassword.setSelection(selection);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        // fingerprint save (enroll) flow
        btnFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDoing) return;

                ensureDeviceOpenThen(new Runnable() {
                    @Override
                    public void run() {
                        performFingerprintSave();
                    }
                });
            }
        });
    }

    private void ensurePermissionsAndOpenDevice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            openDeviceAsync();
        }
    }

    private void ensureDeviceOpenThen(final Runnable then) {
        if (deviceOpened) {
            then.run();
            return;
        }
        openDeviceAsync(new Runnable() {
            @Override
            public void run() {
                then.run();
            }
        });
    }

    private void openDeviceAsync() {
        openDeviceAsync(null);
    }

    private void openDeviceAsync(final Runnable onSuccess) {
        if (isDoing) return;
        isDoing = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean opened = fingerprintE.openDevice();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (opened) {
                                deviceOpened = true;
                                Toast.makeText(Login.this, "Device opened", Toast.LENGTH_SHORT).show();
                            } else {
                                tv_info.setText("Cannot open device");
                                Toast.makeText(Login.this, "Open failed", Toast.LENGTH_SHORT).show();
                            }
                            if (opened && onSuccess != null) {
                                onSuccess.run();
                            }
                        }
                    });
                } finally {
                    isDoing = false;
                }
            }
        }).start();
    }

    private void performFingerprintSave() {
        if (isDoing) return;
        isDoing = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // clear status
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_info.setText("");
                        }
                    });

                    // capture image
                    FingerprintE.FingerImage fingerImage = fingerprintE.getFingerImage();
                    if (fingerImage == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_info.setText("Capture failed");
                            }
                        });
                        return;
                    }

                    // create template
                    byte[] template = fingerprintE.createFingerTemplate(fingerImage.image);
                    if (template == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_info.setText("Template creation failed");
                            }
                        });
                        return;
                    }

                    // save template
                    boolean isSuccess = FileOperate.addData(template);
                    if (isSuccess) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_info.setText("FingerPrint captured");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_info.setText("failed to capture fingerPrint");
                            }
                        });
                    }
                } finally {
                    isDoing = false;
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean granted = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                openDeviceAsync();
            } else {
                Toast.makeText(this, "Storage permissions required for fingerprint templates", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fingerprintE != null) fingerprintE.uninit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
