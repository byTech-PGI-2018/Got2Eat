package bytech.got2eat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class aboutUs extends AppCompatActivity {

    ImageView facebook;
    ImageView launchrock;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        facebook = findViewById(R.id.face);
        launchrock = findViewById(R.id.launch);

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openFacebook = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/got2eatAPP/"));
                getApplicationContext().startActivity(openFacebook);
            }
        });

        launchrock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openLaunchrock = new Intent(Intent.ACTION_VIEW, Uri.parse("http://got2eat.launchrock.com"));
                getApplicationContext().startActivity(openLaunchrock);
            }
        });
    }

}
