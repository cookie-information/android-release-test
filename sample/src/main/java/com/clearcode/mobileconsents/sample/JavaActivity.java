package com.clearcode.mobileconsents.sample;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.clearcode.mobileconsents.domain.Consent;
import com.clearcode.mobileconsents.networking.CallListener;
import com.clearcode.mobileconsents.sdk.MobileConsentSdk;
import java.io.IOException;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class JavaActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    MobileConsentSdk sdk = new MobileConsentSdk.Builder().postUrl(BuildConfig.BASE_URL).build();

    sdk.getConsent(
        UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767"),
        new CallListener<Consent>() {
          @Override
          public void onFailure(@NotNull IOException error) {
            Log.e("MainActivity", error.getMessage(), error);
          }

          @Override
          public void onSuccess(Consent result) {
            Log.e("MainActivity", result.toString());
          }
        }
    );
  }
}
